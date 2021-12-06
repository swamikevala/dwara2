package org.ishafoundation.dwaraapi.storage.storagetype.tape.library;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.StorageElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.status.MtxStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractTapeLibraryManagerImpl implements TapeLibraryManager{
	
	private static Logger logger = LoggerFactory.getLogger(AbstractTapeLibraryManagerImpl.class);
	
	@Autowired
	private TapeDriveManager tapeDriveManager;

	public List<DataTransferElement> getAllDataTransferElements(String tapeLibraryName) throws Exception{
		MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
		return mtxStatus.getDteList();
	}
	
	public List<StorageElement> getAllStorageElements(String tapeLibraryName) throws Exception {
		MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
		return mtxStatus.getSeList();
	}
	  
	public List<TapeOnLibrary> getAllLoadedTapesInTheLibrary(String tapeLibraryName) throws Exception{
		logger.trace("getAllLoadedTapesInTheLibrary for " + tapeLibraryName);
		MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
		logger.trace("mtx call complete for " + tapeLibraryName);
		List<TapeOnLibrary> tapeOnLibraryList = new ArrayList<TapeOnLibrary>();
		
		List<DataTransferElement> dteList = mtxStatus.getDteList();
		for (DataTransferElement nthDataTransferElement : dteList) {
			String vt = nthDataTransferElement.getVolumeTag();
			if(vt != null) {
				TapeOnLibrary tol = new TapeOnLibrary();
				tol.setAddress(nthDataTransferElement.getsNo());
				tol.setLoaded(true);
				tol.setVolumeTag(vt);
				tapeOnLibraryList.add(tol);
			}
		}
		
		List<StorageElement> seList = mtxStatus.getSeList();
		for (StorageElement nthStorageElement : seList) {
			String vt = nthStorageElement.getVolumeTag();
			if(vt != null) {
				TapeOnLibrary tol = new TapeOnLibrary();
				tol.setAddress(nthStorageElement.getsNo());
				tol.setLoaded(false);
				tol.setVolumeTag(vt);
				tapeOnLibraryList.add(tol);
			}
		}
		logger.trace("getAllLoadedTapesInTheLibrary completed for " + tapeLibraryName);
		return tapeOnLibraryList;
	}

	public synchronized boolean locateAndLoadTapeOnToDrive(String toBeUsedTapeBarcode, String tapeLibraryName, int toBeUsedDataTransferElementSNo, String dataTransferElementName) throws Exception{
		boolean isSuccess = false;
		try {
			MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
			logger.trace("getting details of drive " + toBeUsedDataTransferElementSNo);
			
			DriveDetails driveStatusDetails = getDriveDetails(toBeUsedDataTransferElementSNo, dataTransferElementName, 0);
			
			if(driveStatusDetails.getMtStatus().isReady()){ // means drive is not empty and has a tape
				DataTransferElement dte = mtxStatus.getDte(toBeUsedDataTransferElementSNo);
				String alreadyLoadedVolumeTag = dte.getVolumeTag();
				if(alreadyLoadedVolumeTag.equals(toBeUsedTapeBarcode)) { // drive is already loaded with the needed tape
					logger.trace("Drive " + toBeUsedDataTransferElementSNo + " is already loaded with the needed tape");
					return true;
				}
				logger.trace(toBeUsedDataTransferElementSNo + " is not empty and has another tape - so we need to unload the other tape");
				logger.trace("Unloading ");
				unload(tapeLibraryName, toBeUsedDataTransferElementSNo);
				logger.trace("Unload successful ");
			}
	
			int storageElementNo = locateTape(toBeUsedTapeBarcode, mtxStatus);
	
			// load the volume in the passed slot to the passed dte
			logger.trace("Now loading " + toBeUsedTapeBarcode + " from " + storageElementNo + " and loading into drive " + toBeUsedDataTransferElementSNo);
			load(tapeLibraryName, storageElementNo, toBeUsedDataTransferElementSNo);
			isSuccess = true;
		}
		catch (Exception e) {
			isSuccess = false;
			logger.error(e.getMessage(), e);
			throw e;
		}
		return isSuccess;
	}
	
	public synchronized int locateTape(String toBeUsedTapeBarcode, String tapeLibraryName) throws Exception{
		MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
		return locateTape(toBeUsedTapeBarcode, mtxStatus);
	}
	
	public synchronized int locateTape(String toBeUsedTapeBarcode, MtxStatus mtxStatus) throws Exception{
		// locate in which slot the volume is...
		List<StorageElement> seList = mtxStatus.getSeList();
		logger.trace("Now locating in which slot the volume to be used is ..." + seList);
		return locateTheTapesStorageElement(seList, toBeUsedTapeBarcode);
	}	
	
	
	private DriveDetails getDriveDetails(int toBeUsedDataTransferElementSNo, String dataTransferElementName, int nthRetryAttempt) throws Exception{
		DriveDetails driveStatusDetails = tapeDriveManager.getDriveDetails(dataTransferElementName);
		if(driveStatusDetails.getMtStatus().isBusy()) {
			if(nthRetryAttempt < 3) {
				driveStatusDetails = getDriveDetails(toBeUsedDataTransferElementSNo, dataTransferElementName, nthRetryAttempt + 1);
			}
			else {
				logger.trace("Something wrong with the app and tapelibrary sync. Drive is not supposed to be busy, but seems busy");
				throw new Exception("Drive " + toBeUsedDataTransferElementSNo + "(" + dataTransferElementName + ") seems to be busy, when its not supposed to. App Bug. Contact Dev team");
			}
		}
		return driveStatusDetails;
	}
	
	
	private int locateTheTapesStorageElement(List<StorageElement> seList, String toBeUsedTapeBarCode) throws Exception{
		int storageElementNo = -9;
		logger.trace("seList " + seList);
		logger.trace("toBeUsedVolume.getCode() " + toBeUsedTapeBarCode);
		for (Iterator<StorageElement> iterator = seList.iterator(); iterator.hasNext();) {
			StorageElement nthStorageElement = (StorageElement) iterator.next();
			//logger.trace("nthStorageElement.getVolumeTag() " + nthStorageElement.getVolumeTag());
			if(nthStorageElement.getVolumeTag() != null && nthStorageElement.getVolumeTag().equals(toBeUsedTapeBarCode)) {
				storageElementNo = nthStorageElement.getsNo();
				//logger.trace("storageElementNo " + storageElementNo);
			}
		}

		if(storageElementNo == -9) {
			String msg = toBeUsedTapeBarCode + " Tape not inside the library slots";
			logger.error(msg);
			throw new Exception(msg);
		}
		logger.trace(toBeUsedTapeBarCode + " is in " +  storageElementNo);
		return storageElementNo;
	}
}
