package org.ishafoundation.dwaraapi.tape.library;

import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.TapelibraryDao;
import org.ishafoundation.dwaraapi.db.model.master.Tapelibrary;
import org.ishafoundation.dwaraapi.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.tape.drive.status.DriveStatusDetails;
import org.ishafoundation.dwaraapi.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.tape.library.components.StorageElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeDriveMapper {

	@Autowired
	private TapelibraryDao tapelibraryDao;
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;

	@Autowired
	private TapeDriveManager tapeDriveManager;

	public void mapDrives() {

		// this should be for multiple tape libraries... lets not assume single tape library...
		List<Tapelibrary> tapelibraryList = (List<Tapelibrary>) tapeLibraryManager.getAllTapeLibraries();
		for (Tapelibrary tapelibrary : tapelibraryList) {
			// check if any drive is busy doing stuff...
			// if yes throw message saying "drive busy"...
			// if no continue


			// Decision - To leave it to the user to decide 
			// if (s)he should let all currently queued jobs be processed 
			// OR...
			// cancel the queued storage jobs
			// if tapelibrary and drive details are not synced jobs fail, so better do the latter..

			// so no extra checks on job is required in here... 

			// TODO but when this api runs we need to ensure no queued jobs are taken up for processing

			//		List<DataTransferElement> allDrives = tapeLibraryManager.getAllDrivesList(tapelibrary.getName());
			String tapelibraryName = tapelibrary.getName();
			int tapeLibraryId = getTapeLibraryId(tapelibraryName);
			List<DataTransferElement> allDrives = tapeLibraryManager.getAllDrivesList(tapelibraryName); // Need to pass tapelibrary name here...
			//		List<DriveStatusDetails> allAvailableDrives = tapeLibraryManager.getAvailableDrivesList(tapelibrary.getName());
			//		
			//		if(allAvailableDrives.size() == allDrives.size()) { // means all drives are available
			//			
			/*
			getDrivesList()
			iterate the drives
				unload
			iterate the drives
				load a tape on to drive i
				!~! mt status the already mapped devicewwid and check if drive status online meaning tape is loaded
				if not loop the drivelist
					check !~!
					if true skip loop else continue
				Update tapedrive or hold the details in collection and update the table later...
			 */	

			for (DataTransferElement dataTransferElement : allDrives) {
				int toBeUsedDataTransferElementSNo = dataTransferElement.getsNo();
				int toBeUsedStorageElementNo = dataTransferElement.getStorageElementNo();
				tapeLibraryManager.unload(tapelibraryName, toBeUsedStorageElementNo, toBeUsedDataTransferElementSNo);
			}
			int toBeUsedStorageElementNo = 0;
			List<StorageElement> storageElementsList = tapeLibraryManager.getAllStorageElementsList(tapelibraryName);
			for (StorageElement storageElement : storageElementsList) {
				if(!storageElement.isEmpty()) {
					toBeUsedStorageElementNo = storageElement.getsNo();
					break;
				}
			}
			for (DataTransferElement dataTransferElement : allDrives) {
				int toBeUsedDataTransferElementSNo = dataTransferElement.getsNo();
				tapeLibraryManager.load(tapelibraryName, toBeUsedStorageElementNo, toBeUsedDataTransferElementSNo);
				DriveStatusDetails driveStatusDetails = tapeDriveManager.getDriveDetails(tapeLibraryId, toBeUsedDataTransferElementSNo);
				if(driveStatusDetails.getMtStatus().isReady()){ // means drive is not empty and has the tape we loaded
					// Nothing needs to be done - continue
				}
				else {
					for (Iterator<DataTransferElement> iterator = allDrives.iterator(); iterator.hasNext();) {
						DataTransferElement nthDataTransferElement = (DataTransferElement) iterator.next();
						int nthDataTransferElementSNo = nthDataTransferElement.getsNo();
						if(nthDataTransferElementSNo == toBeUsedDataTransferElementSNo) // skipping the current drive thats getting verified
							continue;
						DriveStatusDetails nthDataTransferElementStatusDetails = tapeDriveManager.getDriveDetails(tapeLibraryId, nthDataTransferElementSNo);
						if(nthDataTransferElementStatusDetails.getMtStatus().isReady()){ // means drive is not empty and has the tape we loaded
							// Nothing needs to be done - continue
							// TODO : update the tapelibrary db accordingly...
						}
					}
				}	
			}

			//		}
			//		else {
			//			// return back a message to try later
			//		}
		}
	}


	private int getTapeLibraryId(String tapeLibraryName) {
		Tapelibrary tapelibrary = tapelibraryDao.findByName(tapeLibraryName);
		return tapelibrary.getId();
	}
}
