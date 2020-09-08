package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.autoloader.AutoloaderResponse;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Drive;
import org.ishafoundation.dwaraapi.api.resp.autoloader.DriveStatus;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Element;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Tape;
import org.ishafoundation.dwaraapi.api.resp.autoloader.TapeStatus;
import org.ishafoundation.dwaraapi.api.resp.autoloader.TapeUsageStatus;
import org.ishafoundation.dwaraapi.api.resp.mapdrives.MapDrivesResponse;
import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.AutoloaderService;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeDeviceUtil;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeOnLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class AutoloaderController {

	private static final Logger logger = LoggerFactory.getLogger(AutoloaderController.class);

	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private VolumeDao volumeDao;

	@Autowired
	private AutoloaderService autoloaderService;
	
	@Autowired
	private TapeDeviceUtil tapeDeviceUtil;

	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@PostMapping(value = "/autoloader/{autoloaderId}/mapDrives", produces = "application/json")
	public ResponseEntity<MapDrivesResponse> mapDrives(@PathVariable("autoloaderId") String autoloaderId){
		MapDrivesResponse mapDrivesResponse = null;
		
		try {
			mapDrivesResponse = autoloaderService.mapDrives(autoloaderId);
		}catch (Exception e) {
			String errorMsg = "Unable to map drives - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(mapDrivesResponse);
	}
	
	@GetMapping(value = "/autoloader", produces = "application/json")
	public ResponseEntity<List<AutoloaderResponse>> getAllAutoloaders(){
		List<AutoloaderResponse> autoloaderResponseList = new ArrayList<AutoloaderResponse>();
		try {
			//List<Device> autoloaderDevices = configurationTablesUtil.getAllConfiguredAutoloaderDevices();
			List<Device> autoloaderDevices = deviceDao.findAllByType(Devicetype.tape_autoloader);
			for (Device autoloaderDevice : autoloaderDevices) {
				String autoloaderId = autoloaderDevice.getId();
				AutoloaderResponse autoloaderResponse = getAutoloader_Internal(autoloaderId);
				autoloaderResponseList.add(autoloaderResponse);
			}
		}catch (Exception e) {
			String errorMsg = "Unable to get autoloader details - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(autoloaderResponseList);
	}
	
	@GetMapping(value = "/autoloader/{autoloaderId}", produces = "application/json")
	public ResponseEntity<AutoloaderResponse> getAutoloader(@PathVariable("autoloaderId") String autoloaderId){ // TODO is this id or uid thats going to be requested? should be uid 
		AutoloaderResponse autoloaderResponse = getAutoloader_Internal(autoloaderId);
		return ResponseEntity.status(HttpStatus.OK).body(autoloaderResponse);
	}

	private AutoloaderResponse getAutoloader_Internal(String autoloaderId) {
		AutoloaderResponse autoloaderResponse = new AutoloaderResponse();
		try {
			autoloaderResponse.setId(autoloaderId);
			List<Drive> drives = new ArrayList<Drive>();
			List<Tape> tapes = new ArrayList<Tape>();
			
//			Device autoloaderDevice = configurationTablesUtil.getDevice(autoloaderId);
			Device autoloaderDevice = deviceDao.findById(autoloaderId).get();
			if(autoloaderDevice == null) {
				throw new DwaraException(autoloaderId + " does not exist in the system", null);
			}

//			List<Device> allDrives = configurationTablesUtil.getAllConfiguredDriveDevices();
			logger.trace("Following drives are mapped in dwara to " + autoloaderId);
			List<Device> allDrives = deviceDao.findAllByType(Devicetype.tape_drive);
			HashMap<String, Device> deviceId_DeviceObj_Map = new HashMap<String, Device>();
			for (Device device : allDrives) {
				logger.trace(device.getId());	
				deviceId_DeviceObj_Map.put(device.getId(), device);
			}
			
			logger.trace("Getting all drives details from the physcial Tape library " + autoloaderId);
			List<DriveDetails> driveDetailsList = tapeDeviceUtil.getAllDrivesDetails();
			for (DriveDetails driveDetails : driveDetailsList) {
				String driveId = driveDetails.getDriveId();
				logger.trace("Adding details for " + driveId);
				Device configuredDriveDevice = deviceId_DeviceObj_Map.get(driveId);
				
				Drive drive = new Drive();
				drive.setId(driveId);
				drive.setAddress(configuredDriveDevice.getDetails().getAutoloaderAddress());
				drive.setBarcode(driveDetails.getDte().getVolumeTag());
				drive.setEmpty(driveDetails.getDte().isEmpty());
				DriveStatus status = DriveStatus.available;
				if(driveDetails.getMtStatus().isBusy())
					status = DriveStatus.busy;
				drive.setStatus(status);
				
				drives.add(drive);
			}
			autoloaderResponse.setDrives(drives);
			
			Iterable<Volume> volumeList = volumeDao.findAllByStoragetypeAndType(Storagetype.tape, Volumetype.physical);
			HashMap<String, Volume> volumeId_VolumeObj_Map = new HashMap<String, Volume>();
			for (Volume volume : volumeList) {
				volumeId_VolumeObj_Map.put(volume.getId(), volume);
			}
			
			logger.trace("Getting all loaded tapes in the physcial Tape library " + autoloaderId);
			List<TapeOnLibrary> tapeOnLibraryList = tapeLibraryManager.getAllLoadedTapesInTheLibrary(autoloaderDevice.getWwnId());
			for (TapeOnLibrary tapeOnLibrary : tapeOnLibraryList) {
				Tape tape = new Tape();
				Element element = Element.slot;
				if(tapeOnLibrary.isLoaded())
					element = Element.drive;
				tape.setElement(element);
				
				tape.setAddress(tapeOnLibrary.getAddress());
				
				String barcode = tapeOnLibrary.getVolumeTag();
				tape.setBarcode(barcode);
				
				Volume volume = volumeId_VolumeObj_Map.get(barcode);
				if(volume != null) {  
					tape.setLocation(volume.getLocation().getId());
					tape.setRemoveAfterJob(volume.getDetails().getRemoveAfterJob());
					
					TapeStatus tapeStatus = getTapeStatus(volume);
					tape.setStatus(tapeStatus);
					
					TapeUsageStatus usageStatus = getTapeUsageStatus(volume);
					tape.setUsageStatus(usageStatus);
				}
				else { // If its not regd in dwara yet, it means its either blank(not formatted) or unknown
					TapeUsageStatus tapeUsageStatus = getTapeUsageStatus(volume);
					if(tapeUsageStatus == TapeUsageStatus.no_job_queued) {
						if(isTapeBlank(volume)){
							tape.setStatus(TapeStatus.blank);
						}
						else
							tape.setStatus(TapeStatus.unknown);
					}
					else{
						tape.setStatus(TapeStatus.initializing);
					}
				}
				tapes.add(tape);
			}
			
			autoloaderResponse.setTapes(tapes);
		}catch (Exception e) {
			String errorMsg = "Unable to get autoloader details - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		return autoloaderResponse;
	}
	private TapeStatus getTapeStatus(Volume volume) {
		TapeStatus tapeStatus = null;
		if(volume.isImported()) {
			tapeStatus = TapeStatus.imported;
		}else if(volume.isFinalized()) {
			tapeStatus = TapeStatus.finalized;
		}else if(hasAnyArtifactOnVolume(volume)) { // any artifact_volume record means partially_written
			tapeStatus = TapeStatus.partially_written;
		}else if(isInitialized(volume)) {
			tapeStatus = TapeStatus.initialized;
		}else {
			tapeStatus = TapeStatus.unknown;
		}
		return tapeStatus;
	}
	
	private boolean isTapeBlank(Volume volume){
		boolean isTapeBlank = true; // TODO : Hardcoded.... Should we check if the tape is blank or not by loading the tape and verifying every time its called - No way, there should be a better way...
		return isTapeBlank;
	}
	
	private boolean hasAnyArtifactOnVolume(Volume volume){
		boolean hasAnyArtifactOnVolume = false; // TODO : Hardcoded....
		return hasAnyArtifactOnVolume;
	}
	
	private boolean isInitialized(Volume volume){
		boolean isInitialized = true; // TODO : Hardcoded....
		return isInitialized;
	}
	
	private TapeUsageStatus getTapeUsageStatus(Volume volume) {
		TapeUsageStatus tapeUsageStatus = null;
		
		if(getInProgressJobOnVolume(volume)) {
			tapeUsageStatus = TapeUsageStatus.job_in_progress;
		}
		else if(getQueuedJobOnVolume(volume)){
			// any group job queued up
			tapeUsageStatus = TapeUsageStatus.job_queued;
		}
		else {
			// if no job lined up, means no job needs it  
			tapeUsageStatus = TapeUsageStatus.no_job_queued;
		}
		return tapeUsageStatus;
	}
	
	private boolean getInProgressJobOnVolume(Volume volume){
		boolean inProgressJob = false; // TODO : Hardcoded....
		return inProgressJob;
	}
	
	private boolean getQueuedJobOnVolume(Volume volume){
		boolean queuedJob = false; // TODO : Hardcoded....
		return queuedJob; 
	}
}	