package org.ishafoundation.dwaraapi.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req.initialize.InitializeUserRequest;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Tape;
import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.service.AutoloaderService;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.VolumeInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@Component
public class ScheduledBlankTapeAutoInitializer {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduledBlankTapeAutoInitializer.class);
	
	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private AutoloaderService autoloaderService;
	
	@Autowired
	private VolumeInitializer volumeInitializer;

	@Scheduled(cron = "${scheduler.blankTapeAutoInitializer.cronExpression}")
	public void autoInitializeBlankTapes(){
		logger.info("***** Auto initializing blank Tapes *****");

		try {
			List<Device> autoloaderDevices = deviceDao.findAllByType(Devicetype.tape_autoloader);
			for (Device autoloaderDevice : autoloaderDevices) {
				List<InitializeUserRequest> initializeUserRequestList = new ArrayList<InitializeUserRequest>(); 
				List<Tape> blankTapesList = autoloaderService.getLoadedTapesInLibrary(autoloaderDevice, true); // get only blank tapes from library
				for (Tape tape : blankTapesList) {
					InitializeUserRequest initializeUserRequest = new InitializeUserRequest();
					initializeUserRequest.setForce(false);
					initializeUserRequest.setStoragesubtype(tape.getStoragesubtype());
					initializeUserRequest.setVolume(tape.getBarcode());
					// Not needed initializeUserRequest.setVolumeBlocksize(524288);
					initializeUserRequest.setVolumeGroup(tape.getVolumeGroup());
					initializeUserRequestList.add(initializeUserRequest);
				}
				
				volumeInitializer.validateInitializeUserRequest(initializeUserRequestList);
				
				volumeInitializer.initialize(DwaraConstants.SYSTEM_USER_NAME, initializeUserRequestList);
			}
			
		} catch (Exception e) {
			logger.error("Unable to auto initializing blank Tapes", e);
		}
	}
}