package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.storage.storagesubtype.AbstractStoragesubtype;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.StorageElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.status.MtxStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeDriveMapper {
	
	private static Logger logger = LoggerFactory.getLogger(TapeDriveMapper.class);
	
	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;

	@Autowired
	private TapeDriveManager tapeDriveManager;

	@Autowired
	private Map<String, AbstractStoragesubtype> storagesubtypeMap;
	
	/*
		Step 1 - get a tape from the storageelement that can be used to load and verify...
		Step 2 - load the tape on drives and verify
	*/	
	public void mapDrives(String tapelibraryId, List<DriveDetails> allDrivesList) throws Exception {
		try {
			// Step 0 - defaulting autoloader addresses
//			Map<Integer, String> autoloaderAddress_DriveId_Map = new HashMap<Integer, String>();
			List<Device> allConfiguredDrivesInDwara = deviceDao.findAllByType(Devicetype.tape_drive);
//			int autoloaderAddress = 0;
			for (Device nthDrive : allConfiguredDrivesInDwara) {
				nthDrive.getDetails().setAutoloaderAddress(null);	
//				autoloaderAddress_DriveId_Map.put(autoloaderAddress, nthDrive.getId());
//				autoloaderAddress =+ 1;
			}
			allConfiguredDrivesInDwara = (List<Device>) deviceDao.saveAll(allConfiguredDrivesInDwara);

			
			String tapelibraryName = deviceDao.findById(tapelibraryId).get().getWwnId();
			logger.info("Now mapping drives for library - " + tapelibraryName);
			
			MtxStatus mtxStatus = tapeLibraryManager.getMtxStatus(tapelibraryName);

			Map<Integer, Actor> volumeGeneration_Actor_Map = new HashMap<Integer, TapeDriveMapper.Actor>(); 
			// Step 1 - getting the empty slot list and an "actor" tape from the storageelement - that can be used to load and verify...
			logger.debug("Now selecting actor tapes to be used for load/unloading into different generation drives");
			List<StorageElement> storageElementsList = mtxStatus.getSeList();
			for (StorageElement storageElement : storageElementsList) {
				if(!storageElement.isEmpty()) {
					String volumeTag = storageElement.getVolumeTag();
					
					if(volumeTag.endsWith("CU")) // clean up tape shouldnt be used as an actor tape // TODO :: configure the chars...
						continue;

					Optional<Volume> volumeEntity = volumeDao.findById(volumeTag);
					if(volumeEntity.isPresent()) { // if volumeTag in tapelibrary is a registered dwara tape - get its generation
						String storagesubtype = volumeEntity.get().getStoragesubtype();
						int volumeGeneration = storagesubtypeMap.get(storagesubtype).getGeneration();
						
						if(volumeGeneration_Actor_Map.get(volumeGeneration) == null) {
							Actor actor = new Actor();
							actor.setStorageElementNo(storageElement.getsNo());
							actor.setVolumeTag(volumeTag);

							volumeGeneration_Actor_Map.put(volumeGeneration, actor);
							logger.info("Actor details for volumeGeneration " + volumeGeneration + " - StorageElementNo " + actor.getStorageElementNo() + " and volumeTag " + actor.getVolumeTag());
						}
					}
				}
			}
		
			if(volumeGeneration_Actor_Map.keySet().size() == 0) {
				throw new Exception("No registered tapes");
			}
			// Step 2 - unload all drives
			logger.debug("Now unloading all non-empty drives...");
			List<DataTransferElement> dataTransferElementList = mtxStatus.getDteList();
			
			for (DataTransferElement nthDataTransferElement : dataTransferElementList) {
				int toBeMappedDataTransferElementSNo = nthDataTransferElement.getsNo();
				
				if(!nthDataTransferElement.isEmpty()) {
					logger.debug(nthDataTransferElement + " has a tape loaded already. So unloading it");
					try {
						tapeLibraryManager.unload(tapelibraryName, toBeMappedDataTransferElementSNo); // unload to an empty slot
					} catch (Exception e) {
						logger.error("Unable to unload " + tapelibraryName + ":" + toBeMappedDataTransferElementSNo);
						throw e;
					}
					logger.debug("Unloaded drive " + toBeMappedDataTransferElementSNo);
				}
				else {
					logger.debug(nthDataTransferElement + " already empty. So not performing unload");
				}
			}
		
			
			/*
			Step 3 - load the tape on to drives and verify drive status
				
				load a tape on to drive i
				!~! mt status the already mapped devicewwid and check if drive status online meaning tape is loaded
				loop the drivelist
					check mt status and check if drive status online meaning tape is loaded
					if true 
						update
						skip loop 
					else continue
				iterate to next drive
				unload the tape from drive i
				
			*/
			List<Integer> unmappedDTEs = new ArrayList<Integer>();
			List<Device> mappedDrives = new ArrayList<Device>();
			
			
			for (DataTransferElement nthDataTransferElement : dataTransferElementList) {
				int toBeMappedDataTransferElementSNo = nthDataTransferElement.getsNo();
				
				// We dont know what generation this DataTransferElement/Drive is so looping on generations
				logger.info("Now checking mapping for DataTransferElement - " + toBeMappedDataTransferElementSNo);
				Integer actorStorageElementNo = load(tapelibraryName, volumeGeneration_Actor_Map, toBeMappedDataTransferElementSNo);
				
				if(actorStorageElementNo == null) {
					logger.warn("No supported generation tape available for DataTransferElement - " + toBeMappedDataTransferElementSNo + ". Skipping mapping");
					unmappedDTEs.add(toBeMappedDataTransferElementSNo);
					continue;
				}
					

				String matchingDriveId = null;
				for (DriveDetails nthDriveDetails : allDrivesList) {
					String driveId = nthDriveDetails.getDriveId();
					String driveName = nthDriveDetails.getDriveName();
					logger.trace("Checking if " + driveName + " has got the tape loaded");
					DriveDetails driveStatusDetails = tapeDriveManager.getDriveDetails(driveName);
					if(driveStatusDetails.getMtStatus().isReady()){ // means drive is not empty and has the tape we loaded
						matchingDriveId = driveId;
						logger.trace(driveId + " has got the tape loaded");	
						logger.info(toBeMappedDataTransferElementSNo + " maps to " + driveId);

						Device deviceToBeUpdated = deviceDao.findById(driveId).get();
//						String existingAutoloaderAddressReferencedDriveId = autoloaderAddress_DriveId_Map.get(toBeMappedDataTransferElementSNo);
//						if(!driveId.equals(existingAutoloaderAddressReferencedDriveId)) {
//							int existingAutoloaderAddress = deviceToBeUpdated.getDetails().getAutoloaderAddress();
							deviceToBeUpdated.getDetails().setAutoloaderAddress(toBeMappedDataTransferElementSNo);
							deviceDao.save(deviceToBeUpdated);
							logger.info(driveId + " updated with " + toBeMappedDataTransferElementSNo);
//							logger.info(driveId + " replaces existing autoloaderaddress " + existingAutoloaderAddress + " with " + toBeMappedDataTransferElementSNo);
							mappedDrives.add(deviceToBeUpdated);
//							Device deviceToBeSwapped = deviceDao.findById(existingAutoloaderAddressReferencedDriveId).get();
//							deviceToBeSwapped.getDetails().setAutoloaderAddress(existingAutoloaderAddress);
//							deviceDao.save(deviceToBeSwapped);
//							logger.info(driveId + " has swapped its existing autoloaderaddress " + toBeMappedDataTransferElementSNo + " with " + existingAutoloaderAddress);
//						}
//						else {
//							logger.info("No new mapping needed for " + driveId + ". Already pakka" + toBeMappedDataTransferElementSNo);
//						}
						allDrivesList.remove(nthDriveDetails);
						break;
					}
				}
				
				if(matchingDriveId == null) {
					logger.warn(toBeMappedDataTransferElementSNo + " is not mapped to any drive");
					unmappedDTEs.add(toBeMappedDataTransferElementSNo);
				}
				
				tapeLibraryManager.unload(tapelibraryName, actorStorageElementNo, toBeMappedDataTransferElementSNo);
				if(allDrivesList.size() == 0) {
					logger.info("All drives mapped. Breaking the loop");
					break;
				}
			}
			
			// Setting the unmapped drives with some unmapped DTEs...
			allConfiguredDrivesInDwara = deviceDao.findAllByType(Devicetype.tape_drive);
			allConfiguredDrivesInDwara.removeAll(mappedDrives);
			int count = 0;
			for (Device nthUnmappedDevice : allConfiguredDrivesInDwara) {
				if(count < allConfiguredDrivesInDwara.size()) {
					Integer dteSno= unmappedDTEs.get(count);
					nthUnmappedDevice.getDetails().setAutoloaderAddress(dteSno);
					deviceDao.save(nthUnmappedDevice);
					logger.info("Unmapped Device " +  nthUnmappedDevice.getId() + " defaulted with " + dteSno);
				}else
					logger.info("Unable to default Device " +  nthUnmappedDevice.getId());
				count += 1;
			}
			
		}
		catch (Exception e) {
			logger.error("Unable to map drives " + e.getMessage(), e);
			throw e;
		}
	}
	
	private Integer load(String tapelibraryName,  Map<Integer, Actor> volumeGeneration_Actor_Map, int toBeMappedDataTransferElementSNo){
		Set<Integer> volumeGenerations = volumeGeneration_Actor_Map.keySet();
		Integer slotNo = null;
		for (Integer nthGeneration : volumeGenerations) {
			Actor actor = volumeGeneration_Actor_Map.get(nthGeneration);
			
			try {
				tapeLibraryManager.load(tapelibraryName, actor.getStorageElementNo(), toBeMappedDataTransferElementSNo);
				logger.debug(actor.getVolumeTag() + " from slot " + actor.getStorageElementNo() + " loaded succesfully into drive " + toBeMappedDataTransferElementSNo);
				slotNo = actor.getStorageElementNo();
				break;
			}catch (Exception e) {
				logger.debug("Potentially a different generation drive. Try with another one");
			}
		}
		return slotNo;
	}
	
	private class Actor{
		Integer storageElementNo = null;
		String volumeTag = null;
		
		public Integer getStorageElementNo() {
			return storageElementNo;
		}
		public void setStorageElementNo(Integer storageElementNo) {
			this.storageElementNo = storageElementNo;
		}
		public String getVolumeTag() {
			return volumeTag;
		}
		public void setVolumeTag(String volumeTag) {
			this.volumeTag = volumeTag;
		}
	}
}
