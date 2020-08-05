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

	// Multiple tape libraries are not needed to be supported down stream, so we are not passing library specific details...
	public List<DriveDetails> getAllAvailableDrivesDetails(){

		HashMap<Integer, DataTransferElement> driveAutoloaderAddress_DataTransferElement_Map = new HashMap<Integer, DataTransferElement>();
		
		List<DriveDetails> driveDetailsList = new ArrayList<DriveDetails>();
		List<Device> tapelibraryDeviceList = deviceDao.findAllByDevicetype(Devicetype.tape_autoloader);
		List<Device> tapedriveDeviceList = deviceDao.findAllByDevicetype(Devicetype.tape_drive);
		
		
		for (Device tapelibrary : tapelibraryDeviceList) { // iterating through all libraries configured in dwara app
			int tapelibraryId = tapelibrary.getId();
			String tapelibraryName = tapelibrary.getUid();
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
				int tapedriveDeviceId = tapedriveDevice.getId();
				String dataTransferElementName = tapedriveDevice.getUid();
				Integer driveAutoloaderAddress = tapedriveDevice.getDetails().getAutoloader_address();
				if(tapedriveDevice.getDetails().getAutoloader_id() == tapelibraryId) { // equivalent of calling the query devicetype=drive and details.autoloader_id=X, query NOT easy with json
					TActivedevice tActivedevice = tActivedeviceDao.findByDeviceId(tapedriveDeviceId);
					if(tActivedevice == null) {
						logger.error("Tactivedevice is not having the device " + tapedriveDeviceId + " configured");
						// TODO throw new Exception("Tactivedevice is not having the device " + tapedriveDevice.getId());
					}else {
						if(tActivedevice.getDeviceStatus() == DeviceStatus.AVAILABLE) {// means available
							// verify once again
							DriveDetails driveDetails = null;
							try {
								driveDetails = tapeDriveManager.getDriveDetails(dataTransferElementName);
							} catch (Exception e) {
								logger.error("Unable to get tape drive details " + dataTransferElementName);
								continue;
							}
	
							if(driveDetails != null && driveDetails.getMtStatus().isBusy()) {
								logger.error("Something wrong. Driver' mt status and dwara's tactivedevice not in sync");
								continue;
							}
							
							// Attaching the real tape library and drive details
							driveDetails.setTapelibraryName(tapelibraryName);
							DataTransferElement dte = driveAutoloaderAddress_DataTransferElement_Map.get(driveAutoloaderAddress);
							driveDetails.setDte(dte);
							
							driveDetailsList.add(driveDetails);
						}
					}
				}
			}
		}
		return driveDetailsList;
	}
	
	public List<DriveDetails> prepareAllTapeDrivesForBlockingJobs(){

		logger.debug("Preparing all drives for blocking jobs like format/map drives");
		HashMap<Integer, DataTransferElement> driveAutoloaderAddress_DataTransferElement_Map = new HashMap<Integer, DataTransferElement>();
		
		List<DriveDetails> driveDetailsList = new ArrayList<DriveDetails>();
		List<Device> tapelibraryDeviceList = deviceDao.findAllByDevicetype(Devicetype.tape_autoloader);
		List<Device> tapedriveDeviceList = deviceDao.findAllByDevicetype(Devicetype.tape_drive);
		
		
		for (Device tapelibrary : tapelibraryDeviceList) { // iterating through all libraries configured in dwara app
			int tapelibraryId = tapelibrary.getId();
			String tapelibraryName = tapelibrary.getUid();
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
				int tapedriveDeviceId = tapedriveDevice.getId();
				String dataTransferElementName = tapedriveDevice.getUid();
				Integer driveAutoloaderAddress = tapedriveDevice.getDetails().getAutoloader_address();
				if(tapedriveDevice.getDetails().getAutoloader_id() == tapelibraryId) { // equivalent of calling the query devicetype=drive and details.autoloader_id=X, query NOT easy with json
					boolean isDriveAvailable = false;	
					while(!isDriveAvailable) { // should we keep looping or iterate for only a specific number of times like 25 that matches the maximum time that a drive can occupy a tape...
						TActivedevice tActivedevice = tActivedeviceDao.findByDeviceId(tapedriveDeviceId);
						if(tActivedevice == null) {
							logger.error("Tactivedevice is not having the device " + tapedriveDeviceId + " configured");
							// TODO throw new Exception("Tactivedevice is not having the device " + tapedriveDevice.getId());
						}else {
							if(tActivedevice.getDeviceStatus() == DeviceStatus.AVAILABLE) {// means available
								isDriveAvailable = true;
	
								// verify once again
								DriveDetails driveDetails = null;
								try {
									driveDetails = tapeDriveManager.getDriveDetails(dataTransferElementName);
								} catch (Exception e) {
									logger.error("Unable to get tape drive details " + dataTransferElementName);
									continue;
								}
		
								if(driveDetails != null && driveDetails.getMtStatus().isBusy()) {
									logger.error("Something wrong. Driver' mt status and dwara's tactivedevice not in sync");
									continue;
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
			}
		}
		return driveDetailsList;
	}
}
