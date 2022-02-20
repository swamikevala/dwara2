package org.ishafoundation.dwaraapi.storage.storagetype.disk.job;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.storage.model.DiskJob;
import org.ishafoundation.dwaraapi.storage.model.GroupedJobsCollection;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetype.disk.thread.executor.DiskTaskThreadPoolExecutor;
import org.ishafoundation.dwaraapi.storage.storagetype.job.JobSelector;
import org.ishafoundation.dwaraapi.storage.storagetype.thread.AbstractStoragetypeJobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("disk"+DwaraConstants.STORAGETYPE_JOBMANAGER_SUFFIX)
@Scope("prototype")
public class DiskJobManager extends AbstractStoragetypeJobManager {


    private static final Logger logger = LoggerFactory.getLogger(DiskJobManager.class);

	@Autowired
	private DiskTaskThreadPoolExecutor diskTaskThreadPoolExecutor;	

	@Autowired
	private JobDao jobDao;
	
	@Override
    public void run() {
		logger.info("Disk job manager kicked off");
		List<StorageJob> storageJobsList = getStorageJobList();
		
		JobSelector js = new JobSelector();
		GroupedJobsCollection gjc = js.groupJobsBasedOnVolumeTag(storageJobsList); // Grouping the storage Jobs on volume
		Map<String, List<StorageJob>> volumeTag_volumeTagGroupedJobs = gjc.getVolumeTag_volumeTagGroupedJobs();
		
		storageJobsList = removeJobsThatDontHaveNeededDiskAttached(storageJobsList);
		
		// Removing all same pool jobs that are running currently. We want only one job to write on a volume at any point in time... 
		List<Job> inprogressWriteJobs = jobDao.findAllByStatusAndStoragetaskActionIdOrderById(Status.in_progress, Action.write);
		
		Set<String> volumeTagSet = volumeTag_volumeTagGroupedJobs.keySet();
		for (String nthVolumeTag : volumeTagSet) {
			for (Job nthInprogressJob : inprogressWriteJobs) {
				if(nthInprogressJob.getVolume().getId().equals(nthVolumeTag)) {
					storageJobsList.removeAll(volumeTag_volumeTagGroupedJobs.get(nthVolumeTag));
					volumeTag_volumeTagGroupedJobs.remove(nthVolumeTag);
					break;
				}
			}
		}

		// Now just select the job from the leftovers
		volumeTagSet = volumeTag_volumeTagGroupedJobs.keySet();
		for (String nthVolumeTag : volumeTagSet) {
			for (StorageJob storageJob : storageJobsList) {
				if(storageJob.getVolume().getId().equals(nthVolumeTag)) {
					Job job = storageJob.getJob();
					logger.debug("Processing disk job " + job.getId());	
					job = jobDao.findById(job.getId()).get();
					if(job.getStatus() != Status.queued) { // This check is to avoid race condition on jobs. This check is not needed for non-blocking jobs as Jobselector will take care of it...
						logger.info(job.getId() + " - job probably already picked up for processing in the last run. Skipping it");
						continue;
					}
	
					Volume volume = storageJob.getVolume();
					job.setDevice(null);// TODO "???"
					job.setVolume(volume);
					storageJob.setJob(job);
					
					DiskJob dj = new DiskJob();
					dj.setStorageJob(storageJob);
					dj.setMountPoint(volume.getDetails().getMountpoint());
					// dj.setDeviceWwnId();
	
					logger.debug("Launching separate disk task thread -----------");
					DiskTask diskTask = new DiskTask();//applicationContext.getBean(DiskTask.class); 
					diskTask.setDiskJob(dj);
					diskTaskThreadPoolExecutor.getExecutor().execute(diskTask);
					
					break;
				}
			}
		}
	}
	
	
	private List<StorageJob>  removeJobsThatDontHaveNeededDiskAttached(List<StorageJob> storageJobsList) {
		List<StorageJob> onlyDiskOnLibraryStorageJobsList = new ArrayList<StorageJob>(); 
		for (int i = 0; i < storageJobsList.size(); i++) {
			StorageJob nthStorageJob = storageJobsList.get(i);
			Volume volume = nthStorageJob.getVolume();
			String volumeTag = volume.getId();
			
			String messageToBeSaved = null;
	    	Path destDiskpath = Paths.get(volume.getDetails().getMountpoint(), volume.getId());
	    	if(destDiskpath.toFile().exists()) {
	    		onlyDiskOnLibraryStorageJobsList.add(nthStorageJob);
	    	}
	    	else {
				messageToBeSaved = volumeTag + " not attached ";
				logger.debug(messageToBeSaved + " . Skipping job - " + nthStorageJob.getJob().getId()); 
	    	}	
			
			Job nthJob = nthStorageJob.getJob();
			String alreadyExistingJobMessage = nthJob.getMessage();
			if((messageToBeSaved == null && alreadyExistingJobMessage != null) || (messageToBeSaved != null && !messageToBeSaved.equals(alreadyExistingJobMessage))) {
				nthJob.setMessage(messageToBeSaved);
				Job latestJobObjFromDb = jobDao.findById(nthJob.getId()).get();
				if(latestJobObjFromDb.getStatus() != Status.cancelled) // if a job is cancelled in another thread - dont save the object as it overwrites the status
					jobDao.save(nthJob);
			}
		}
	
		return onlyDiskOnLibraryStorageJobsList;
	}
	
	public class DiskTask implements Runnable{
		
		private DiskJob diskJob;
		
		public DiskJob getDiskJob() {
			return diskJob;
		}
	
		public void setDiskJob(DiskJob diskJob) {
			this.diskJob = diskJob;
		}
	
		@Override
		public void run() {
			logger.info("Now taking up Disk job - " + diskJob.getStorageJob().getJob().getId());	
			manage(diskJob);
		}
	}
}
