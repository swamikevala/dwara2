package org.ishafoundation.dwaraapi.storage.storagetype.thread;

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
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.executor.TapeTaskThreadPoolExecutor;
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
	
	@Override
    public void run() {
		logger.trace("Tape job manager kicked off");
		List<StorageJob> storageJobsList = getStorageJobList();
		// execute the job

		StorageJob storageJob = storageJobsList.get(0); // if there is a format/mapdrive job only one job will be in the list coming from JobManager... 
		Action storagetaskAction = storageJob.getJob().getStoragetaskActionId();
//		Action action = storageJob.getJob().getRequest().getAction();
//		// are we ok?
//		if(action == Action.format || action == Action.map_tapedrives || action == Action.finalize) {
//			logger.debug("unloading all tapes from all drives");
//			
//		}
//		else if(action == Action.ingest || action == Action.restore) {
//			
//		}
		// TODO : need to call tapelibrary to get the available drives also
		List<TActivedevice> activeDeviceList = tActivedeviceDao.findAllByDeviceDevicetypeAndDeviceStatus(Devicetype.tape_drive, DeviceStatus.AVAILABLE);

		if(storagetaskAction == Action.map_tapedrives || storagetaskAction == Action.format) {
			updateJobInProgress(storageJob.getJob());
			logger.debug("Unloading all tapes from all drives by tapeDrivePreparer");
			//tapeDrivePreparer
			
			if(storagetaskAction == Action.map_tapedrives) {

				try {
					logger.debug("Mapping drives using TapeDriveMapper");
					//TapeDriveMapper
					updateJobCompleted(storageJob.getJob());
				}catch (Exception e) {
					logger.error(e.getMessage());
					updateJobFailed(storageJob.getJob());
				}
			}
			else if(storagetaskAction == Action.format) {
				TActivedevice tActivedevice = activeDeviceList.get(0);
				prepareTapeJobAndContinueNextSteps(storageJob, tActivedevice, false);
			}
		}
		else {
			logger.trace("Iterating through available drives");
			for (TActivedevice tActivedevice : activeDeviceList) {
				
				Device tapedriveDevice = tActivedevice.getDevice();
				logger.trace("Selecting Job for drive - " + tapedriveDevice.getUid());
				// select a job for the available drive
				// if drive has a tape loaded that matches the joblist
				// TODO : Tape job selection happens here
				StorageJob selectedStorageJob = selectJob(storageJobsList, tapedriveDevice);
				logger.debug("Job " + selectedStorageJob.getJob().getId() + " selected");
				
				prepareTapeJobAndContinueNextSteps(selectedStorageJob, tActivedevice, true);
 
				// based on available tapedrives
				

			}
		}
//		
//		tActivedeviceDao.findAllByDeviceDevicetypeAndDeviceStatus(Devicetype.tape_drive, DeviceStatus.AVAILABLE);
//		List<Device> de = deviceDao.findAllByDevicetype();
		
//
//		
//		for (Iterator<Job> iterator = getStrorJobList().iterator(); iterator.hasNext();) {
//			Job job = (Job) iterator.next();
//			
//			int volumegroupId = job.getActionelement().getVolumeId();
//			List<Volume> volumesList = volumeDao.findAllByVolumeRefId(volumegroupId);
//			for (Volume volume : volumesList) {
//				logger.debug("vol Id - " + volume.getUid());
//			}
//			
//			
//			//storageJobList.add(storageJobBuilder.buildStorageJob(job));
//		}
		

	}
	
	private StorageJob selectJob(List<StorageJob> storageJobsList, Device tapedriveDevice) {
		StorageJob sj = storageJobsList.get(0);
		storageJobsList.remove(0);
		return sj;
	}

	private void prepareTapeJobAndContinueNextSteps(StorageJob storageJob, TActivedevice tActivedevice, boolean nextStepsInSeparateThread) {
		Job job = null;
		try {
			job = storageJob.getJob();
			Volume volume = storageJob.getVolume();
			updateJobInProgress(job);
			
			Device tapedriveDevice = tActivedevice.getDevice();
			String tapedriveUid = tapedriveDevice.getUid();
			DeviceStatus deviceStatus = DeviceStatus.BUSY;
			logger.debug("Marking drive " + tapedriveUid + " - " +  deviceStatus);
			tActivedevice.setJob(job);
			tActivedevice.setVolume(volume);
			tActivedevice.setDeviceStatus(deviceStatus);
			tActivedevice = tActivedeviceDao.save(tActivedevice);
			
			int tapedriveNo = tapedriveDevice.getDetails().getAutoloader_address(); // data transfer element/drive no
			logger.trace("Getting associated tape library");
			Device tapelibraryDevice = deviceDao.findById(tapedriveDevice.getDetails().getAutoloader_id()).get();
			String tapeLibraryName = tapelibraryDevice.getUid();
			
			boolean tapedriveAlreadyLoadedWithTape = false;
			
			logger.trace("Composing Tape job");
			TapeJob tapeJob = new TapeJob();
			tapeJob.setStorageJob(storageJob);
			tapeJob.settActivedevice(tActivedevice);
			tapeJob.setTapeLibraryName(tapeLibraryName);
			tapeJob.setTapedriveNo(tapedriveNo);
			tapeJob.setTapedriveUid(tapedriveUid);
			tapeJob.setTapedriveAlreadyLoadedWithTape(tapedriveAlreadyLoadedWithTape);
			
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
			logger.debug("Marking back drive " + tapeJob.getTapedriveUid() + " - " + deviceStatus);
			tActivedevice.setJob(null);
			tActivedevice.setVolume(null);
			tActivedevice.setDeviceStatus(deviceStatus);
			tActivedevice = tActivedeviceDao.save(tActivedevice);

//			if current job is write dont mark the device as available as verify needs to happen
//			tapeJob.getStorageJob().getJob().getJobRef().getStatus()
//			if() {
//				
//				
//			}
		}
	}
}
