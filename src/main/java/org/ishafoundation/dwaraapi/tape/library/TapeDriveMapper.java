package org.ishafoundation.dwaraapi.tape.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ishafoundation.dwaraapi.db.dao.master.TapedriveDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.tape.drive.status.DriveStatusDetails;
import org.ishafoundation.dwaraapi.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.tape.library.components.StorageElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeDriveMapper {
	
	@Autowired
	private TapedriveDao tapedriveDao;
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;

	@Autowired
	private TapeDriveManager tapeDriveManager;

	/*
		Step 3 - get a tape from the storageelement that can be used to load and verify...
		Step 4 - load a tape on drives and verify
	*/	
	public void mapDrives(String tapelibraryName, List<DataTransferElement> allDrives) {
		try {
			
			// Step 3 - get a tape from the storageelement that can be used to load and verify...
			int toBeUsedStorageElementNo = 0;
			List<StorageElement> storageElementsList = tapeLibraryManager.getAllStorageElementsList(tapelibraryName);
			for (StorageElement storageElement : storageElementsList) {
				if(!storageElement.isEmpty()) {
					toBeUsedStorageElementNo = storageElement.getsNo();
					break;
				}
			}
			
			/*
			Step 4 - load a tape on drives and verify
				
				load a tape on to drive i
				!~! mt status the already mapped devicewwid and check if drive status online meaning tape is loaded
				if not loop the drivelist
					check !~!
					if true skip loop else continue
				iterate to next drive
				
				Update tapedrive or hold the details in collection and update the table later...
				unload the tape from drive i
				
			*/
			Map<Integer, Integer> libraryToDriveElementAddressMap = new HashMap<Integer, Integer>();
			for (DataTransferElement dataTransferElement : allDrives) {
				int toBeMappedDataTransferElementSNo = dataTransferElement.getsNo();
				
				tapeLibraryManager.load(tapelibraryName, toBeUsedStorageElementNo, toBeMappedDataTransferElementSNo);
				DriveStatusDetails driveStatusDetails = tapeDriveManager.getDriveDetails(tapelibraryName, toBeMappedDataTransferElementSNo);
				if(driveStatusDetails.getMtStatus().isReady()){ // means drive is not empty and has the tape we loaded
					// Nothing needs to be done - continue
				}
				else {
					for (Iterator<DataTransferElement> iterator = allDrives.iterator(); iterator.hasNext();) {
						DataTransferElement nthDataTransferElement = (DataTransferElement) iterator.next();
						int nthDataTransferElementSNo = nthDataTransferElement.getsNo();
						if(nthDataTransferElementSNo == toBeMappedDataTransferElementSNo) // skipping the current drive thats getting verified
							continue;
						DriveStatusDetails nthDataTransferElementStatusDetails = tapeDriveManager.getDriveDetails(tapelibraryName, nthDataTransferElementSNo);
						if(nthDataTransferElementStatusDetails.getMtStatus().isReady()){ // means drive is not empty and has the tape we loaded
							// TODO : update the tapelibrary db accordingly...
							libraryToDriveElementAddressMap.put(toBeMappedDataTransferElementSNo, nthDataTransferElementSNo);
							break;
						}
					}
				}	
				tapeLibraryManager.unload(tapelibraryName, toBeUsedStorageElementNo, toBeMappedDataTransferElementSNo);
			}
			if(libraryToDriveElementAddressMap.size() > 0) {
				Map<String, Integer> oldSerialNumberToNewElementAddressMap = new HashMap<String, Integer>();
				List<Tapedrive> elementAddressNulledTapedriveList = new ArrayList<Tapedrive>();
				List<Tapedrive> tapedriveList = new ArrayList<Tapedrive>();
				
				Set<Integer> keySet = libraryToDriveElementAddressMap.keySet();
				for (Iterator<Integer> iterator = keySet.iterator(); iterator.hasNext();) {
					Integer toBeMappedDataTransferElementSNo = iterator.next();
					Integer existingDataTransferElementSNo = libraryToDriveElementAddressMap.get(toBeMappedDataTransferElementSNo);
					Tapedrive tapedrive = tapedriveDao.findByTapelibraryNameAndElementAddress(tapelibraryName, existingDataTransferElementSNo);
					oldSerialNumberToNewElementAddressMap.put(tapedrive.getSerialNumber(), toBeMappedDataTransferElementSNo); 
					tapedrive.setElementAddress(null);
					elementAddressNulledTapedriveList.add(tapedrive);
				}
				tapedriveDao.saveAll(elementAddressNulledTapedriveList);
				
				for (Tapedrive tapedrive : elementAddressNulledTapedriveList) {
					tapedrive.setElementAddress(oldSerialNumberToNewElementAddressMap.get(tapedrive.getSerialNumber()));
					tapedriveList.add(tapedrive);
				}
				tapedriveDao.saveAll(tapedriveList);
			}
		}
		catch (Exception e) {
			throw e;
		}
	}
}
