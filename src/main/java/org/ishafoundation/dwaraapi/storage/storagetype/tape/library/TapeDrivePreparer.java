package org.ishafoundation.dwaraapi.storage.storagetype.tape.library;

import java.util.HashMap;
import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.Tapelibrary;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveStatusDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.DataTransferElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TapeDrivePreparer {
	
	Logger logger = LoggerFactory.getLogger(TapeDrivePreparer.class);

	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@Value("${tapedrivemapping.pollinterval}")
	private long waitInterval;
	
	public HashMap<Tapelibrary,List<DataTransferElement>> prepareAllTapeDrivesForBlockingJobs() {

		logger.debug("Preparing all drives for blocking jobs like format/map");
		HashMap<Tapelibrary, List<DataTransferElement>> tapelibrary_dteList_map = new HashMap<Tapelibrary, List<DataTransferElement>>();
		
		// this should be for multiple tape libraries... lets not assume single tape library...
		List<Tapelibrary> tapelibraryList = (List<Tapelibrary>) tapeLibraryManager.getAllTapeLibraries();
		for (Tapelibrary tapelibrary : tapelibraryList) {
			String tapelibraryName = tapelibrary.getName();
			logger.debug("Now checking if any drive is busy doing stuff on " + tapelibraryName);
			boolean isAllDrivesAvailable = false;	
			while(!isAllDrivesAvailable) {
				List<DataTransferElement> allDrives = tapeLibraryManager.getAllDrivesList(tapelibraryName); 
				List<DriveStatusDetails> allAvailableDrives = tapeLibraryManager.getAvailableDrivesList(tapelibrary.getName());
						
				if(allAvailableDrives.size() == allDrives.size()) { // means all drives are available in the library... and so safe to continue...
					isAllDrivesAvailable = true;
					logger.debug("All drives available, now unloading them all");
					// unload all the drives
					for (DataTransferElement dataTransferElement : allDrives) {
						if(!dataTransferElement.isEmpty()) {
							int toBeUsedDataTransferElementSNo = dataTransferElement.getsNo();
							int toBeUsedStorageElementNo = dataTransferElement.getStorageElementNo();
							
							tapeLibraryManager.unload(tapelibraryName, toBeUsedStorageElementNo, toBeUsedDataTransferElementSNo);
							logger.debug("Unloaded drive " + toBeUsedDataTransferElementSNo);
						}
					}
					logger.debug("All drives unload complete");
					tapelibrary_dteList_map.put(tapelibrary, allDrives);
				}
				else {
					try {
						logger.debug("Some drive(s) are busy. Will wait " + waitInterval);
						Thread.sleep(waitInterval);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}				
			}
		}
		
		return tapelibrary_dteList_map;
	}

}
