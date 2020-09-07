package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.StorageElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeDriveMapper {
	
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
	public void mapDrives(String tapelibraryId, List<DriveDetails> preparedDrivesList) throws Exception {
		try {
			
			String tapelibraryName = deviceDao.findById(tapelibraryId).get().getWwnId();
			
			// Step 1 - get a tape from the storageelement that can be used to load and verify...
			int toBeUsedStorageElementNo = 0;
			List<StorageElement> storageElementsList = tapeLibraryManager.getAllStorageElements(tapelibraryName);
			for (StorageElement storageElement : storageElementsList) {
				if(!storageElement.isEmpty()) {
					toBeUsedStorageElementNo = storageElement.getsNo();
					break;
				}
			}
			
			/*
			blank
			
			Drive full
			
			load - drive full 
			
			readlabel - dont have to rewind - 
			
			No device found 
			
			launch map drive
			
			Tape job process - UI - blocking job - Initialize - Map Drive
			stinit
				retry
			launch map drive job
				1 - dwara - user 
				requestid and jobid
			
				
			Request.
				request_id
				job_id
				Rerun_no
			
			*/	
			
			/*
			Step 2 - load the tape on drives and verify
				
				load a tape on to drive i
				!~! mt status the already mapped devicewwid and check if drive status online meaning tape is loaded
				if not loop the drivelist
					check !~!
					if true skip loop else continue
				iterate to next drive
				
				Update tapedrive or hold the details in collection and update the table later...
				unload the tape from drive i
				
			*/
//			0 - 2
//			1 - 3
//			2 - 1
//			3 = 0 so is it ok just to iterate n-1 times as the last drive to be mapped will be the left out no? Safe to do the load and verify if the drive is indeed working or offline etc... so not avoiding the last call...   
			Map<Integer, Integer> libraryToDriveElementAddressMap = new HashMap<Integer, Integer>();
			for (DriveDetails nthDriveDetails : preparedDrivesList) {
				String driveId = nthDriveDetails.getDriveId();
				int toBeMappedDataTransferElementSNo = nthDriveDetails.getDte().getsNo();
				tapeLibraryManager.load(tapelibraryName, toBeUsedStorageElementNo, toBeMappedDataTransferElementSNo);
				DriveDetails driveStatusDetails = tapeDriveManager.getDriveDetails(driveId);
				if(driveStatusDetails.getMtStatus().isReady()){ // means drive is not empty and has the tape we loaded
					// Nothing needs to be done - continue
				}
				else { // if not iterate through the other drives and get the drive which has the tape loaded
					for (DriveDetails xthDriveDetails : preparedDrivesList) {
						String xthDriveId = xthDriveDetails.getDriveId();
						if(driveId.equals(xthDriveId)) {
							continue; // skipping the current drive thats getting verified
						}
						DriveDetails xthDriveDetailsAfterLoad = tapeDriveManager.getDriveDetails(xthDriveId);
						if(xthDriveDetailsAfterLoad.getMtStatus().isReady()){ // means drive is not empty and has the tape we loaded
							Device deviceToBeUpdated = deviceDao.findById(xthDriveId).get();
							deviceToBeUpdated.getDetails().setAutoloaderAddress(xthDriveDetailsAfterLoad.getDte().getsNo());
							deviceDao.save(deviceToBeUpdated);
							break;
						}
					}
				}	
				tapeLibraryManager.unload(tapelibraryName, toBeUsedStorageElementNo, toBeMappedDataTransferElementSNo);
			}
		}
		catch (Exception e) {
			throw e;
		}
	}
}
