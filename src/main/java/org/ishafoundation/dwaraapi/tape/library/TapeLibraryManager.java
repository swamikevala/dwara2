package org.ishafoundation.dwaraapi.tape.library;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.db.model.master.storage.Tape;
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
	
	 // FIXME : Hardcoded LibraryName
	String tapeLibraryName = "/dev/tape/by-id/scsi-1IBM_03584L32_0000079313020400";
	
	static Logger logger = LoggerFactory.getLogger(TapeLibraryManager.class);
	
	@Autowired
	private CommandLineExecuter commandLineExecuter;
	
	@Autowired
	private TapeDriveManager tapeDriveManager;
	
	public MtxStatus getMtxStatus(String tapeLibraryName){
		String mtxStatusResponse = callMtxStatus(tapeLibraryName);
		MtxStatus mtxStatus = MtxStatusResponseParser.parseMtxStatusResponse(mtxStatusResponse);
		return mtxStatus;
	}
	
	private String callMtxStatus(String tapeLibraryName) {
		String mtxStatusResponse = null;
		String mtxStatusResponseFileName = "/data/tmp/" + tapeLibraryName.replace("/", "_") + "_status.err";
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
		System.out.println("dteList " +  dteList);
		for (Iterator<DataTransferElement> iterator = dteList.iterator(); iterator.hasNext();) {
			DataTransferElement nthDataTransferElement = (DataTransferElement) iterator.next();
			int dataTransferElementSNo = nthDataTransferElement.getsNo(); 
			DriveStatusDetails dsd = tapeDriveManager.getDriveDetails(dataTransferElementSNo);
			dsd.setDte(nthDataTransferElement);
			if(!dsd.getMtStatus().isBusy()) { // only not busy drives are candidates
				availablDriveDetailsList.add(dsd);
			} else {
				
			}
		}
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
			DriveStatusDetails driveStatusDetails = tapeDriveManager.getDriveDetails(toBeUsedDataTransferElementSNo);
			
			if(driveStatusDetails.getMtStatus().isDriveReady()){ // means drive is not empty and has another tape - so we need to unload the other tape
				System.out.println(toBeUsedDataTransferElementSNo + " is not empty and has another tape - so we need to unload the other tape");
				if(!driveStatusDetails.getMtStatus().isBusy()) {
					System.out.println("Unloading ");
					unload(tapeLibraryName, driveStatusDetails.getDte().getStorageElementNo(), driveStatusDetails.getDte().getsNo());
					System.out.println("Unload successful ");
				}else {
					System.out.println("Something wrong with the logic. Drive is not supposed to be busy, but seems busy");
				}
			}
	
			// locate in which slot the volume is...
			MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
			List<StorageElement> seList = mtxStatus.getSeList();
			System.out.println("Now locating in which slot the volume is from the list ..." + seList);
			int storageElementNo = locateTheTapesStorageElement(seList, toBeUsedTape);
	
			// load the volume in the passed slot to the passed dte
			System.out.println("now loading " + toBeUsedTape.getBarcode() + " from " + storageElementNo + " and loading into drive " + toBeUsedDataTransferElementSNo);
			load(tapeLibraryName, storageElementNo, toBeUsedDataTransferElementSNo);
			isSuccess = true;
		}
		catch (Exception e) {
			isSuccess = false;
			System.err.println(e.getMessage());e.printStackTrace();
			throw e;
		}
		return isSuccess;
	}
	private int locateTheTapesStorageElement(List<StorageElement> seList, Tape toBeUsedTape){
		int storageElementNo = -9;
		System.out.println("seList " + seList);
		System.out.println("toBeUsedVolume.getCode() " + toBeUsedTape.getBarcode());
		for (Iterator<StorageElement> iterator = seList.iterator(); iterator.hasNext();) {
			StorageElement nthStorageElement = (StorageElement) iterator.next();
			System.out.println("nthStorageElement.getVolumeTag() " + nthStorageElement.getVolumeTag());
			if(nthStorageElement.getVolumeTag() != null && nthStorageElement.getVolumeTag().equals(toBeUsedTape.getBarcode())) {
				storageElementNo = nthStorageElement.getsNo();
				System.out.println("storageElementNo " + storageElementNo);
			}
		}

		if(storageElementNo == -9) {
			System.err.println("Tape not inside the library");
			// TODO : Handle this...
		}
		System.out.println(toBeUsedTape.getBarcode() + " is in " +  storageElementNo);
		return storageElementNo;
	}
}
