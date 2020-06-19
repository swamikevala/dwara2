package org.ishafoundation.dwaraapi.storage.storagetype.thread;

import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.StoragetaskAttributeConverter;
import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TActivedeviceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.ishafoundation.dwaraapi.enumreferences.DeviceStatus;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.enumreferences.Storagetask;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetask;
import org.ishafoundation.dwaraapi.storage.storagetask.StoragetaskFactory;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.TapeTask;
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
	
	@Override
    public void run() {
		System.out.println(this.getClass().getName() + " tape job manager");
		List<TActivedevice> activeDevice = tActivedeviceDao.findAllByDeviceDevicetypeAndDeviceStatus(Devicetype.tape_drive, DeviceStatus.AVAILABLE);
		for (TActivedevice tActivedevice : activeDevice) {
			System.out.println("\t\titerating through available drives");
			Device tapedriveDevice = tActivedevice.getDevice();
			
			int tapedriveNo = tapedriveDevice.getDetails().getAutoloader_address(); // data transfer element/drive no
			String tapedriveUid = tapedriveDevice.getUid();
			Device tapelibraryDevice = deviceDao.findById(tapedriveDevice.getDetails().getAutoloader_id()).get();
			String tapeLibraryName = tapelibraryDevice.getUid();
			
			// TODO : need to call tapelibrary to get the available drives also 
			// TODO : Tape job selection happens here
			// based on available tapedrives
			
			// select a job for the available drive
				// if drive has a tape loaded that matches the joblist
			List<StorageJob> storageJobsList = getStorageJobList();
			// execute the job

			StorageJob storageJob = storageJobsList.get(0); //selected Job
			
			boolean tapedriveAlreadyLoadedWithTape = false;
			System.out.println("\t\tcomposing Tape job");
			TapeJob tapeJob = new TapeJob();
			tapeJob.setStorageJob(storageJob);
			tapeJob.setTapeLibraryName(tapeLibraryName);
			tapeJob.setTapedriveNo(tapedriveNo);
			tapeJob.setTapedriveUid(tapedriveUid);
			tapeJob.setTapedriveAlreadyLoadedWithTape(tapedriveAlreadyLoadedWithTape);
			System.out.println("\t\tlaunching separate tape task thread -----------");
			TapeTask tapeTask = new TapeTask();//applicationContext.getBean(TapeTask.class); 
			tapeTask.setTapeJob(tapeJob);
			tapeTaskThreadPoolExecutor.getExecutor().execute(tapeTask);
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
//				System.out.println("vol Id - " + volume.getUid());
//			}
//			
//			
//			//storageJobList.add(storageJobBuilder.buildStorageJob(job));
//		}
		

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
			System.out.println(this.getClass().getName() + " prototype scoped tape task");
			invokeStoragetask(tapeJob);
		}
	}
}
