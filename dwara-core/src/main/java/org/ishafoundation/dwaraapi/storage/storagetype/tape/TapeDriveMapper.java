package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.util.ArrayList;
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
			logger.trace("Now mapping drives for " + tapelibraryName);
			
			MtxStatus mtxStatus = tapeLibraryManager.getMtxStatus(tapelibraryName);

			// Step 1 - getting the empty slot list and an "actor" tape from the storageelement - that can be used to load and verify...
			logger.debug("Now getting empty slot list and selecting an actor tape to be used for load/unloading into drives");
			List<StorageElement> emptyStorageElementsList = new ArrayList<StorageElement>();
			int actorStorageElementNo = 0;
			String actorVolumeTag = null;
			boolean actorPicked = false;
			
			List<StorageElement> storageElementsList = mtxStatus.getSeList();
			for (StorageElement storageElement : storageElementsList) {
				if(storageElement.isEmpty()) {
					emptyStorageElementsList.add(storageElement);
				}else if(!actorPicked){
					actorStorageElementNo = storageElement.getsNo();
					actorVolumeTag = storageElement.getVolumeTag();
					actorPicked = true;
				}
			}
			logger.trace("Actor details - StorageElementNo " + actorStorageElementNo + " and volumeTag " + actorVolumeTag);
			
			// Step 2 - unload all drives
			logger.debug("Now unloading all non-empty drives...");
			List<DataTransferElement> dataTransferElementList = mtxStatus.getDteList();
			
			for (DataTransferElement nthDataTransferElement : dataTransferElementList) {
				int toBeMappedDataTransferElementSNo = nthDataTransferElement.getsNo();
				
				if(!nthDataTransferElement.isEmpty()) {
					logger.debug(nthDataTransferElement + " has a tape loaded already. So unloading it");
					int toBeUnloadedStorageElementNo = emptyStorageElementsList.remove(0).getsNo();
					try {
						tapeLibraryManager.unload(tapelibraryName, toBeMappedDataTransferElementSNo);
						//tapeLibraryManager.unload(tapelibraryName, toBeUnloadedStorageElementNo, toBeMappedDataTransferElementSNo);
					} catch (Exception e) {
						logger.error("Unable to unload " + tapelibraryName + ":" + toBeUnloadedStorageElementNo + ":" + toBeMappedDataTransferElementSNo);
					}
					logger.debug("Unloaded drive " + toBeMappedDataTransferElementSNo);
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
				
				logger.trace("Now checking mapping for DataTransferElement - " + toBeMappedDataTransferElementSNo);
				tapeLibraryManager.load(tapelibraryName, actorStorageElementNo, toBeMappedDataTransferElementSNo);

				for (DriveDetails nthDriveDetails : allDrivesList) {
					String driveId = nthDriveDetails.getDriveId();
					String driveName = nthDriveDetails.getDriveName();
					DriveDetails driveStatusDetails = tapeDriveManager.getDriveDetails(driveName);
					if(driveStatusDetails.getMtStatus().isReady()){ // means drive is not empty and has the tape we loaded
						logger.trace(driveId + " maps to " + toBeMappedDataTransferElementSNo);
						Device deviceToBeUpdated = deviceDao.findById(driveId).get();
						deviceToBeUpdated.getDetails().setAutoloaderAddress(toBeMappedDataTransferElementSNo);
						deviceDao.save(deviceToBeUpdated);
						break;
					}
				}
				tapeLibraryManager.unload(tapelibraryName, actorStorageElementNo, toBeMappedDataTransferElementSNo);
			}
		}
		catch (Exception e) {
			throw e;
		}
	}
}
