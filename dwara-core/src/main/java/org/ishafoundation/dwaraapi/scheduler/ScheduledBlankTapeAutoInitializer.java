package org.ishafoundation.dwaraapi.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req.initialize.InitializeUserRequest;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Tape;
import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.AutoloaderService;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.VolumeInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScheduledBlankTapeAutoInitializer {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduledBlankTapeAutoInitializer.class);
	
	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private AutoloaderService autoloaderService;
	
	@Autowired
	private VolumeInitializer volumeInitializer;

	@Scheduled(cron = "${scheduler.blankTapeAutoInitializer.cronExpression}")
	@PostMapping("/autoInitializeBlankTapes")
	public void autoInitializeBlankTapes(){
		logger.info("***** Auto initializing blank Tapes *****");

		try {
			List<Device> autoloaderDevices = deviceDao.findAllByType(Devicetype.tape_autoloader);
			
			List<Volume> volumeGroupList = volumeDao.findAllByType(Volumetype.group); 
			Map<String, Volume> volumeGroupId_Volume_Map = new HashMap<String, Volume>();
			for (Volume volume : volumeGroupList) {
				volumeGroupId_Volume_Map.put(volume.getId(), volume);
			}

			
			for (Device autoloaderDevice : autoloaderDevices) {
				List<InitializeUserRequest> initializeUserRequestList = new ArrayList<InitializeUserRequest>(); 
				List<Tape> blankTapesList = autoloaderService.getLoadedTapesInLibrary(autoloaderDevice, true); // get only blank tapes from library
				for (Tape tape : blankTapesList) {
					String barcode = tape.getBarcode();
					if(barcode.startsWith(DwaraConstants.CLEANUP_TAPE_PREFIX)) // Dont load clean up tapes
						continue;
					
					String volumeGroupId = tape.getVolumeGroup();
					Volume volumeGroup = volumeGroupId_Volume_Map.get(volumeGroupId);
					if(volumeGroup == null) {
						logger.warn("Volume Group " + volumeGroupId + " doesnt exist. Skipping " + barcode);
						continue;
					}
					
					InitializeUserRequest initializeUserRequest = new InitializeUserRequest();
					initializeUserRequest.setForce(false);
					initializeUserRequest.setStoragesubtype(tape.getStoragesubtype());
					initializeUserRequest.setVolume(barcode);
					// Not needed initializeUserRequest.setVolumeBlocksize(524288);
					initializeUserRequest.setVolumeGroup(volumeGroupId);
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