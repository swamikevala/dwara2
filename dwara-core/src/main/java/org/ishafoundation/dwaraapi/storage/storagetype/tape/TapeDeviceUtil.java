package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TActivedeviceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.ishafoundation.dwaraapi.enumreferences.DeviceStatus;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.DataTransferElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TapeDeviceUtil {
	
    private static final Logger logger = LoggerFactory.getLogger(TapeDeviceUtil.class);
	
	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private TActivedeviceDao tActivedeviceDao;
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@Autowired
	private TapeDriveManager tapeDriveManager;
	
	@Value("${tapedrivemapping.pollinterval}")
	private long waitInterval;

	public List<DriveDetails> getAllAvailableDrivesDetails() throws Exception{
		return iterateDrives("getAllAvailableDrivesDetails");
	}
	
	public List<DriveDetails> prepareAllTapeDrivesForBlockingJobs() throws Exception{
		return iterateDrives("prepareAllTapeDrivesForBlockingJobs");
	}
	
	// Multiple tape libraries are not needed to be supported down stream, so we are not passing library specific details...
	private List<DriveDetails> iterateDrives(String taskName) throws Exception{

		HashMap<Integer, DataTransferElement> driveAutoloaderAddress_DataTransferElement_Map = new HashMap<Integer, DataTransferElement>();
		
		List<DriveDetails> driveDetailsList = new ArrayList<DriveDetails>();
		List<Device> tapelibraryDeviceList = deviceDao.findAllByDevicetypeAndStatusAndDefectiveIsFalse(Devicetype.tape_autoloader, DeviceStatus.ONLINE);
		List<Device> tapedriveDeviceList = deviceDao.findAllByDevicetypeAndStatusAndDefectiveIsFalse(Devicetype.tape_drive, DeviceStatus.ONLINE);
		
		
		for (Device tapelibrary : tapelibraryDeviceList) { // iterating through all libraries configured in dwara app
			String tapelibraryId = tapelibrary.getId();
			String tapelibraryName = tapelibrary.getWwnId();
			List<DataTransferElement> dteList = null;
			try {
				dteList = tapeLibraryManager.getAllDataTransferElements(tapelibraryName); // get the drives in the real tape library
			} catch (Exception e) {
				logger.error("Unable to get tape drive details for " + tapelibraryName);
				continue;
			}
			
			for (DataTransferElement dte : dteList) {
				driveAutoloaderAddress_DataTransferElement_Map.put(dte.getsNo(), dte);
			}
			
			for (Device tapedriveDevice : tapedriveDeviceList) {
				String tapedriveDeviceId = tapedriveDevice.getId();
				String dataTransferElementName = tapedriveDevice.getWwnId();
				Integer driveAutoloaderAddress = tapedriveDevice.getDetails().getAutoloader_address();
				if(tapedriveDevice.getDetails().getAutoloader_id() == tapelibraryId) { // equivalent of calling the query devicetype=drive and details.autoloader_id=X, query NOT easy with json
						if(taskName.equals("getAllAvailableDrivesDetails")) {
							try {
								getDriveDetailsIfAvailable(tapelibraryName, tapedriveDeviceId, dataTransferElementName, driveAutoloaderAddress, driveAutoloaderAddress_DataTransferElement_Map, driveDetailsList);
							} catch (Exception e) {
								logger.error(e.getMessage());
								continue;
							}
						}
						else {
							prepareDriveForBlockingJobs(tapelibraryName, tapedriveDeviceId, dataTransferElementName, driveAutoloaderAddress, driveAutoloaderAddress_DataTransferElement_Map, driveDetailsList);
						}

				}
			}
		}
		return driveDetailsList;
	}
	
	private void getDriveDetailsIfAvailable(String tapelibraryName, String tapedriveDeviceId, String dataTransferElementName, Integer driveAutoloaderAddress, HashMap<Integer, DataTransferElement>  driveAutoloaderAddress_DataTransferElement_Map, List<DriveDetails> driveDetailsList) throws Exception{
		TActivedevice tActivedevice = tActivedeviceDao.findByDeviceId(tapedriveDeviceId);
	
		if(tActivedevice == null) {// means available
			// verify once again
			DriveDetails driveDetails = null;
			try {
				driveDetails = tapeDriveManager.getDriveDetails(dataTransferElementName);
			} catch (Exception e) {
				throw new Exception("Unable to get tape drive details " + dataTransferElementName);
			}
		
			if(driveDetails != null && driveDetails.getMtStatus().isBusy()) {
				throw new Exception("Something wrong. Driver " + dataTransferElementName + " mt status and dwara's tactivedevice not in sync");
			}
			
			// Attaching the real tape library and drive details
			driveDetails.setTapelibraryName(tapelibraryName);
			DataTransferElement dte = driveAutoloaderAddress_DataTransferElement_Map.get(driveAutoloaderAddress);
			driveDetails.setDte(dte);
			
			driveDetailsList.add(driveDetails);
		}
	}

	private void prepareDriveForBlockingJobs(String tapelibraryName, String tapedriveDeviceId, String dataTransferElementName, Integer driveAutoloaderAddress, HashMap<Integer, DataTransferElement>  driveAutoloaderAddress_DataTransferElement_Map, List<DriveDetails> driveDetailsList) throws Exception{	
		boolean isBusy = true;
		while(isBusy){//
			TActivedevice tActivedevice = tActivedeviceDao.findByDeviceId(tapedriveDeviceId);
			if(tActivedevice == null) {	
				isBusy = false; // available...
				// verify once again
				DriveDetails driveDetails = null;
				try {
					driveDetails = tapeDriveManager.getDriveDetails(dataTransferElementName);
				} catch (Exception e) {
					throw new Exception("Unable to get tape drive details " + dataTransferElementName);
				}
		
				if(driveDetails != null && driveDetails.getMtStatus().isBusy()) {
					throw new Exception("Something wrong. Driver " + dataTransferElementName + " mt status and dwara's tactivedevice not in sync");
				}
				DataTransferElement dataTransferElement = driveAutoloaderAddress_DataTransferElement_Map.get(driveAutoloaderAddress);
				if(!dataTransferElement.isEmpty()) {
					logger.debug("Drive available, and has a tape so unloading it");
					int toBeUsedDataTransferElementSNo = dataTransferElement.getsNo();
					int toBeUsedStorageElementNo = dataTransferElement.getStorageElementNo();
					
					try {
						tapeLibraryManager.unload(tapelibraryName, toBeUsedStorageElementNo, toBeUsedDataTransferElementSNo);
					} catch (Exception e) {
						logger.error("Unable to unload " + tapelibraryName + ":" + toBeUsedStorageElementNo + ":" + toBeUsedDataTransferElementSNo);
					}
					logger.debug("Unloaded drive " + toBeUsedDataTransferElementSNo);
					
					// Attaching the real tape library and drive details
					driveDetails.setTapelibraryName(tapelibraryName);
					DataTransferElement dte = driveAutoloaderAddress_DataTransferElement_Map.get(driveAutoloaderAddress);
					driveDetails.setDte(dte);
					
					driveDetailsList.add(driveDetails);
				}
		
			}else {
				try {
					logger.debug("Drive busy. Will wait " + waitInterval);
					Thread.sleep(waitInterval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}								
			}
		}
	}
}
