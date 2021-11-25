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
	
	public List<DriveDetails> getAllDrivesDetails() throws Exception{
		List<DriveDetails> driveDetailsList = iterateDrives("getAllDrivesDetails");
		logger.trace("Successfully able to retrieve details for all configured drives...");
		for (DriveDetails driveDetails : driveDetailsList) {
			logger.trace(driveDetails.getDriveId());
		}
		return driveDetailsList;
	}
	
	public List<DriveDetails> getAllAvailableDrivesDetails() throws Exception{
		List<DriveDetails> driveDetailsList = iterateDrives("getAllAvailableDrivesDetails");
		logger.trace("Successfully able to retrieve details for all available drives...");
		for (DriveDetails driveDetails : driveDetailsList) {
			logger.trace(driveDetails.getDriveId());
		}
		return driveDetailsList;
	}
	
	public List<DriveDetails> prepareAllTapeDrivesForBlockingJobs() throws Exception{
		List<DriveDetails> driveDetailsList = iterateDrives("prepareAllTapeDrivesForBlockingJobs");
		logger.trace("Successfully able to prepare and retrieve details for the below drives...");
		for (DriveDetails driveDetails : driveDetailsList) {
			logger.trace(driveDetails.getDriveId());
		}
		return driveDetailsList;
	}
	
	// Multiple tape libraries are not needed to be supported down stream, so we are not passing library specific details...
	private List<DriveDetails> iterateDrives(String taskName) throws Exception{

		HashMap<Integer, DataTransferElement> driveAutoloaderAddress_DataTransferElement_Map = new HashMap<Integer, DataTransferElement>();
		
		List<DriveDetails> driveDetailsList = new ArrayList<DriveDetails>();
		List<Device> tapelibraryDeviceList = deviceDao.findAllByTypeAndStatusAndDefectiveIsFalse(Devicetype.tape_autoloader, DeviceStatus.online);
		List<Device> tapedriveDeviceList = deviceDao.findAllByTypeAndStatusAndDefectiveIsFalse(Devicetype.tape_drive, DeviceStatus.online);
		
		
		for (Device tapelibrary : tapelibraryDeviceList) { // iterating through all libraries configured in dwara app
			String tapelibraryId = tapelibrary.getId();
			String tapelibraryName = tapelibrary.getWwnId();
			
			List<DataTransferElement> dteList = null;
			try {
				dteList = tapeLibraryManager.getAllDataTransferElements(tapelibraryName);
			}catch (Exception e) {
				logger.error("Unable to get tape library details for " + tapelibraryName);
				continue;
			}
			
			logger.trace("Following drives are in phyiscal tape library " + tapelibraryId);
			for (DataTransferElement dte : dteList) {
				logger.trace("Drive's AutoloaderAddress - " + dte.getsNo());
				driveAutoloaderAddress_DataTransferElement_Map.put(dte.getsNo(), dte);
			}
			
			for (Device tapedriveDevice : tapedriveDeviceList) {
				String tapedriveDeviceId = tapedriveDevice.getId();
				String dataTransferElementName = tapedriveDevice.getWwnId();
				Integer driveAutoloaderAddress = tapedriveDevice.getDetails().getAutoloaderAddress();
//				DataTransferElement dte = driveAutoloaderAddress_DataTransferElement_Map.get(driveAutoloaderAddress);
//				if(dte == null) {
//					logger.info("Skipping getting details for drive " + tapedriveDeviceId + " as its not mapped properly");
//					continue;
//				}
				String tapedriveDeviceStoragesubtype = tapedriveDevice.getDetails().getType();
				if(tapelibraryId.equals(tapedriveDevice.getDetails().getAutoloaderId())) { // equivalent of calling the query devicetype=drive and details.autoloader_id=X, query NOT easy with json
					logger.trace("Getting details for - " + tapedriveDeviceId);
					DriveDetails driveDetails = null;
					try {
						if(taskName.equals("getAllDrivesDetails")) {
							logger.trace("Getting Drives Details");
							driveDetails = getDriveDetails(tapelibraryName, tapedriveDeviceId, dataTransferElementName, driveAutoloaderAddress, driveAutoloaderAddress_DataTransferElement_Map);
						}
						else if(taskName.equals("getAllAvailableDrivesDetails")) {
							logger.trace("Getting Drives Details If Available");
							driveDetails = getDriveDetailsIfAvailable(tapelibraryName, tapedriveDeviceId, dataTransferElementName, driveAutoloaderAddress, driveAutoloaderAddress_DataTransferElement_Map);
						}
						else {
							logger.trace("Preparing Drive For Blocking Jobs");
							driveDetails = prepareDriveForBlockingJobs(tapelibraryName, tapedriveDeviceId, dataTransferElementName, driveAutoloaderAddress, driveAutoloaderAddress_DataTransferElement_Map);
						}
					} catch (Exception e) {
						logger.error(e.getMessage() + " Skipping getting details for - " + tapedriveDeviceId, e);
						continue;
					}
					
					if(driveDetails != null) {
						driveDetails.setDriveStoragesubtype(tapedriveDeviceStoragesubtype);
						driveDetailsList.add(driveDetails);
					}
				}
			}
		}
		if(driveDetailsList.size() == 0)
			throw new Exception("No drive available");
		return driveDetailsList;
	}

	private DriveDetails getDriveDetails(String tapelibraryName, String tapedriveDeviceId, String dataTransferElementName, Integer driveAutoloaderAddress, HashMap<Integer, DataTransferElement>  driveAutoloaderAddress_DataTransferElement_Map) throws Exception{
		DriveDetails driveDetails = null;
		try {
			driveDetails = tapeDriveManager.getDriveDetails(dataTransferElementName);
		} catch (Exception e) {
			logger.error(dataTransferElementName + "(device - " + tapedriveDeviceId + ") seems to be offline, but is marked online in Dwara. Resetting its status to " + DeviceStatus.not_found.name()); 
			Device device = deviceDao.findById(tapedriveDeviceId).get(); 
			device.setStatus(DeviceStatus.not_found);
			deviceDao.save(device);
			
			throw new Exception("Unable to get tape drive details " + dataTransferElementName);
		}
		
		// Adding tape library and other details
		driveDetails.setTapelibraryName(tapelibraryName);
		driveDetails.setDriveId(tapedriveDeviceId);
		DataTransferElement dte = driveAutoloaderAddress_DataTransferElement_Map.get(driveAutoloaderAddress);
		driveDetails.setDte(dte);
		return driveDetails;
	}
	
	private DriveDetails getDriveDetailsIfAvailable(String tapelibraryName, String tapedriveDeviceId, String dataTransferElementName, Integer driveAutoloaderAddress, HashMap<Integer, DataTransferElement>  driveAutoloaderAddress_DataTransferElement_Map) throws Exception{
		TActivedevice tActivedevice = tActivedeviceDao.findByDeviceId(tapedriveDeviceId);
		DriveDetails driveDetails = null;
		if(tActivedevice == null) {// means available
			// verify once again
			driveDetails = getDriveDetails(tapelibraryName, tapedriveDeviceId, dataTransferElementName, driveAutoloaderAddress, driveAutoloaderAddress_DataTransferElement_Map);
		
			if(driveDetails != null && driveDetails.getMtStatus().isBusy()) {
				throw new Exception("Something wrong. Driver " + dataTransferElementName + " mt status and dwara's tactivedevice not in sync");
			}
		} else {
			logger.trace(tapedriveDeviceId + " flagged as busy in dwara");
		}
		return driveDetails;
	}
	
	private DriveDetails prepareDriveForBlockingJobs(String tapelibraryName, String tapedriveDeviceId, String dataTransferElementName, Integer driveAutoloaderAddress, HashMap<Integer, DataTransferElement>  driveAutoloaderAddress_DataTransferElement_Map) throws Exception{	
		DriveDetails driveDetails = null;
		boolean isBusy = true;
		while(isBusy){//
			TActivedevice tActivedevice = tActivedeviceDao.findByDeviceId(tapedriveDeviceId);
			if(tActivedevice == null) {	
				isBusy = false; // available...
				// verify once again
				driveDetails = getDriveDetails(tapelibraryName, tapedriveDeviceId, dataTransferElementName, driveAutoloaderAddress, driveAutoloaderAddress_DataTransferElement_Map);
		
				if(driveDetails != null && driveDetails.getMtStatus().isBusy()) {
					throw new Exception("Something wrong. Driver " + dataTransferElementName + " mt status and dwara's tactivedevice not in sync");
				}
				
				DataTransferElement dataTransferElement = driveAutoloaderAddress_DataTransferElement_Map.get(driveAutoloaderAddress);
				if(!dataTransferElement.isEmpty()) {
					logger.info("Available drive has a tape loaded already. so unloading it");
					int toBeUsedDataTransferElementSNo = dataTransferElement.getsNo();
				
					try {
						tapeLibraryManager.unload(tapelibraryName, toBeUsedDataTransferElementSNo);
					} catch (Exception e) {
						String errMsg = "Blocking jobs need to unload all tapes from drive. Unable to unload " + tapelibraryName + ":" + toBeUsedDataTransferElementSNo + " :: " + e.getMessage();
						logger.error(errMsg);
						throw new Exception(errMsg);
					}
					logger.debug("Unloaded drive " + toBeUsedDataTransferElementSNo);
				}
				// Adding tape library and other details
				driveDetails.setTapelibraryName(tapelibraryName);
				driveDetails.setDriveId(tapedriveDeviceId);
				DataTransferElement dte = driveAutoloaderAddress_DataTransferElement_Map.get(driveAutoloaderAddress);
				driveDetails.setDte(dte);
			}else {
				try {
					logger.info(tapedriveDeviceId + " Drive busy. Will wait " + waitInterval);
					Thread.sleep(waitInterval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}								
			}
		}
		return driveDetails;
	}
}
