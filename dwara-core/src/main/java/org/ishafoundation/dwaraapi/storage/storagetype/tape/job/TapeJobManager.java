package org.ishafoundation.dwaraapi.storage.storagetype.tape.job;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TActivedeviceDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeDeviceUtil;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeOnLibrary;
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
	private DeviceDao deviceDao;
	
	@Autowired
	private TActivedeviceDao tActivedeviceDao;
	
	@Autowired
	private TapeTaskThreadPoolExecutor tapeTaskThreadPoolExecutor;	
	
	@Autowired
	private TapeJobSelector tapeJobSelector;

	@Autowired
	private TapeDeviceUtil tapeDeviceUtil;

	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@Autowired
	private JobDao jobDao;
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
		StorageJob firstStorageJob = storageJobsList.get(0); // if there is a format/mapdrive job only one job will be in the list coming from JobManager... 
		Action storagetaskAction = firstStorageJob.getJob().getStoragetaskActionId();
		
		if(storagetaskAction == Action.map_tapedrives || storagetaskAction == Action.initialize) {
			updateJobInProgress(firstStorageJob.getJob());
			logger.debug("Unloading all tapes from all drives");
			List<DriveDetails> preparedDrives = null;
			try {
				preparedDrives = tapeDeviceUtil.prepareAllTapeDrivesForBlockingJobs();
			} catch (Exception e1) {
				logger.error(e1.getMessage());
				updateJobFailed(firstStorageJob.getJob());
			}
			
			
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
						updateJobCompleted(firstStorageJob.getJob());
					}catch (Exception e) {
						logger.error(e.getMessage());
						updateJobFailed(firstStorageJob.getJob());
					}
				}
				else {
					logger.trace("Taking Option 2 Route");
					logger.trace("Composing Tape job");
					TapeJob tapeJob = new TapeJob();
					tapeJob.setStorageJob(firstStorageJob);
					manage(tapeJob);
				}
			}
			else if(storagetaskAction == Action.initialize) {
				DriveDetails driveDetails = preparedDrives.get(0);
				prepareTapeJobAndContinueNextSteps(firstStorageJob, driveDetails, false);
			}
		}
		else {

			List<DriveDetails> availableDrivesDetails = null;
			try {
				availableDrivesDetails = tapeDeviceUtil.getAllAvailableDrivesDetails();
			} catch (Exception e1) {
				logger.error("Unable to get Drives info. Skipping storage jobs...",e1.getMessage());
				//updateJobFailed(storageJob.getJob());
			}
			if(availableDrivesDetails.size() > 0) { // means drive(s) available
				logger.trace("No. of drives available "+ availableDrivesDetails.size());

				// Remove jobs that dont have tapes yet in the library
				removeJobsThatDontHaveNeededTapeOnLibrary(storageJobsList, availableDrivesDetails);
				
				if(storageJobsList.size() == 0) {
					logger.debug("No eligible tape jobs in queue.");
					return;
				}
				
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
					logger.debug("Now selecting job for drive - " + nthAvailableDriveDetails.getDriveId());//+ nthAvailableDriveDetails.getDriveName() + "(" + nthAvailableDriveDetails.getDte().getsNo() + ")");
					
					// STEP 2a
					StorageJob selectedStorageJob = null;
					try {
						// Just double ensuring that pickedup job still is queued and not taken up for processing in the earlier schedule...
						// If status of the selected job is queued we break the loop and move on to next steps
						// If the selected job' status is not queued, then remove it from the list and do the tapejobselection again for the drive...
						for (int i = 0; i < storageJobsList.size(); i++) {
							selectedStorageJob = tapeJobSelector.selectJob(storageJobsList, nthAvailableDriveDetails);
							if(selectedStorageJob != null) { // If any job is selected for the drive...
								Job selectedJob = jobDao.findById(selectedStorageJob.getJob().getId()).get();
								
								if(selectedJob.getStatus() == Status.queued) {
									logger.trace("Selected job is good for next steps");
									break; 
								} else {
									logger.trace("Selected job was potentially picked up by one of the previous scheduled executor' joblist, after the current list is picked up from DB");
									storageJobsList.remove(selectedStorageJob); // remove the already selected job from the list and do the tapejobselection again for the drive...
								}
							}
						}
						
					} catch (Exception e) {
						logger.error("Unable to select a job for drive - " + nthAvailableDriveDetails.getDriveId(), e);
						continue;
					}
					
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
			else {
				logger.info("All drives busy");
			}
		}
	}

	private void removeJobsThatDontHaveNeededTapeOnLibrary(List<StorageJob> storageJobsList,
			List<DriveDetails> availableDrivesDetails) {
		// For now assuming just one tape libarary is supported
		String tapeLibraryName = availableDrivesDetails.get(0).getTapelibraryName();
		List<TapeOnLibrary> tapeOnLibraryObjList = null;
		try {
			tapeOnLibraryObjList = tapeLibraryManager.getAllLoadedTapesInTheLibrary(tapeLibraryName);
		} catch (Exception e) {
			logger.error("Unable to get list of tapes on library " + tapeLibraryName + ". So not able to removeJobsThatDontHaveNeededTapeOnLibrary");
			return;
		}
		List<String> tapeOnLibraryList =  new ArrayList<String>();
		for (TapeOnLibrary tapeOnLibrary : tapeOnLibraryObjList) {
			tapeOnLibraryList.add(tapeOnLibrary.getVolumeTag());
		}
		
		List<StorageJob> onlyTapeOnLibraryStorageJobsList = new ArrayList<StorageJob>(); 
		for (int i = 0; i < storageJobsList.size(); i++) {
			StorageJob nthStorageJob = storageJobsList.get(i);
			String volumeTag = nthStorageJob.getVolume().getId();
			if(tapeOnLibraryList.contains(volumeTag)) {// Tape not on library - don't pick it up
				onlyTapeOnLibraryStorageJobsList.add(nthStorageJob);
			}
			else {
				logger.info(volumeTag + " not in library " + tapeLibraryName +" . Skipping job - " + nthStorageJob.getJob().getId()); 
			}
		}
	
		if(onlyTapeOnLibraryStorageJobsList.size() > 0) {
			storageJobsList = onlyTapeOnLibraryStorageJobsList;
		}
	}
	private void prepareTapeJobAndContinueNextSteps(StorageJob storageJob, DriveDetails driveDetails, boolean nextStepsInSeparateThread) {
		Job job = null;
		TapeJob tapeJob = null;
		TActivedevice tActivedevice = null;
		try {
			job = storageJob.getJob();
			updateJobInProgress(job);
			
			Volume volume = storageJob.getVolume();
			
			tActivedevice = new TActivedevice();// Challenge here...
			Device tapedriveDevice = deviceDao.findByWwnId(driveDetails.getDriveName());
			String tapedriveUid = tapedriveDevice.getWwnId();
			tActivedevice.setDevice(tapedriveDevice);

			logger.debug("Flagging drive " + tapedriveUid + " as busy, by adding tActivedevice entry" );
			tActivedevice.setJob(job);
			Action storagetaskAction = job.getStoragetaskActionId();
			if(storagetaskAction != Action.initialize) // For format the volume is still not in the DB just yet. Not having this condition will cause FK failure while saving device...  
				tActivedevice.setVolume(volume);

			tActivedevice = tActivedeviceDao.save(tActivedevice);
			
			int tapedriveNo = tapedriveDevice.getDetails().getAutoloaderAddress(); // data transfer element/drive no
			String tapeLibraryName = driveDetails.getTapelibraryName();
			

			logger.trace("Composing Tape job");
			tapeJob = new TapeJob();
			tapeJob.setStorageJob(storageJob);
			tapeJob.settActivedevice(tActivedevice);
			tapeJob.setTapeLibraryName(tapeLibraryName);
			tapeJob.setTapedriveNo(tapedriveNo);
			tapeJob.setDeviceWwnId(tapedriveUid);
			if(storagetaskAction == Action.write) {
				Domain domain = storageJob.getDomain();
			    ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
			    int artifactVolumeCount = domainSpecificArtifactVolumeRepository.countByIdVolumeId(volume.getId());
			    tapeJob.setArtifactVolumeCount(artifactVolumeCount);
			}
//			tapeJob.setTapedriveAlreadyLoadedWithNeededTape(tapedriveAlreadyLoadedWithTape);

			job.setDevice(tapedriveDevice);
			if(storagetaskAction != Action.initialize) // For format the volume is still not in the DB just yet. Not having this condition will cause FK failure while saving... 
				job.setVolume(volume);
			
			if(nextStepsInSeparateThread) {
				logger.debug("Launching separate tape task thread -----------");
				TapeTask tapeTask = new TapeTask();//applicationContext.getBean(TapeTask.class); 
				tapeTask.setTapeJob(tapeJob);
				tapeTaskThreadPoolExecutor.getExecutor().execute(tapeTask);
			}
			else {
				logger.debug("Continuing in same thread");
				StorageResponse storageResponse = manage(tapeJob);
				
				logger.debug("Deleting the t_activedevice record for " + tapeJob.getDeviceWwnId());
				tActivedeviceDao.delete(tActivedevice);
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			updateJobFailed(job);

			logger.debug("Deleting the t_activedevice record for " + tapeJob.getDeviceWwnId());
			tActivedeviceDao.delete(tActivedevice);
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
			manage(tapeJob);
			
			TActivedevice tActivedevice = tapeJob.gettActivedevice();
			logger.debug("Deleting the t_activedevice record for " + tapeJob.getDeviceWwnId());
			tActivedeviceDao.delete(tActivedevice);


		}
	}
}
