package org.ishafoundation.dwaraapi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.resp.autoloader.AutoloaderResponse;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Drive;
import org.ishafoundation.dwaraapi.api.resp.autoloader.DriveStatus;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Element;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Tape;
import org.ishafoundation.dwaraapi.api.resp.autoloader.TapeStatus;
import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.storage.storagesubtype.AbstractStoragesubtype;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeDeviceUtil;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeOnLibrary;
import org.ishafoundation.dwaraapi.utils.TapeUsageStatus;
import org.ishafoundation.dwaraapi.utils.VolumeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AutoloaderService{

	private static final Logger logger = LoggerFactory.getLogger(AutoloaderService.class);
	
	@Autowired
	private RequestDao requestDao;

	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private Map<String, AbstractStoragesubtype> storagesubtypeMap;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private TapeDeviceUtil tapeDeviceUtil;
	
	@Autowired
	private VolumeUtil volumeUtil;
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;

	public AutoloaderResponse getAutoloader(Device autoloaderDevice) {
		AutoloaderResponse autoloaderResponse = new AutoloaderResponse();
		try {
			String autoloaderId = autoloaderDevice.getId();
			autoloaderResponse.setId(autoloaderId);
			List<Drive> drives = new ArrayList<Drive>();

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
			
			List<Tape> tapes = getLoadedTapesInLibrary(autoloaderDevice, false);
			
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
	
	public List<Tape> getLoadedTapesInLibrary(Device autoloaderDevice, boolean getBlankTapesOnly) throws Exception{
		List<Tape> tapes = new ArrayList<Tape>();
		logger.trace("Getting all loaded tapes in the physcial Tape library " + autoloaderDevice.getId());
		
		Iterable<Volume> volumeList = volumeDao.findAllByStoragetypeAndType(Storagetype.tape, Volumetype.physical);
		HashMap<String, Volume> volumeId_VolumeObj_Map = new HashMap<String, Volume>();
		for (Volume volume : volumeList) {
			volumeId_VolumeObj_Map.put(volume.getId(), volume);
		}
		
		List<Volume> volumeGroupList = volumeDao.findAllByType(Volumetype.group);
		Set<String> volumeGroupIdSet = new TreeSet<String>();
		for (Volume volume : volumeGroupList) {
			volumeGroupIdSet.add(volume.getId());
		}
		
		List<TapeOnLibrary> tapeOnLibraryList = tapeLibraryManager.getAllLoadedTapesInTheLibrary(autoloaderDevice.getWwnId());
		for (TapeOnLibrary tapeOnLibrary : tapeOnLibraryList) {
			String barcode = tapeOnLibrary.getVolumeTag();
			Volume volume = volumeId_VolumeObj_Map.get(barcode);
			if(volume != null && getBlankTapesOnly) { // if we need only blank tapes then skip the volumes that are already registered in Dwara 
				continue; 
			}
			
			Tape tape = new Tape();
			
			tape.setBarcode(barcode);
			
			Element element = Element.slot;
			if(tapeOnLibrary.isLoaded())
				element = Element.drive;
			tape.setElement(element);
			
			tape.setAddress(tapeOnLibrary.getAddress());
			
			String volumeGroup = StringUtils.substring(barcode, 0, 2);
			int volumeGroupIdLength = 0;
			for (String volumeGroupId : volumeGroupIdSet) {
				if(barcode.contains(volumeGroupId) && volumeGroupId.length() > volumeGroupIdLength) {
					volumeGroupIdLength = volumeGroupId.length();
					volumeGroup = volumeGroupId;
				}
			}
			tape.setVolumeGroup(volumeGroup);
			
			String storagesubtypeSuffix = StringUtils.substring(barcode, barcode.length()-2, barcode.length());
			Set<String> storagesubtypeSet = storagesubtypeMap.keySet();
			for (String nthStoragesubtypeImpl : storagesubtypeSet) {
				if(storagesubtypeSuffix.equals(storagesubtypeMap.get(nthStoragesubtypeImpl).getSuffixToEndWith())) {
					tape.setStoragesubtype(nthStoragesubtypeImpl);
					break;
				}
			}
			
			
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
				
				if(barcode.startsWith(volumeGroup)) {
					tapeStatus = TapeStatus.blank;
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
			
			if(getBlankTapesOnly && tapeStatus == TapeStatus.blank) // only add blank tapes to blank tapes call
				tapes.add(tape);
		}
		return tapes;
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
}

