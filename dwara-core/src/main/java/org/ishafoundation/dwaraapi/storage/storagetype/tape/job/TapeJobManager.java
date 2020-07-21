package org.ishafoundation.dwaraapi.storage.storagetype.tape.job;

import java.util.List;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TActivedeviceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.JobDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.DeviceStatus;
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
	private DeviceDao deviceDao;
	
	@Autowired
	private TActivedeviceDao tActivedeviceDao;
	
	@Autowired
	private TapeTaskThreadPoolExecutor tapeTaskThreadPoolExecutor;	
	
	@Autowired
	private TapeJobSelector tapeJobSelector;

	@Autowired
	private TapeDeviceUtil tapeDeviceUtil;
	
	@Override
    public void run() {
		logger.trace("Tape job manager kicked off");
		List<StorageJob> storageJobsList = getStorageJobList();
		// execute the job

		StorageJob storageJob = storageJobsList.get(0); // if there is a format/mapdrive job only one job will be in the list coming from JobManager... 
		Action storagetaskAction = storageJob.getJob().getStoragetaskActionId();
		
		List<DriveDetails> availableDrivesDetails = tapeDeviceUtil.getAllAvailableDrivesDetails();

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
			for (DriveDetails nthAvailableDriveDetails : availableDrivesDetails) {
				StorageJob selectedStorageJob = tapeJobSelector.selectJob(storageJobsList, nthAvailableDriveDetails);
				if(selectedStorageJob != null) {
					logger.debug("Job " + selectedStorageJob.getJob().getId() + " selected");
					
					prepareTapeJobAndContinueNextSteps(selectedStorageJob, nthAvailableDriveDetails, true);
				}else {
					logger.debug("No more jobs");
				}

			}
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
					
			boolean tapedriveAlreadyLoadedWithTape = false;
			
			logger.trace("Composing Tape job");
			TapeJob tapeJob = new TapeJob();
			tapeJob.setStorageJob(storageJob);
			tapeJob.settActivedevice(tActivedevice);
			tapeJob.setTapeLibraryName(tapeLibraryName);
			tapeJob.setTapedriveNo(tapedriveNo);
			tapeJob.setDeviceUid(tapedriveUid);
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
