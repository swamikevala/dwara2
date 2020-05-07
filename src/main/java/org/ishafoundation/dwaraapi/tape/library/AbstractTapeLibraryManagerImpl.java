package org.ishafoundation.dwaraapi.tape.library;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.constants.TapedriveStatus;
import org.ishafoundation.dwaraapi.db.dao.master.TapedriveDao;
import org.ishafoundation.dwaraapi.db.dao.master.TapelibraryDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.db.model.master.Tapelibrary;
import org.ishafoundation.dwaraapi.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.tape.drive.status.DriveStatusDetails;
import org.ishafoundation.dwaraapi.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.tape.library.components.StorageElement;
import org.ishafoundation.dwaraapi.tape.library.status.MtxStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractTapeLibraryManagerImpl implements TapeLibraryManager{
	
	private static Logger logger = LoggerFactory.getLogger(AbstractTapeLibraryManagerImpl.class);
	
	@Autowired
	private TapelibraryDao tapelibraryDao;
	
	@Autowired
	private TapedriveDao tapedriveDao;		
	
	@Autowired
	private TapeDriveManager tapeDriveManager;

	@Override
	public Iterable<Tapelibrary> getAllTapeLibraries() {
		return tapelibraryDao.findAll();
	}

	@Override	
	public List<DataTransferElement> getAllDrivesList(String tapeLibraryName){
		MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
		return mtxStatus.getDteList();
	}
	
	@Override
	public List<DriveStatusDetails> getAvailableDrivesList() {
		List<Tapelibrary> tapelibraryList = (List<Tapelibrary>) getAllTapeLibraries();
		List<DriveStatusDetails> availablDriveDetailsList = new ArrayList<DriveStatusDetails>();
		for (Tapelibrary tapelibrary : tapelibraryList) {

			String tapelibraryName = tapelibrary.getName();
			availablDriveDetailsList.addAll(getAvailableDrivesList(tapelibraryName));
		}
		return availablDriveDetailsList;
	}

	@Override
	public List<DriveStatusDetails> getAvailableDrivesList(String tapeLibraryName){
		MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
		return getAvailableDrivesList(tapeLibraryName, mtxStatus);
	}	
	
	@Override
	public List<StorageElement> getAllStorageElementsList(String tapeLibraryName) {
		MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
		return mtxStatus.getSeList();
	}
	
	@Override
	public List<String> getAllLoadedTapesInTheLibraries(){
		List<Tapelibrary> tapelibraryList = (List<Tapelibrary>) getAllTapeLibraries();
		List<String> tapeList = new ArrayList<String>();
		for (Tapelibrary tapelibrary : tapelibraryList) {

			String tapelibraryName = tapelibrary.getName();
			tapeList.addAll(getAllLoadedTapesInTheLibrary(tapelibraryName));
		}
		return tapeList;
	}
	
	@Override
	public List<String> getAllLoadedTapesInTheLibrary(String tapeLibraryName){
		MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
		List<String> tapeList = new ArrayList<String>();
		
		List<DataTransferElement> dteList = mtxStatus.getDteList();
		for (DataTransferElement nthDataTransferElement : dteList) {
			String vt = nthDataTransferElement.getVolumeTag();
			if(vt != null)
				tapeList.add(vt);
		}
		
		List<StorageElement> seList = mtxStatus.getSeList();
		for (StorageElement nthStorageElement : seList) {
			String vt = nthStorageElement.getVolumeTag();
			if(vt != null)
				tapeList.add(vt);
		}
		
		return tapeList;
	}
	
	@Override
	public synchronized boolean locateAndLoadTapeOnToDrive(String toBeUsedTapeBarcode, String tapeLibraryName, int toBeUsedDataTransferElementSNo) throws Exception{
		boolean isSuccess = false;
		try {
			MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
			logger.trace("getting details of drive " + toBeUsedDataTransferElementSNo);
			
			DriveStatusDetails driveStatusDetails = tapeDriveManager.getDriveDetails(tapeLibraryName, toBeUsedDataTransferElementSNo);
			driveStatusDetails.setTapelibraryName(tapeLibraryName);
			if(driveStatusDetails.getMtStatus().isReady()){ // means drive is not empty and has another tape - so we need to unload the other tape
				logger.trace(toBeUsedDataTransferElementSNo + " is not empty and has another tape - so we need to unload the other tape");
				if(!driveStatusDetails.getMtStatus().isBusy()) {
					logger.trace("Unloading ");
					unload(tapeLibraryName, mtxStatus.getDte(toBeUsedDataTransferElementSNo).getStorageElementNo(), toBeUsedDataTransferElementSNo);
					logger.trace("Unload successful ");
				}else {
					logger.trace("Something wrong with the logic. Drive is not supposed to be busy, but seems busy");
				}
			}
	
			// locate in which slot the volume is...
			List<StorageElement> seList = mtxStatus.getSeList();
			logger.trace("Now locating in which slot the volume to be used is ..." + seList);
			int storageElementNo = locateTheTapesStorageElement(seList, toBeUsedTapeBarcode);
	
			// load the volume in the passed slot to the passed dte
			logger.trace("Now loading " + toBeUsedTapeBarcode + " from " + storageElementNo + " and loading into drive " + toBeUsedDataTransferElementSNo);
			load(tapeLibraryName, storageElementNo, toBeUsedDataTransferElementSNo);
			isSuccess = true;
		}
		catch (Exception e) {
			isSuccess = false;
			logger.error(e.getMessage());e.printStackTrace();
			throw e;
		}
		return isSuccess;
	}

	protected abstract MtxStatus getMtxStatus(String tapeLibraryName);
	
	private List<DriveStatusDetails> getAvailableDrivesList(String tapeLibraryName, MtxStatus mtxStatus){
		List<DriveStatusDetails> availablDriveDetailsList = new ArrayList<DriveStatusDetails>();
		List<DataTransferElement> dteList = mtxStatus.getDteList();
		logger.trace("All drives list " +  dteList);
		for (Iterator<DataTransferElement> iterator = dteList.iterator(); iterator.hasNext();) {
			DataTransferElement nthDataTransferElement = (DataTransferElement) iterator.next();
			int dataTransferElementSNo = nthDataTransferElement.getsNo(); 
			DriveStatusDetails dsd = tapeDriveManager.getDriveDetails(tapeLibraryName, dataTransferElementSNo);
			dsd.setTapelibraryName(tapeLibraryName);
			dsd.setDte(nthDataTransferElement);
			if(!dsd.getMtStatus().isBusy()) { // only not busy drives are candidates
				logger.trace("Drive " + dataTransferElementSNo + " is available");
				logger.trace("Double verifying if the drive is not been used indeed");
				// we flag a tapedrive as busy after job is selected for the drive even before the job deals with the drive... The below is take care of such usecase...
				Tapedrive tapedrive = tapedriveDao.findByTapelibraryNameAndElementAddress(tapeLibraryName, dataTransferElementSNo);
				String tapedriveStatus = tapedrive.getStatus();
				if(!tapedriveStatus.equals(TapedriveStatus.AVAILABLE.toString())){
					logger.info("Tapedrive table's status for element address " + dataTransferElementSNo + " is flagged as being used by " + tapedrive.getId());
					//logger.trace("If the job " + tapedrive.getId() + " is already complete/failed, then table seems to be out of sync with the physical drive. Something unexpected must have happened. Please check out and sync the table manually with the physical drive's status");
					continue;
				}
				logger.trace("Drive " + dataTransferElementSNo + " is added to the available drives list");
				availablDriveDetailsList.add(dsd);
			} else {
				
			}
		}
		logger.debug("Available drives list " +  dteList);
		return availablDriveDetailsList;
	}
	
	private int locateTheTapesStorageElement(List<StorageElement> seList, String toBeUsedTapeBarCode){
		int storageElementNo = -9;
		logger.trace("seList " + seList);
		logger.trace("toBeUsedVolume.getCode() " + toBeUsedTapeBarCode);
		for (Iterator<StorageElement> iterator = seList.iterator(); iterator.hasNext();) {
			StorageElement nthStorageElement = (StorageElement) iterator.next();
			logger.trace("nthStorageElement.getVolumeTag() " + nthStorageElement.getVolumeTag());
			if(nthStorageElement.getVolumeTag() != null && nthStorageElement.getVolumeTag().equals(toBeUsedTapeBarCode)) {
				storageElementNo = nthStorageElement.getsNo();
				logger.trace("storageElementNo " + storageElementNo);
			}
		}

		if(storageElementNo == -9) {
			logger.error("Tape not inside the library");
			// TODO : Handle this...
		}
		logger.trace(toBeUsedTapeBarCode + " is in " +  storageElementNo);
		return storageElementNo;
	}
}
