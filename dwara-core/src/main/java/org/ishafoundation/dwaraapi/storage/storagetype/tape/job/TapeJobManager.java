package org.ishafoundation.dwaraapi.storage.storagetype.tape.job;

import java.util.List;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.transactional.TActivedeviceDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.JobDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.DeviceStatus;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeDeviceUtil;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.executor.TapeTaskThreadPoolExecutor;
import org.ishafoundation.dwaraapi.storage.storagetype.thread.AbstractStoragetypeJobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("tape"+DwaraConstants.STORAGETYPE_JOBMANAGER_SUFFIX)
@Scope("prototype")
public class TapeJobManager extends AbstractStoragetypeJobManager {


    private static final Logger logger = LoggerFactory.getLogger(TapeJobManager.class);
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private TActivedeviceDao tActivedeviceDao;
	
	@Autowired
	private TapeTaskThreadPoolExecutor tapeTaskThreadPoolExecutor;	
	
	@Autowired
	private TapeJobSelector tapeJobSelector;

	@Autowired
	private TapeDeviceUtil tapeDeviceUtil;
	
	/**
	 * 
  		1) Get Available(Non busy) Drive list - We need to Dequeue as many jobs and Spawn as many threads For eg., If 2 free drives are available then we need to allocate 2 jobs to these drives on their own threads
  		2) Iterate the drive list
			a) choose a job(check for nonoptimisationjobs in the joblist and work on that job list first)
			b) launch a thread to process the job on a drive
			c) remove the job from the list so that the next iteration will not consider it...
	 * @param tapeStorageJobsList
	 */
	@Override
    public void run() {
		logger.trace("Tape job manager kicked off");
		List<StorageJob> storageJobsList = getStorageJobList();
		// execute the job
		
		List<DriveDetails> availableDrivesDetails = tapeDeviceUtil.getAllAvailableDrivesDetails();
		if(availableDrivesDetails.size() > 0) { // means drive(s) available
			logger.trace("No. of drives available "+ availableDrivesDetails.size());
			
			StorageJob storageJob = storageJobsList.get(0); // if there is a format/mapdrive job only one job will be in the list coming from JobManager... 
			Action storagetaskAction = storageJob.getJob().getStoragetaskActionId();
			
			if(storagetaskAction == Action.map_tapedrives || storagetaskAction == Action.format) {
				updateJobInProgress(storageJob.getJob());
				logger.debug("Unloading all tapes from all drives by tapeDrivePreparer");
				//tapeDrivePreparer
				
				if(storagetaskAction == Action.map_tapedrives) {
					logger.debug("Option 1 - call TapeDriveMapper straight from here and update statuses on your own");
					logger.debug("OR");
					logger.debug("Option 2 - Be consistent and continue to processor and let it do the mapping and status updates... Just Note that mapping is specific to Tape");
					boolean isOption1 = false;			
					if(isOption1) {
						try {
	
							logger.trace("Taking Option 1 Route");
							logger.trace("Mapping drives using TapeDriveMapper");
							//TapeDriveMapper
							updateJobCompleted(storageJob.getJob());
						}catch (Exception e) {
							logger.error(e.getMessage());
							updateJobFailed(storageJob.getJob());
						}
					}
					else {
						logger.trace("Taking Option 2 Route");
						logger.trace("Composing Tape job");
						TapeJob tapeJob = new TapeJob();
						tapeJob.setStorageJob(storageJob);
						manage(tapeJob);
					}
				}
				else if(storagetaskAction == Action.format) {
					DriveDetails driveDetails = availableDrivesDetails.get(0);
					prepareTapeJobAndContinueNextSteps(storageJob, driveDetails, false);
				}
			}
			else {
				// TODO - To load balance across drives based on their usage. The usage parameters is not retrieved...
	//			Map<Integer, DriveStatusDetails> usage_driveStatusDetails = new TreeMap<Integer, DriveStatusDetails>(); 
	//			for (Iterator<DriveStatusDetails> driveStatusDetailsIterator = availableDrivesList.iterator(); driveStatusDetailsIterator.hasNext();) {
	//				DriveStatusDetails driveStatusDetails = (DriveStatusDetails) driveStatusDetailsIterator.next();
	//				usage_driveStatusDetails.put(driveStatusDetails.getTotalUsageInHours(), driveStatusDetails); // TODO Need to decide based on what parameter the load has to be balanced...
	//			}
	//			Set<Integer> treeSet = new TreeSet<Integer>();
	//			treeSet.addAll(usage_driveStatusDetails.keySet());
	//			for (Integer usageHours : treeSet) {
	//				DriveStatusDetails driveStatusDetails = usage_driveStatusDetails.get(usageHours);
	//				// code goes here
	//			}
				// STEP 1
				for (DriveDetails nthAvailableDriveDetails : availableDrivesDetails) {
					logger.debug("Now selecting job for drive - " + nthAvailableDriveDetails.getDriveName());
					
					// STEP 2a
					StorageJob selectedStorageJob = tapeJobSelector.selectJob(storageJobsList, nthAvailableDriveDetails);
					
					// STEP 2b
					if(selectedStorageJob == null) {
						logger.debug("No tape jobs in queue are eligible to be processed for the drive");
						//break;
					}
					else if(selectedStorageJob != null) {
						logger.debug("Job " + selectedStorageJob.getJob().getId() + " selected");
						
						prepareTapeJobAndContinueNextSteps(selectedStorageJob, nthAvailableDriveDetails, true);
						
						// STEP 3 filter the job from the next iteration
						storageJobsList.remove(selectedStorageJob);
					}
	
					if(storageJobsList.size() <= 0) {
						logger.debug("No tape jobs in queue anymore. So skipping the loop");
						break;
					}
	
				}
			}
		}
		else {
			logger.info("All drives busy");
		}
	}
	
	private void prepareTapeJobAndContinueNextSteps(StorageJob storageJob, DriveDetails driveDetails, boolean nextStepsInSeparateThread) {
		Job job = null;
		try {
			job = storageJob.getJob();
			updateJobInProgress(job);
			
			Volume volume = storageJob.getVolume();
			TActivedevice tActivedevice = tActivedeviceDao.findByDeviceUid(driveDetails.getDriveName());

			Device tapedriveDevice = tActivedevice.getDevice();
			String tapedriveUid = tapedriveDevice.getUid();
			DeviceStatus deviceStatus = DeviceStatus.BUSY;
			logger.debug("Marking drive " + tapedriveUid + " - " +  deviceStatus);
			tActivedevice.setJob(job);
			Action storagetaskAction = job.getStoragetaskActionId();
			if(storagetaskAction != Action.format) // For format the volume is still not in the DB just yet. Not having this condition will cause FK failure while saving device...  
				tActivedevice.setVolume(volume);
			tActivedevice.setDeviceStatus(deviceStatus);
			tActivedevice = tActivedeviceDao.save(tActivedevice);
			
			int tapedriveNo = tapedriveDevice.getDetails().getAutoloader_address(); // data transfer element/drive no
//			logger.trace("Getting associated tape library");
//			Device tapelibraryDevice = deviceDao.findById(tapedriveDevice.getDetails().getAutoloader_id()).get();
//			String tapeLibraryName = tapelibraryDevice.getUid();
			String tapeLibraryName = driveDetails.getTapelibraryName();
			
			Domain domain = storageJob.getDomain();
		    ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
		    int artifactVolumeCount = domainSpecificArtifactVolumeRepository.countByIdVolumeId(volume.getId());
			
			logger.trace("Composing Tape job");
			TapeJob tapeJob = new TapeJob();
			tapeJob.setStorageJob(storageJob);
			tapeJob.settActivedevice(tActivedevice);
			tapeJob.setTapeLibraryName(tapeLibraryName);
			tapeJob.setTapedriveNo(tapedriveNo);
			tapeJob.setDeviceUid(tapedriveUid);
			tapeJob.setArtifactVolumeCount(artifactVolumeCount);
//			tapeJob.setTapedriveAlreadyLoadedWithNeededTape(tapedriveAlreadyLoadedWithTape);
			
			JobDetails jobDetails = new JobDetails();
			jobDetails.setDevice_id(tapedriveDevice.getId());
			jobDetails.setVolume_id(volume.getId());
			job.setDetails(jobDetails);
			
			if(nextStepsInSeparateThread) {
				logger.debug("Launching separate tape task thread -----------");
				TapeTask tapeTask = new TapeTask();//applicationContext.getBean(TapeTask.class); 
				tapeTask.setTapeJob(tapeJob);
				tapeTaskThreadPoolExecutor.getExecutor().execute(tapeTask);
			}
			else {
				logger.debug("Continuing in same thread");
				StorageResponse storageResponse = manage(tapeJob);
				
				deviceStatus = DeviceStatus.AVAILABLE;
				logger.debug("Marking back drive " + tapeJob.getDeviceUid() + " - " + deviceStatus);
				tActivedevice.setJob(null);
				tActivedevice.setVolume(null);
				tActivedevice.setDeviceStatus(deviceStatus);
				tActivedevice = tActivedeviceDao.save(tActivedevice);

			}
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			updateJobFailed(job);
		}
	}
	
	public class TapeTask implements Runnable{
	
		private TapeJob tapeJob;
		
		public TapeJob getTapeJob() {
			return tapeJob;
		}
	
		public void setTapeJob(TapeJob tapeJob) {
			this.tapeJob = tapeJob;
		}
	
		@Override
		public void run() {
			logger.info("Now taking up Tape job - " + tapeJob.getStorageJob().getJob().getId());	
			StorageResponse storageResponse = manage(tapeJob);
			
			TActivedevice tActivedevice = tapeJob.gettActivedevice();
			DeviceStatus deviceStatus = DeviceStatus.AVAILABLE;
			logger.debug("Marking back drive " + tapeJob.getDeviceUid() + " - " + deviceStatus);
			tActivedevice.setJob(null);
			tActivedevice.setVolume(null);
			tActivedevice.setDeviceStatus(deviceStatus);
			tActivedevice = tActivedeviceDao.save(tActivedevice);

		}
	}
}
