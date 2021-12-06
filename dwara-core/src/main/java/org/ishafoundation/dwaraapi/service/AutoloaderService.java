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
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ArtifactVolumeDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
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
	private ArtifactVolumeDao artifactVolumeDao;
	
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
			
			String volumeGroup = null; // StringUtils.substring(barcode, 0, 2);
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
				tape.setRemoveAfterJob(volume.getGroupRef().getDetails().getRemoveAfterJob()); 
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
				
				if(volumeGroup != null && barcode.startsWith(volumeGroup)) {
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
			
			if(!getBlankTapesOnly || (getBlankTapesOnly && tapeStatus == TapeStatus.blank)) // only add blank tapes to blank tapes call
				tapes.add(tape);
		}
		logger.trace("Got all loaded tapes in the physcial Tape library " + autoloaderDevice.getId());
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
	    int artifactVolumeCount = artifactVolumeDao.countByIdVolumeId(volume.getId());
		if(artifactVolumeCount > 0)
			hasAnyArtifactOnVolume = true;
		return hasAnyArtifactOnVolume;
	}
	
//	public Set<Tape> handleTapes(){
//		Set<Tape> handleTapeList = new HashSet<Tape>();
//		List<String> onlineVolumeList = new ArrayList<String>();
//		Map<String, String> onlineVolume_Autoloader_Map = new HashMap<String, String>();
//		Map<String, Tape> onlineBarcode_Tape_Map = new HashMap<String, Tape>();
//	
//		// get all online tapes across all libraries
//		Set<Tape> tapeList = new HashSet<Tape>();
//		
//		List<Device> autoloaderDevices = deviceDao.findAllByType(Devicetype.tape_autoloader);
//		for (Device autoloaderDevice : autoloaderDevices) {
//			String autoloaderId = autoloaderDevice.getId();
//			tapeList.addAll(getLoadedTapesInLibrary(autoloaderDevice, false));
//	
//			for (Tape nthTape : tapeList) {
//				String barcode = nthTape.getBarcode();
//				onlineVolumeList.add(barcode);
//				onlineVolume_Autoloader_Map.put(barcode, autoloaderId);
//				onlineBarcode_Tape_Map.put(barcode, nthTape);
//			}
//		}
//		
//	//	List<Status> statusList = new ArrayList<Status>();
//	//	statusList.add(Status.queued);
//	//	statusList.add(Status.in_progress);
//	//	
//	//	List<Job> jobList = jobDao.findAllByStoragetaskActionIdIsNotNullAndStatusInOrderById(statusList);
//		
//		// Add tapes - for queued jobs not in tape library 			
//		List<Job> jobList = jobDao.findAllByStoragetaskActionIdIsNotNullAndStatusOrderById(Status.queued);
//		if(jobList.size() == 0)
//			logger.info("No storage jobs in queue");
//		else {
//			int priorityCount = 1;
//			for (Job nthJob : jobList) {
//				Volume volume = null;
//				TapeUsageStatus tapeUsageStatus = null;
//				if(!jobUtil.isJobReadyToBeExecuted(nthJob))
//					continue;
//				
//				AbstractStoragetaskAction storagetaskActionImpl = storagetaskActionMap.get(nthJob.getStoragetaskActionId().name());
//				logger.trace("Building storage job - " + nthJob.getId() + ":" + storagetaskActionImpl.getClass().getSimpleName());
//				StorageJob storageJob = null;
//				try {
//					storageJob = storagetaskActionImpl.buildStorageJob(nthJob);
//				} catch (Exception e) {
//					logger.error("Unable to gather necessary details for executing the job " + nthJob.getId() + " - " + Status.failed, e);
//					continue;
//				}
//				
//				volume = storageJob.getVolume();
//				tapeUsageStatus = TapeUsageStatus.job_queued;
//				
//				if(volume != null) {
//					String barcode = volume.getId();
//					if(!onlineVolumeList.contains(barcode)) {
//						Tape tapeNeeded = new Tape();//onlineBarcode_Tape_Map.get(barcode);
//						tapeNeeded.setBarcode(barcode);
//						tapeNeeded.setAction(nthJob.getStoragetaskActionId().name());
//						tapeNeeded.setLocation(volume.getLocation().getId());
//						tapeNeeded.setUsageStatus(tapeUsageStatus);
//						//toLoadTape.setAutoloader(onlineVolume_Autoloader_Map.get(barcode));
//						handleTapeList.add(tapeNeeded);
//						logger.debug(tapeUsageStatus + " but tape " + barcode + " missing in library");
//						priorityCount = priorityCount + 1;
//					}
//				}
//			}
//		}
//	
//		// Add tapes - for capacity expansion
//		// If there are any groups running out of space and needing new tapes
//		List<VolumeResponse> volGroupList = volumeService.getVolumeByVolumetype(Volumetype.group.name());
//		for (VolumeResponse volumeResponse : volGroupList) {
//			if(volumeResponse.getDetails().isExpandCapacity()) {
//				Tape tapeNeeded = new Tape();//onlineBarcode_Tape_Map.get(barcode);
//				tapeNeeded.setBarcode(volumeResponse.getDetails().getNextBarcode());
//				tapeNeeded.setAction("@MH what action for pools running out of space???"); // TODO - Action = Write ???
//				// tapeNeeded.setUsageStatus(TapeUsageStatus.job_queued);
//				handleTapeList.add(tapeNeeded);
//			}
//		}
//	
//		// Show Tapes in action - currently restoring/writing
//		List<Job> inProgressJobsList = jobDao.findAllByStoragetaskActionIdIsNotNullAndStatusOrderById(Status.in_progress);
//		if(inProgressJobsList.size() == 0)
//			logger.info("No storage jobs in progress");
//		else {
//			for (Job nthJob : inProgressJobsList) {
//				Volume volume = nthJob.getVolume();
//				TapeUsageStatus tapeUsageStatus = TapeUsageStatus.job_in_progress;
//				if(volume != null) {
//					String barcode = volume.getId();
//					Tape tapeInAction = new Tape();
//					tapeInAction.setBarcode(barcode);
//					tapeInAction.setAction(nthJob.getStoragetaskActionId().name());
//					tapeInAction.setLocation(volume.getLocation().getId());
//					tapeInAction.setUsageStatus(tapeUsageStatus);
//					handleTapeList.add(tapeInAction);
//					logger.debug(barcode + " " + tapeUsageStatus);
//				}
//			}
//		}
//	
//		// Remove/Written tapes - No jobs queued and either finalized or removeAfterJob
//		for (Tape nthTapeOnLibrary : tapeList) {
//			if(nthTapeOnLibrary.getUsageStatus() == TapeUsageStatus.no_job_queued && (nthTapeOnLibrary.getStatus() == TapeStatus.finalized || nthTapeOnLibrary.isRemoveAfterJob())) {
//				// last job on tape determines if the tape need to be shown in remove tapes(restore) or written tapes
//				Job lastJobOnTape = jobDao.findTopByStoragetaskActionIdIsNotNullAndVolumeIdAndStatusAndCompletedAtIsNotNullOrderByCompletedAtDesc(nthTapeOnLibrary.getBarcode(), Status.completed);
//				Request request = lastJobOnTape.getRequest();
//				Action requestedAction = request.getActionId();
//				if(requestedAction == Action.ingest)
//					nthTapeOnLibrary.setAction("write");
//				else
//					nthTapeOnLibrary.setAction("restore");
//				handleTapeList.add(nthTapeOnLibrary);
//			}
//		}
//	}
}

