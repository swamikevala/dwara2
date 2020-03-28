package org.ishafoundation.dwaraapi.tape.library;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.constants.TapedriveStatus;
import org.ishafoundation.dwaraapi.db.dao.master.TapedriveDao;
import org.ishafoundation.dwaraapi.db.dao.master.TapelibraryDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.master.Tapelibrary;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.tape.drive.DriveStatusDetails;
import org.ishafoundation.dwaraapi.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.tape.library.components.StorageElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeLibraryManager{
	
	private static Logger logger = LoggerFactory.getLogger(TapeLibraryManager.class);
	
	@Autowired
	private TapelibraryDao tapelibraryDao;
	
	@Autowired
	private CommandLineExecuter commandLineExecuter;
	
	@Autowired
	private TapeDriveManager tapeDriveManager;
		
	@Autowired
	private TapedriveDao tapedriveDao;		
	
	private String tapeLibraryName = null;
	
	@PostConstruct
	private void setTapeLibraryName() {
		Iterable<Tapelibrary> tapeLibraries = tapelibraryDao.findAll();
		for (Tapelibrary tapelibrary : tapeLibraries) {
			tapeLibraryName = tapelibrary.getName();  // FIXME : Cant handle multipe tape libraries...
		}
	}
	
	public MtxStatus getMtxStatus(){
		return getMtxStatus(tapeLibraryName);
	}
	
	
	public MtxStatus getMtxStatus(String tapeLibraryName){
		String mtxStatusResponse = callMtxStatus(tapeLibraryName);
		MtxStatus mtxStatus = MtxStatusResponseParser.parseMtxStatusResponse(mtxStatusResponse);
		return mtxStatus;
	}
	
	private String callMtxStatus(String tapeLibraryName) {
		String mtxStatusResponse = null;
		String mtxStatusResponseFileName = tapeLibraryName.replace("/", "_") + "_status.err";
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("mtx -f " + tapeLibraryName + " status", mtxStatusResponseFileName);
		mtxStatusResponse = cler.getStdOutResponse();
		return mtxStatusResponse;
	}	
	
	//		unload if any other tape - we should always check the status of the drive before unloading the tape. if the drive is busy we should not unload the tape.., (At the time of writing another thread/process is allowed to unload the tape)
	public boolean unload(String tapeLibraryName, int seSNo, int driveSNo){
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("mtx -f " + tapeLibraryName + " unload " + seSNo + " " + driveSNo);
		logger.debug(cler.getStdOutResponse());
		return true;
	}
	
	//		load the tape to be used
	public boolean load(String tapeLibraryName, int seSNo, int driveSNo){
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("mtx -f " + tapeLibraryName + " load " + seSNo + " " + driveSNo);
		logger.debug(cler.getStdOutResponse());
		return true;
	}
	
	
	public List<DriveStatusDetails> getAvailableDrivesList(MtxStatus mtxStatus){
		List<DriveStatusDetails> availablDriveDetailsList = new ArrayList<DriveStatusDetails>();
		List<DataTransferElement> dteList = mtxStatus.getDteList();
		logger.trace("All drives list " +  dteList);
		for (Iterator<DataTransferElement> iterator = dteList.iterator(); iterator.hasNext();) {
			DataTransferElement nthDataTransferElement = (DataTransferElement) iterator.next();
			int dataTransferElementSNo = nthDataTransferElement.getsNo(); 
			DriveStatusDetails dsd = tapeDriveManager.getDriveDetails(dataTransferElementSNo);
			dsd.setDte(nthDataTransferElement);
			if(!dsd.getMtStatus().isBusy()) { // only not busy drives are candidates
				logger.trace("Drive " + dataTransferElementSNo + " is available");
				logger.trace("Double verifying if the drive is not been used indeed");
				// we flag a tapedrive as busy after job is selected for the drive even before the job deals with the drive... The below is take care of such usecase...
				Tapedrive tapedrive = tapedriveDao.findByElementAddress(dataTransferElementSNo);
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
	
	public List<DriveStatusDetails> getAvailableDrivesList(){
		MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
		/*
		String mtxStatusResponse = null;
	
		try {
			mtxStatusResponse = FileUtils.readFileToString(new File("C:\\Users\\prakash\\projects\\videoarchives\\bru\\POC\\BRU002\\test\\mtxResponses\\mtx_status_all_drives_available.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // will make a call to mtx and get the status realtime...
		
		mtxStatus = MtxStatusResponseParser.parseMtxStatusResponse(mtxStatusResponse);*/
		return getAvailableDrivesList(mtxStatus);
	}	
	
	public synchronized boolean loadTapeOnToDrive(Tape toBeUsedTape, int toBeUsedDataTransferElementSNo) throws Exception{
		boolean isSuccess = false;
		try {
			MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
			logger.trace("getting details of drive " + toBeUsedDataTransferElementSNo);
			DriveStatusDetails driveStatusDetails = tapeDriveManager.getDriveDetails(toBeUsedDataTransferElementSNo);
			
			if(driveStatusDetails.getMtStatus().isDriveReady()){ // means drive is not empty and has another tape - so we need to unload the other tape
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
			int storageElementNo = locateTheTapesStorageElement(seList, toBeUsedTape);
	
			// load the volume in the passed slot to the passed dte
			logger.trace("Now loading " + toBeUsedTape.getBarcode() + " from " + storageElementNo + " and loading into drive " + toBeUsedDataTransferElementSNo);
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
	
	private int locateTheTapesStorageElement(List<StorageElement> seList, Tape toBeUsedTape){
		int storageElementNo = -9;
		logger.trace("seList " + seList);
		logger.trace("toBeUsedVolume.getCode() " + toBeUsedTape.getBarcode());
		for (Iterator<StorageElement> iterator = seList.iterator(); iterator.hasNext();) {
			StorageElement nthStorageElement = (StorageElement) iterator.next();
			logger.trace("nthStorageElement.getVolumeTag() " + nthStorageElement.getVolumeTag());
			if(nthStorageElement.getVolumeTag() != null && nthStorageElement.getVolumeTag().equals(toBeUsedTape.getBarcode())) {
				storageElementNo = nthStorageElement.getsNo();
				logger.trace("storageElementNo " + storageElementNo);
			}
		}

		if(storageElementNo == -9) {
			logger.error("Tape not inside the library");
			// TODO : Handle this...
		}
		logger.trace(toBeUsedTape.getBarcode() + " is in " +  storageElementNo);
		return storageElementNo;
	}
}
