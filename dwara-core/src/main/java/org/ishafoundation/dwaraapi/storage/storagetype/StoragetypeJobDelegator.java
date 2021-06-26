package org.ishafoundation.dwaraapi.storage.storagetype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepositoryUtil;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.utils.JobUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetask.ImportStoragetaskAction;
import org.ishafoundation.dwaraapi.storage.storagetype.thread.AbstractStoragetypeJobManager;
import org.ishafoundation.dwaraapi.storage.storagetype.thread.IStoragetypeThreadPoolExecutor;
import org.ishafoundation.dwaraapi.storage.utils.StorageJobUtil;
import org.ishafoundation.dwaraapi.thread.executor.ImportStoragetaskSingleThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


@Component
public class StoragetypeJobDelegator {
	
	private static final Logger logger = LoggerFactory.getLogger(StoragetypeJobDelegator.class);

	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private ApplicationContext applicationContext;
		
	@Autowired
	private ImportStoragetaskSingleThreadExecutor importStoragetaskSingleThreadExecutor;
	
	@Autowired
	private ArtifactVolumeRepositoryUtil artifactVolumeRepositoryUtil;

	@Autowired
	private JobUtil jobUtil;

	@Autowired
	private StorageJobUtil storageJobUtil;
	
	@Autowired
	private Map<String, AbstractStoragetypeJobManager> storageTypeJobManagerMap;
	
	@Autowired
	private  Map<String, IStoragetypeThreadPoolExecutor> storagetypeThreadPoolExecutorMap;
	
	/**
	 *  Wraps the jobs with extra storage info and with that extra context filters "already in-progress same volume jobs" and delegates to appropriate storagetype impl
	 * @param jobsList
	 */
	// TODO Need to move the map_drives logic to Tape specific impl...
	public void delegate(List<Job> jobsList) {
		List<StorageJob> storageJobList = new ArrayList<StorageJob>();
		// Need to block all storage jobs from picked up for processing, when there is a queued/inprogress mapdrive/format request... 
		List<Action> actionList = new ArrayList<Action>();
		actionList.add(Action.map_tapedrives);
		actionList.add(Action.initialize);

		// If a subrequest action type is mapdrive and status is queued or inprogress skip storage jobs...
		long blockingJobsLinedUp = jobDao.countByStoragetaskActionIdInAndStatus(actionList, Status.queued);
		long blockingJobsInFlight = jobDao.countByStoragetaskActionIdInAndStatus(actionList, Status.in_progress);
		
		Map<Volume, Job> volume_InProgressJob_Map = new HashMap<Volume, Job>();
		Map<Volume, ArtifactVolume> volume_LastArtifactOnVolume_Map = new HashMap<Volume, ArtifactVolume>();
		for (Job job : jobsList) {
			Action storagetaskAction = job.getStoragetaskActionId();
			
			if(storagetaskAction == Action.import_) {
				ImportStoragetaskAction importStoragetaskAction = applicationContext.getBean(ImportStoragetaskAction.class);
				importStoragetaskAction.setJob(job);
				importStoragetaskSingleThreadExecutor.getExecutor().execute(importStoragetaskAction);
			}
			
			StorageJob storageJob = storageJobUtil.wrapJobWithStorageInfo(job);
			if(storageJob == null)
				continue;

			Volume volume = storageJob.getVolume();
			if(volume == null) {
				String msg = "No volume available just yet for job "  + job.getId() + ". So skipping this job this schedule...";
				logger.debug(msg);
				job.setMessage("[info] No volume available");
				job = jobDao.save(job);
				continue;
			}
			
			if(volume.getStoragetype() == Storagetype.tape) {
				if(blockingJobsInFlight > 0) { // format/tape map drive request in progress, so blocking all storage jobs until the job is complete...
					logger.trace("Skipping adding to storagejob collection as already a blocking request is " + Status.in_progress.name());
					continue;
				}
				else if(blockingJobsLinedUp > 0) { // if any format/tape map drive request queued up
					// only adding one blocking job to the list
					if(job.getRequest().getActionId() == Action.initialize || job.getRequest().getActionId() == Action.map_tapedrives) {
						if(storageJobList.size() == 0) { // add only one job at a time. If already added skip adding to the list and continue loop(we still need to continue so non-storage jobs are managed)...
							storageJobList.add(storageJob);
							logger.trace("Added to storagejob collection");
						}
						else {
							logger.trace("Already another blocking job added to storagejob collection. So skipping this");
							continue;
						}
					}
				}
				else { // only add when no tapedrivemapping or format activity
					// all storage jobs need to be grouped for some optimisation...
					Job inProgressJobOnVolume = volume_InProgressJob_Map.get(volume); // Filtering already inprogress same volume jobs
					if(inProgressJobOnVolume == null) {
						inProgressJobOnVolume = jobDao.findByStoragetaskActionIdIsNotNullAndVolumeIdAndStatus(volume.getId(), Status.in_progress);
						
						if(inProgressJobOnVolume != null) {
							volume_InProgressJob_Map.put(volume, inProgressJobOnVolume);
						}
					}							

					if(inProgressJobOnVolume != null) {
						logger.info("Skipping "  + job.getId() + " as same volume is already in use " + inProgressJobOnVolume.getId());
						continue;
					}
					else {
						if(storagetaskAction == Action.write) {
							ArtifactVolume lastArtifactOnVolume = volume_LastArtifactOnVolume_Map.get(volume);
							if(lastArtifactOnVolume == null) {
								lastArtifactOnVolume = artifactVolumeRepositoryUtil.getLastArtifactOnVolume(storageJob.getDomain(), volume);
								
								if(lastArtifactOnVolume != null) {
									volume_LastArtifactOnVolume_Map.put(volume, lastArtifactOnVolume);
								}
							}
							
							if(lastArtifactOnVolume != null) { // check if the job has dependencies and ensure all nested dependencies are completed...
								//last write job on the volume needed by this job
								Job lastWriteJob = lastArtifactOnVolume.getJob();
								// if a write job failed and we are requeing it its possible that the lastArtifactOnVolume is the same job. if so skip this check 
								if(lastWriteJob.getId() != job.getId() && !jobUtil.isWriteJobAndItsDependentJobsComplete(lastWriteJob)) {
									String msg = "Skipping "  + job.getId() + " as previous write job [" + lastWriteJob.getId() + "] and/or its dependent jobs are yet to complete";
									logger.info(msg);
									job.setMessage(msg);
									jobDao.save(job);
									continue;
								}
							}
						}
					}
					storageJobList.add(storageJob);
					logger.trace("Added to storagejob collection");
				}
			}else {
				storageJobList.add(storageJob);
				logger.trace("Added to storagejob collection");
			}
		}
		
		if(storageJobList.size() > 0) {
			logger.debug(storageJobList.size() + " storage jobs are process ready");
			delegate_internal(storageJobList);
		}else {
			logger.trace("No storage job to be processed");
		}
	}

	/**
	 * Groups based on storagetype and then delegates it to appropriate storagetype specific jobs processor
	 */
	private void delegate_internal(List<StorageJob> storageJobList) {
		Map<Storagetype, List<StorageJob>> storagetype_storageTypeGroupedJobsList_Map = groupOnStorageType(storageJobList);
		Set<Storagetype> storagetypeSet = storagetype_storageTypeGroupedJobsList_Map.keySet();
		
		// delegate the task to appropriate storagetype specific jobs processor
		for (Storagetype storagetype : storagetypeSet) {
			IStoragetypeThreadPoolExecutor storagetypeThreadPoolExecutor = storagetypeThreadPoolExecutorMap.get(storagetype.name() + DwaraConstants.STORAGETYPE_THREADPOOLEXECUTOR_SUFFIX);
			
			ThreadPoolExecutor tpe = storagetypeThreadPoolExecutor.getExecutor();
			BlockingQueue<Runnable> runnableQueueList = tpe.getQueue();
			// Ideally the Storagetype specific JobManager just need to delegate the job to the processor thread. So by the time the next schedule gets here the queue should be empty. 
			// But just in case if it has items clear them and feed it with the fresh job list...
			runnableQueueList.clear();
			logger.debug("Cleared existing IStoragetypeThreadPoolExecutor queue item and added the fresh storage job list...");
			/*
			if(runnableQueueList.size() >= 3) {
				logger.debug("Already 3 storagejobset lined up in " + storagetype.name() + DwaraConstants.STORAGETYPE_THREADPOOLEXECUTOR_SUFFIX + ". Skipping this schedule");
			}else {
			 */
				
//			boolean alreadyQueued = false;
//			for (Runnable runnable : runnableQueueList) {
//				AbstractStoragetypeJobManager asjm = (AbstractStoragetypeJobManager) runnable;
//				
//				if(job.getId() == asjm.getJob().getId()) {
//					alreadyQueued = true;
//					break;
//				}
//			}
				logger.debug("Delegating to " + storagetype.name() + DwaraConstants.STORAGETYPE_JOBMANAGER_SUFFIX + "'s separate thread ================");
				AbstractStoragetypeJobManager storageTypeJobManager = storageTypeJobManagerMap.get(storagetype.name() + DwaraConstants.STORAGETYPE_JOBMANAGER_SUFFIX);
				storageTypeJobManager.setStorageJobList(storagetype_storageTypeGroupedJobsList_Map.get(storagetype));
				tpe.execute(storageTypeJobManager);
			/*}*/
		}
	}
	
	private Map<Storagetype, List<StorageJob>> groupOnStorageType(List<StorageJob> storageJobList){
		Map<Storagetype, List<StorageJob>> storagetype_storageTypeGroupedJobsList_Map = new HashMap<>();
		logger.debug("Wrapping jobs with more storage info and grouping them on Storagetype");
		for (StorageJob storageJob : storageJobList) {
			Volume volume = storageJob.getVolume();
			Storagetype storagetype = volume.getStoragetype();
			if(storagetype_storageTypeGroupedJobsList_Map.get(storagetype) != null) {
				List<StorageJob> jobList = storagetype_storageTypeGroupedJobsList_Map.get(storagetype);
				jobList.add(storageJob);
			}
			else {
				List<StorageJob> jobList = new ArrayList<StorageJob>();
				jobList.add(storageJob);
				storagetype_storageTypeGroupedJobsList_Map.put(storagetype, jobList);
			}

			
//			int volumeId = job.getActionelement().getVolumeId();
//			if(volumeId > 0) {
//				Volume volume = volumeDao.findById(volumeId).get();
//			}
		}
		return storagetype_storageTypeGroupedJobsList_Map;
	}
	
}