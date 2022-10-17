package org.ishafoundation.dwaraapi.storage.storagetype.tape.job;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TActivedeviceDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.JobRunDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.utils.JobUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.VolumeHealthStatus;
import org.ishafoundation.dwaraapi.exception.StorageException;
import org.ishafoundation.dwaraapi.service.JobServiceRequeueHelper;
import org.ishafoundation.dwaraapi.service.UserRequestHelper;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeDeviceUtil;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeException;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeOnLibrary;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.executor.TapeTaskThreadPoolExecutor;
import org.ishafoundation.dwaraapi.storage.storagetype.thread.AbstractStoragetypeJobManager;
import org.ishafoundation.dwaraapi.storage.utils.StorageJobUtil;
import org.ishafoundation.dwaraapi.utils.VolumeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("tape"+DwaraConstants.STORAGETYPE_JOBMANAGER_SUFFIX)
@Scope("prototype")
public class TapeJobManager extends AbstractStoragetypeJobManager {


    private static final Logger logger = LoggerFactory.getLogger(TapeJobManager.class);

	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private TActivedeviceDao tActivedeviceDao;

	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private JobRunDao jobRunDao;
	
	@Autowired
	private JobServiceRequeueHelper jobServiceRequeueHelper;
	
	@Autowired
	private UserRequestHelper userRequestHelper;
	
	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private TapeTaskThreadPoolExecutor tapeTaskThreadPoolExecutor;	
	
	@Autowired
	private TapeJobSelector tapeJobSelector;

	@Autowired
	private TapeDeviceUtil tapeDeviceUtil;
	
	@Autowired
	private JobUtil	jobUtil;
	
	@Autowired
	private StorageJobUtil storageJobUtil;
	
	@Autowired
	private VolumeUtil volumeUtil;
		
	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@Autowired
	private Configuration configuration;
	
	@Value("${scheduler.jobManager.fixedDelay}")
	private String fixedDelayAsString;


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
		logger.info("Tape job manager kicked off");
		List<StorageJob> storageJobsList = getStorageJobList();

		// execute the job
		StorageJob firstStorageJob = storageJobsList.get(0); // if there is a format/mapdrive job only one job will be in the list coming from JobManager... 
		Action storagetaskAction = firstStorageJob.getJob().getStoragetaskActionId();
		
		if(storagetaskAction == Action.map_tapedrives || storagetaskAction == Action.initialize) {
			Job job = firstStorageJob.getJob();
			job = jobDao.findById(job.getId()).get();
			if(job.getStatus() != Status.queued) { // This check is to avoid race condition on jobs. This check is not needed for non-blocking jobs as Jobselector will take care of it...
				logger.info(job.getId() + " - job probably already picked up for processing in the last run. Skipping it");
			}
			else {
				if(storagetaskAction == Action.map_tapedrives) {
					logger.info("Will be mapping drives");
					try {
						boolean areJobsRunning = true;
						while(areJobsRunning) {
							List<TActivedevice> tActivedevices = (List<TActivedevice>) tActivedeviceDao.findAll();
							if(tActivedevices.size() > 0) {
								logger.info("Waiting on running jobs to complete");
								try {
									Thread.sleep(10000); // 10 secs
								} catch (InterruptedException e) {
									// swallow it
								} 
							}
							else
								areJobsRunning = false;
						}
			
						logger.info("No running jobs as per dwara TActivedevice table. But double checking with physical library to see if any drive is still busy");
						List<DriveDetails> driveDetailsList = null;

						boolean areJobsStillRunning = true;
						while(areJobsStillRunning) {
							driveDetailsList = tapeDeviceUtil.getAllDrivesDetails();
							if(driveDetailsList.size() == 0)
								throw new Exception("No drives configured/online in dwara. Nothing to map. Please check devices table");
							boolean anyDriveStillBusy = false;
							for (DriveDetails driveDetails : driveDetailsList) {
								if(driveDetails.getMtStatus().isBusy())
									anyDriveStillBusy = true;
							}

							if(anyDriveStillBusy) {
								logger.error("Something wrong. TActivedevice showed no job running. Drives should all be free. Needs attention. Nevertheless waiting on running jobs to complete");
								try {
									Thread.sleep(10000); // 10 secs
								} catch (InterruptedException e) {
									// swallow it
								} 
							}
							else
								areJobsStillRunning = false;
						}
					
						logger.info("No running jobs. Proceeding mapping drives");
						
						checkAndUpdateStatusesToInProgress(firstStorageJob.getJob());
						logger.trace("Composing Tape job");
						TapeJob tapeJob = new TapeJob();
						tapeJob.setStorageJob(firstStorageJob);
						tapeJob.setDriveDetails(driveDetailsList);
						manage(tapeJob);
					} catch (Exception e1) {
						logger.error(e1.getMessage());
						updateJobFailed(firstStorageJob.getJob());
					}
				} 
				else if(storagetaskAction == Action.initialize) {
					logger.info("Will be initializing " + job.getRequest().getDetails().getVolumeId());
					checkAndUpdateStatusesToInProgress(job);
					logger.info("Now preparing all tape drives for initialization");
					List<DriveDetails> preparedDrives = null;
					try {

						preparedDrives = tapeDeviceUtil.prepareAllTapeDrivesForBlockingJobs();
						
						// When we initialize the first time - mapping wouldnt have happened as a regd tape is a criteria for mapping drive - so we dont the device getting used and so can update Tactivedevice table
						logger.trace("Composing Tape job");
						TapeJob tapeJob = new TapeJob();
						tapeJob.setStorageJob(firstStorageJob);
//						tapeJob.setTapeLibraryName(selectedDriveDetails.getTapelibraryName());
//						tapeJob.setTapedriveNo(selectedDriveDetails.getDriveId());
						tapeJob.setDriveDetails(preparedDrives);
						manage(tapeJob);
					} catch (Exception e1) {
						logger.error(e1.getMessage(), e1);
						updateJobFailed(firstStorageJob.getJob());
					}
				}
			}
		}
		else {
			List<DriveDetails> availableDrivesDetails = null;
			try {
				logger.info("Getting all available drives");
				availableDrivesDetails = tapeDeviceUtil.getAllAvailableDrivesDetails();
			} catch (Exception e1) {
				logger.error("Unable to get Drives info. Skipping storage jobs...",e1.getMessage());
				return;
				//updateJobFailed(storageJob.getJob());
			}
			if(availableDrivesDetails.size() > 0) { // means drive(s) available
				logger.trace("No. of drives available "+ availableDrivesDetails.size());

				// Remove jobs that dont have tapes yet in the library
				storageJobsList = removeJobsThatDontHaveNeededTapeOnLibrary(storageJobsList, availableDrivesDetails);
				
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
					logger.info("Now selecting job for drive - " + nthAvailableDriveDetails.getDriveId());//+ nthAvailableDriveDetails.getDriveName() + "(" + nthAvailableDriveDetails.getDte().getsNo() + ")");
					try {
						StorageJob selectedStorageJob = null;
						String volumeTag = nthAvailableDriveDetails.getDte().getVolumeTag();
						
						// if an available drive is already loaded with tape - we need to see if we have to hold the tape or not...
						if(StringUtils.isNotBlank(volumeTag)) {
							Volume volume = volumeDao.findById(volumeTag).get();
							
							if(!volume.isImported()) {
								// ensure that last job on tape has not completed and spawned a dependent storage job "after" the scheduler prepared the storageJobs list.
								// TODO better take it from the artifact_volume table... 
								Job lastJobOnTape = jobDao.findTopByStoragetaskActionIdIsNotNullAndVolumeIdAndStatusAndCompletedAtIsNotNullOrderByCompletedAtDesc(volumeTag, Status.completed);
								
								List<Job> dependentJobList = jobUtil.getDependentJobs(lastJobOnTape);
								Job dependentJob = null;
								for (Job nthDependentJob : dependentJobList) {
									if(nthDependentJob.getStoragetaskActionId() != null) {
										dependentJob = nthDependentJob;
										break;
									}
								}
								
								if(lastJobOnTape.getStoragetaskActionId() == Action.write) {
									boolean isDependentJobInTheStorageList = false;
									for (StorageJob nthStorageJob : storageJobsList) {
										if(nthStorageJob.getJob().getId() == dependentJob.getId()) {
											isDependentJobInTheStorageList = true;
											break;
										}
									}
									// check if the last completed tape job' dependency is in the storageJobsList - 
									// when a write job completes and creates its dependent restore job after the storageJobsList is prepared by the scheduled JobManager 
									// the restore job will not be in the storageJobsList - ensure we pick it up here 
									if(dependentJob != null && !isDependentJobInTheStorageList) {
										int delayInSecs = (int) (Long.parseLong(fixedDelayAsString)/1000);
										
										long elapsedSecsSinceLastTapeOnJob = ChronoUnit.SECONDS.between(LocalDateTime.now(), lastJobOnTape.getCompletedAt());
	
										if(elapsedSecsSinceLastTapeOnJob < (delayInSecs + 60) && dependentJob.getStatus() == Status.queued) { // Also ensure the dependency job's status is queued - even a failed dependency job eg. restore will reach this far and cause a loop
											selectedStorageJob = storageJobUtil.wrapJobWithStorageInfo(dependentJob);
											logger.info("Last job on tape " + lastJobOnTape.getId() + " just got completed and its dependent job is not in the list for job selection. So had to wrap it here");
										}
									}
								}
								else { // if restore
									VolumeHealthStatus volumeStatus = volume.getHealthstatus();
									if(volumeStatus == VolumeHealthStatus.suspect ||  volumeStatus == VolumeHealthStatus.defective || volume.isFinalized()) {
										logger.info("Will be potentially yielding the tape " + volumeTag + " as it is flagged suspect/defective/finalized");
									}
									else {
										String driveLoadedTape_GroupVolumeId = volume.getGroupRef().getId();
										
										// check if any job on same volumegroup id is queued
										boolean isQueuedJobOnGroupVolume = volumeUtil.isQueuedJobOnGroupVolume(driveLoadedTape_GroupVolumeId);
										
										
										boolean isSameGroupVolumeJobInTheStorageList = false;
										for (StorageJob nthStorageJob : storageJobsList) { // check if any job on same group volume is there in the list
											String toBeUsedGroupVolumeId = nthStorageJob.getVolume().getGroupRef().getId();
											if(toBeUsedGroupVolumeId.equals(driveLoadedTape_GroupVolumeId)) {
												isSameGroupVolumeJobInTheStorageList = true;
												break;
											}
										}
										
										int delayInSecs = (int) (Long.parseLong(fixedDelayAsString)/1000);
										
										long elapsedSecsSinceLastTapeOnJob = ChronoUnit.SECONDS.between(LocalDateTime.now(), lastJobOnTape.getCompletedAt());
										if(!isSameGroupVolumeJobInTheStorageList && isQueuedJobOnGroupVolume &&  elapsedSecsSinceLastTapeOnJob < (delayInSecs + 60)) {// if no same groupVolume job in the list, but samegroupvolume jobs are queued then select one or skip this cycle
											logger.info("No job selected. Last job on tape " + lastJobOnTape.getId() + " just got completed and same volume job is queued but not in the list for job selection. So skipping this drive and tape this cycle");
											continue;
										}
									}
								}
							}
						}
						
						// STEP 2a
						if(selectedStorageJob == null) {
							try {
								// Just double ensuring that pickedup job still is queued and not taken up for processing in the earlier schedule...
								// If status of the selected job is queued we break the loop and move on to next steps
								// If the selected job' status is not queued, then remove it from the list 
								
								for (int i = 0; i < storageJobsList.size(); i++) {
									selectedStorageJob = tapeJobSelector.selectJob(storageJobsList, nthAvailableDriveDetails);
									if(selectedStorageJob != null) { // If any job is selected for the drive...
										Job selectedJob = jobDao.findById(selectedStorageJob.getJob().getId()).get();
										
										if(selectedJob.getStatus() == Status.queued) {
											logger.debug("Job " + selectedJob.getId() + " is good for next steps");
											break; 
										} else {
											logger.debug("Job " + selectedJob.getId() + " was potentially picked up by one of the previous scheduled executor' joblist, after the current list is picked up from DB");
											storageJobsList.remove(selectedStorageJob); // remove the already selected job from the list and do the tapejobselection again for the drive...
										}
									} else {
										break; // Just to ensure that the tapejobselection is not looped again for the drive...
									}
								}
								
							} catch (Exception e) {
								logger.error("Unable to select a job for drive - " + nthAvailableDriveDetails.getDriveId(), e);
								continue;
							}
						}
						// STEP 2b
						if(selectedStorageJob == null) {
							logger.info("No tape jobs in queue are eligible to be processed for the drive");
							//break;
						}
						else if(selectedStorageJob != null) {
							logger.info("Job " + selectedStorageJob.getJob().getId() + " selected");
							
							prepareTapeJobAndContinueNextSteps(selectedStorageJob, nthAvailableDriveDetails, true);
							
							// STEP 3 filter the job from the next iteration
							storageJobsList.remove(selectedStorageJob);
						}
		
						if(storageJobsList.size() <= 0) {
							logger.debug("No tape jobs in queue anymore. So skipping the loop");
							break;
						}
					}
					catch (Exception e) {
						logger.error("Unable to select a job for drive - " + nthAvailableDriveDetails.getDriveId(), e);
					}
				}
			}
			else {
				logger.info("All drives busy");
			}
		}
	}

	private List<StorageJob>  removeJobsThatDontHaveNeededTapeOnLibrary(List<StorageJob> storageJobsList,
			List<DriveDetails> availableDrivesDetails) {
		// TODO For now assuming just one tape libarary is supported
		String tapeLibraryName = availableDrivesDetails.get(0).getTapelibraryName();
		List<TapeOnLibrary> tapeOnLibraryObjList = null;
		try {
			tapeOnLibraryObjList = tapeLibraryManager.getAllLoadedTapesInTheLibrary(tapeLibraryName);
		} catch (Exception e) {
			logger.error("Unable to get list of tapes on library " + tapeLibraryName + ". So not able to removeJobsThatDontHaveNeededTapeOnLibrary");
			return storageJobsList;
		}
		List<String> tapeOnLibraryList =  new ArrayList<String>();
		for (TapeOnLibrary tapeOnLibrary : tapeOnLibraryObjList) {
			tapeOnLibraryList.add(tapeOnLibrary.getVolumeTag());
		}
		
		List<StorageJob> onlyTapeOnLibraryStorageJobsList = new ArrayList<StorageJob>(); 
		for (int i = 0; i < storageJobsList.size(); i++) {
			StorageJob nthStorageJob = storageJobsList.get(i);
			String volumeTag = nthStorageJob.getVolume().getId();
			String messageToBeSaved = null;
			if(tapeOnLibraryList.contains(volumeTag)) {// Only Tapes on library - else don't pick the job - let it be queued
				onlyTapeOnLibraryStorageJobsList.add(nthStorageJob);
			}
			else {
				messageToBeSaved = volumeTag + " not inside the library ";
				logger.debug(messageToBeSaved + tapeLibraryName +" . Skipping job - " + nthStorageJob.getJob().getId()); 
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
	
		return onlyTapeOnLibraryStorageJobsList;
	}
	
	private void prepareTapeJobAndContinueNextSteps(StorageJob storageJob, DriveDetails driveDetails, boolean nextStepsInSeparateThread) {
		Job job = null;
		TapeJob tapeJob = null;
		TActivedevice tActivedevice = null;
		try {
			Volume volume = storageJob.getVolume();
			job = storageJob.getJob();
			Action storagetaskAction = job.getStoragetaskActionId();
			if(storagetaskAction != Action.initialize) // For format the volume is still not in the DB just yet. Not having this condition will cause FK failure while saving... 
				job.setVolume(volume);
			checkAndUpdateStatusesToInProgress(job);
			
			
			tActivedevice = new TActivedevice();// Challenge here...
			Device tapedriveDevice = deviceDao.findByWwnId(driveDetails.getDriveName());
			String tapedriveUid = tapedriveDevice.getWwnId();
			tActivedevice.setDevice(tapedriveDevice);

			logger.debug("Flagging drive " + tapedriveUid + " as busy, by adding tActivedevice entry" );
			tActivedevice.setJob(job);
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
//			if(storagetaskAction == Action.write) {
//				Domain domain = storageJob.getDomain();
//			    ArtifactVolumeRepository<ArtifactVolume> artifactVolumeDao = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
//			    int artifactVolumeCount = artifactVolumeDao.countByIdVolumeId(volume.getId());
//			    tapeJob.setArtifactVolumeCount(artifactVolumeCount);
//			}


			job.setDevice(tapedriveDevice);
			jobDao.save(job);//saving the device details - Dont be tempted to update this info. We want to ensure we set activedevice table and then update this. Worth the db call again.			
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
			
//			if(e instanceof StorageException || e instanceof TapeException) { // we need to requeue and flag suspicion only for storage exception and not because of post processing like moving a file from location a to b etc.,
				if(job.getStoragetaskActionId() == Action.write || job.getStoragetaskActionId() == Action.restore) { // Any write or restore failure should have the tape marked as suspect...
					long jobAlreadyRequeuedCount = jobRunDao.countByJobId(job.getId());
					if(jobAlreadyRequeuedCount < configuration.getAllowedAutoRequeueAttemptsOnFailedStorageJobs()) {
						try {
							logger.info("Requeuing job " + job.getId() + ". Attempt " + jobAlreadyRequeuedCount + 1);
							jobServiceRequeueHelper.requeueJob(job.getId(),DwaraConstants.SYSTEM_USER_NAME);
						} catch (Exception e1) {
							logger.error("Unable to auto requeue failed job..." + job.getId(), e);
						}
					} else {
						if(job.getStoragetaskActionId() == Action.write || job.getStoragetaskActionId() == Action.restore) { // TODO: Should we Mark a tape suspect on restore failures too or only for write?
							Volume volume = storageJob.getVolume();
							volume.setHealthstatus(VolumeHealthStatus.suspect);
							volumeDao.save(volume);
							logger.info("Marked the volume " + volume.getId() + " as suspect");
							
							// create user request for tracking
							HashMap<String, Object> data = new HashMap<String, Object>();
							data.put("volumeId", volume.getId());
							data.put("status", VolumeHealthStatus.suspect);
							String reason = "Repeated failure on job " + job.getId();
							data.put("reason", reason);
							userRequestHelper.createUserRequest(Action.mark_volume, DwaraConstants.SYSTEM_USER_NAME, Status.completed, data, reason);
							
							// requeuing the job in question after marking the tape bad, so that it gets picked up with the new tape...
							try {
								logger.info("Requeuing job " + job.getId() + " after marking the current tape as suspect. Attempt " + jobAlreadyRequeuedCount + 1);
								jobServiceRequeueHelper.requeueJob(job.getId(),DwaraConstants.SYSTEM_USER_NAME);
							} catch (Exception e1) {
								logger.error("Unable to auto requeue failed job..." + job.getId(), e);
							}
						}
					}
				}
//			}
			
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
