package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
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
	private TapeLibraryManager tapeLibraryManager;

	@Autowired
	private TapeDriveManager tapeDriveManager;

	/*
		Step 1 - get a tape from the storageelement that can be used to load and verify...
		Step 2 - load the tape on drives and verify
	*/	
	public void mapDrives(String tapelibraryId, List<DriveDetails> allDrivesList) throws Exception {
		try {
			
			String tapelibraryName = deviceDao.findById(tapelibraryId).get().getWwnId();
			logger.info("Now mapping drives for library - " + tapelibraryName);
			
			MtxStatus mtxStatus = tapeLibraryManager.getMtxStatus(tapelibraryName);

			// Step 1 - getting the empty slot list and an "actor" tape from the storageelement - that can be used to load and verify...
			logger.debug("Now selecting an actor tape to be used for load/unloading into drives");
			int actorStorageElementNo = 0;
			String actorVolumeTag = null;
			
			List<StorageElement> storageElementsList = mtxStatus.getSeList();
			for (StorageElement storageElement : storageElementsList) {
				if(!storageElement.isEmpty()) {
					actorStorageElementNo = storageElement.getsNo();
					actorVolumeTag = storageElement.getVolumeTag();
					if(actorVolumeTag.endsWith("CU")) // clean up tape shouldnt be used as an actor tape // TODO :: configure the chars...
						continue;
					break;
				}
			}
			logger.info("Actor details - StorageElementNo " + actorStorageElementNo + " and volumeTag " + actorVolumeTag);
			
			// Step 2 - unload all drives
			logger.debug("Now unloading all non-empty drives...");
			List<DataTransferElement> dataTransferElementList = mtxStatus.getDteList();
			
			for (DataTransferElement nthDataTransferElement : dataTransferElementList) {
				int toBeMappedDataTransferElementSNo = nthDataTransferElement.getsNo();
				
				if(!nthDataTransferElement.isEmpty()) {
					logger.debug(nthDataTransferElement + " has a tape loaded already. So unloading it");
					try {
						tapeLibraryManager.unload(tapelibraryName, toBeMappedDataTransferElementSNo);
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
			for (DataTransferElement nthDataTransferElement : dataTransferElementList) {
				int toBeMappedDataTransferElementSNo = nthDataTransferElement.getsNo();
				
				logger.info("Now checking mapping for DataTransferElement - " + toBeMappedDataTransferElementSNo);
				tapeLibraryManager.load(tapelibraryName, actorStorageElementNo, toBeMappedDataTransferElementSNo);

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
						deviceToBeUpdated.getDetails().setAutoloaderAddress(toBeMappedDataTransferElementSNo);
						deviceDao.save(deviceToBeUpdated);
						logger.info("Device table for " + driveId + " updated");
						
						allDrivesList.remove(nthDriveDetails);
						break;
					}
				}
				
				if(matchingDriveId == null)
					logger.warn(toBeMappedDataTransferElementSNo + " is not mapped to any drive");
				tapeLibraryManager.unload(tapelibraryName, actorStorageElementNo, toBeMappedDataTransferElementSNo);
				if(allDrivesList.size() == 0) {
					logger.info("All drives mapped. Breaking the loop");
					break;
				}
					
			}
		}
		catch (Exception e) {
			throw e;
		}
	}
}
