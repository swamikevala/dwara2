package org.ishafoundation.dwaraapi.storage.storagetype.thread;

import java.util.List;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.ActionAttributeConverter;
import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TActivedeviceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.DeviceStatus;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.executor.TapeTaskThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("tape"+DwaraConstants.StorageTypeJobManagerSuffix)
@Scope("prototype")
public class TapeJobManager extends AbstractStoragetypeJobManager {


    private static final Logger logger = LoggerFactory.getLogger(TapeJobManager.class);

	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private TActivedeviceDao tActivedeviceDao;
	
	@Autowired
	private ApplicationContext applicationContext;	
	
	@Autowired
	private TapeTaskThreadPoolExecutor tapeTaskThreadPoolExecutor;	
	
	@Autowired
	private ActionAttributeConverter actionAttributeConverter;
	
	@Override
    public void run() {
		logger.debug(this.getClass().getName() + " tape job manager");
		List<StorageJob> storageJobsList = getStorageJobList();
		// execute the job

		StorageJob storageJob = storageJobsList.get(0); // if there is a format/mapdrive job only one job will be in the list coming from JobManager... 
		Action storagetaskAction = actionAttributeConverter.convertToEntityAttribute(storageJob.getJob().getStoragetaskActionId());
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
			logger.debug("unloading all tapes from all drives by tapeDrivePreparer");
			//tapeDrivePreparer
			
			if(storagetaskAction == Action.map_tapedrives) {
				//job inprogress
				Status status = Status.in_progress;
				try {
					logger.debug("mappig drives using TapeDriveMapper");
					//TapeDriveMapper
					status = Status.completed;
				}catch (Exception e) {
					// TODO: handle exception
					status = Status.failed;
				}finally {
					// job status
					// where do we update job status...
				}
			}
			else if(storagetaskAction == Action.format) {
				TActivedevice tActivedevice = activeDeviceList.get(0);
				Device tapedriveDevice = tActivedevice.getDevice();
				logger.debug("\t\tdrive selected for the format job");
				
				prepareTapeJobAndContinueNextSteps(storageJob, tapedriveDevice, false);
			}
		}
		else {
			for (TActivedevice tActivedevice : activeDeviceList) {
				logger.debug("\t\titerating through available drives");
				Device tapedriveDevice = tActivedevice.getDevice();
				logger.debug("\t\tdrive selected");
				// select a job for the available drive
				// if drive has a tape loaded that matches the joblist
				// TODO : Tape job selection happens here
				StorageJob selectedStorageJob = selectJob(storageJobsList, tapedriveDevice);
				logger.debug("\t\tjob selected");
				prepareTapeJobAndContinueNextSteps(selectedStorageJob, tapedriveDevice, true);
 
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
		return storageJobsList.get(0);
	}

	private void prepareTapeJobAndContinueNextSteps(StorageJob storageJob, Device tapedriveDevice, boolean nextStepsInSeparateThread) {
		int tapedriveNo = tapedriveDevice.getDetails().getAutoloader_address(); // data transfer element/drive no
		String tapedriveUid = tapedriveDevice.getUid();
		Device tapelibraryDevice = deviceDao.findById(tapedriveDevice.getDetails().getAutoloader_id()).get();
		String tapeLibraryName = tapelibraryDevice.getUid();
		logger.debug("\t\t drive marked Busy");
		
		boolean tapedriveAlreadyLoadedWithTape = false;
		
		logger.debug("\t\tcomposing Tape job");
		TapeJob tapeJob = new TapeJob();
		tapeJob.setStorageJob(storageJob);
		tapeJob.setTapeLibraryName(tapeLibraryName);
		tapeJob.setTapedriveNo(tapedriveNo);
		tapeJob.setTapedriveUid(tapedriveUid);
		tapeJob.setTapedriveAlreadyLoadedWithTape(tapedriveAlreadyLoadedWithTape);
		
		
		if(nextStepsInSeparateThread) {
			logger.debug("\t\tlaunching separate tape task thread -----------");
			TapeTask tapeTask = new TapeTask();//applicationContext.getBean(TapeTask.class); 
			tapeTask.setTapeJob(tapeJob);
			tapeTaskThreadPoolExecutor.getExecutor().execute(tapeTask);
		}
		else {
			logger.debug("\t\tinvoking storage task in same thread");
			process(tapeJob);
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
			logger.debug(this.getClass().getName() + " prototype scoped tape task");
			process(tapeJob);
		}
	}
}
