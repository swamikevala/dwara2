package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.resp.autoloader.AutoloaderResponse;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Drive;
import org.ishafoundation.dwaraapi.api.resp.autoloader.DriveStatus;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Element;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Tape;
import org.ishafoundation.dwaraapi.api.resp.autoloader.TapeStatus;
import org.ishafoundation.dwaraapi.api.resp.autoloader.ToLoadTape;
import org.ishafoundation.dwaraapi.api.resp.mapdrives.MapDrivesResponse;
import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.db.utils.JobUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.AutoloaderService;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagesubtype.AbstractStoragesubtype;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeDeviceUtil;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeOnLibrary;
import org.ishafoundation.dwaraapi.utils.TapeUsageStatus;
import org.ishafoundation.dwaraapi.utils.VolumeUtil;
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
	private JobDao jobDao;

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private AutoloaderService autoloaderService;
	
	@Autowired
	private Map<String, AbstractStoragesubtype> storagesubtypeMap;
	
	@Autowired
	private Map<String, AbstractStoragetaskAction> storagetaskActionMap;
	
	@Autowired
	private JobUtil jobUtil;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private TapeDeviceUtil tapeDeviceUtil;
	
	@Autowired
	private VolumeUtil volumeUtil;
	
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
		logger.info("/autoloader");
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
	
	@GetMapping(value = "/autoloader/toLoad", produces = "application/json")
	public ResponseEntity<Set<ToLoadTape>> getOfflineTapesToBeLoaded(){ 
		logger.info("/autoloader/toLoad");
		// get all queued storagetask jobs and their needed tapes
		
		// get all jobs with volume id not null and storagetask is not null and status is queued
//		List<Job> queuedJobListWithVolume = jobDao.findAllByVolumeIdIsNotNullAndStoragetaskActionIdIsNotNullAndStatus(Status.queued); // job has volume for restore and finalize
//		List<Job> queuedJobListWithVolumeGroup = jobDao.findAllByGroupVolumeIdIsNotNullAndStoragetaskActionIdIsNotNullAndStatus(Status.queued); // job has volume group for ingest 
		
		Set<ToLoadTape> toLoadTapeList = new HashSet<ToLoadTape>();
		try {
			List<Job> queuedJobList = jobDao.findAllByStoragetaskActionIdIsNotNullAndStatusOrderById(Status.queued); // job has volume group for ingest
			if(queuedJobList.size() == 0)
				logger.info("No storage jobs in queue");
			else {
				List<String> onlineVolumeList = new ArrayList<String>();
				Map<String, String> onlineVolume_Autoloader_Map = new HashMap<String, String>();
	
				// get all online tapes across all libraries
				List<Device> autoloaderDevices = deviceDao.findAllByType(Devicetype.tape_autoloader);
				for (Device autoloaderDevice : autoloaderDevices) {
					String autoloaderId = autoloaderDevice.getId();
					List<TapeOnLibrary> tapeOnLibraryList = tapeLibraryManager.getAllLoadedTapesInTheLibrary(autoloaderDevice.getWwnId());
					
					for (TapeOnLibrary tapeOnLibrary : tapeOnLibraryList) {
						String barcode = tapeOnLibrary.getVolumeTag();
						onlineVolumeList.add(barcode);
						onlineVolume_Autoloader_Map.put(barcode, autoloaderId);
					}
				}
	
				
				int priorityCount = 1;
				for (Job nthQueuedJob : queuedJobList) {
					if(!jobUtil.isJobReadyToBeExecuted(nthQueuedJob))
					continue;
					
					AbstractStoragetaskAction storagetaskActionImpl = storagetaskActionMap.get(nthQueuedJob.getStoragetaskActionId().name());
					logger.trace("Building storage job - " + nthQueuedJob.getId() + ":" + storagetaskActionImpl.getClass().getSimpleName());
					StorageJob storageJob = null;
					try {
						storageJob = storagetaskActionImpl.buildStorageJob(nthQueuedJob);
					} catch (Exception e) {
						logger.error("Unable to gather necessary details for executing the job " + nthQueuedJob.getId() + " - " + Status.failed, e);
						continue;
					}
					
					Volume volume = storageJob.getVolume();
					if(volume != null) {
						String barcode = volume.getId();
						if(!onlineVolumeList.contains(barcode)) {
							ToLoadTape toLoadTape = new ToLoadTape();
							toLoadTape.setAutoloader(onlineVolume_Autoloader_Map.get(barcode));
							toLoadTape.setBarcode(barcode);
							toLoadTape.setFinalized(volume.isFinalized());
							toLoadTape.setLocation(volume.getLocation().getId());
							toLoadTape.setPriority(priorityCount);
							toLoadTapeList.add(toLoadTape);
							priorityCount = priorityCount + 1;
						}
					}
				}
			}
		}
		catch (Exception e) {
			String errorMsg = "Unable to get toload details - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(toLoadTapeList);
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
				// During mapping drives this value is set to null...
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
				tape.setVolumeGroup(StringUtils.substring(barcode, 0, 2));
				String storagesubtypeSuffix = StringUtils.substring(barcode, barcode.length()-2, barcode.length());
				Set<String> storagesubtypeSet = storagesubtypeMap.keySet();
				for (String nthStoragesubtypeImpl : storagesubtypeSet) {
					if(storagesubtypeSuffix.equals(storagesubtypeMap.get(nthStoragesubtypeImpl).getSuffixToEndWith())) {
						tape.setStoragesubtype(nthStoragesubtypeImpl);
						break;
					}
				}
				
				Volume volume = volumeId_VolumeObj_Map.get(barcode);
				TapeStatus tapeStatus = null;
				TapeUsageStatus usageStatus = null;
				if(volume != null) {  
					tape.setLocation(volume.getLocation().getId());
					tape.setRemoveAfterJob(volume.getDetails().getRemoveAfterJob()); // TODO : When do we set this??
					tapeStatus = getTapeStatus(volume);
					usageStatus = volumeUtil.getTapeUsageStatus(volume.getId());
				}
				else { // If its not regd(no entry in volume table for the barcode) in dwara yet, it means its either blank(not formatted) or unknown
					/*
						barcode // if barcode matches a pattern -
							// if any initializing request on this barcode with status queued or in progress
							if
								initializing
							else
								blank 
						else 
							unknown
					*/
					usageStatus = TapeUsageStatus.no_job_queued;
					
					List<Volume> volumeGroupList = volumeDao.findAllByType(Volumetype.group);
					for (Volume nthGroupvolume : volumeGroupList) {
						if(barcode.startsWith(nthGroupvolume.getId())) {
							tapeStatus = TapeStatus.blank;
							break;
						}
					}
					
					if(tapeStatus == TapeStatus.blank) {
						List<Status> statusList = new ArrayList<Status>();
						statusList.add(Status.queued);
						statusList.add(Status.in_progress);
						
						List<Request> initializingSystemRequestList = requestDao.findAllByActionIdAndStatusInAndType(Action.initialize, statusList, RequestType.system);
						for (Request nthInitializingSystemRequest : initializingSystemRequestList) {
							String volumeIdRequested = nthInitializingSystemRequest.getDetails().getVolumeId();
							if(barcode.equals(volumeIdRequested)) {
								tapeStatus = TapeStatus.initializing;
								
								if(nthInitializingSystemRequest.getStatus() == Status.in_progress)
									usageStatus = TapeUsageStatus.job_in_progress;
								else
									usageStatus = TapeUsageStatus.job_queued;
								
								break;
							}
						}
					}
					else {
						tapeStatus = TapeStatus.unknown;
					}
				}
				
				tape.setUsageStatus(usageStatus);
				tape.setStatus(tapeStatus);
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
		}else {
			tapeStatus = TapeStatus.initialized;
		}
		return tapeStatus;
	}
	
	private boolean isTapeBlank(Volume volume){
		boolean isTapeBlank = true; // TODO : Hardcoded.... Should we check if the tape is blank or not by loading the tape and verifying every time its called - No way, there should be a better way...
		return isTapeBlank;
	}
	
	private boolean hasAnyArtifactOnVolume(Volume volume){
		boolean hasAnyArtifactOnVolume = false;
	   	Domain[] domains = Domain.values();
		for (Domain nthDomain : domains) {
		    ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(nthDomain);
		    int artifactVolumeCount = domainSpecificArtifactVolumeRepository.countByIdVolumeId(volume.getId());
			if(artifactVolumeCount > 0) {
				hasAnyArtifactOnVolume = true;
				break;
			}
		}
		return hasAnyArtifactOnVolume;
	}
	
	private boolean isInitialized(Volume volume){
		// If it has come thus far, it means Volume is regd and no artifacts on volume
		return true;
	}
}	