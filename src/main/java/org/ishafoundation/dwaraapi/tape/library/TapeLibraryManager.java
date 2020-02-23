package org.ishafoundation.dwaraapi.tape.library;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.tape.CommandExecuter;
import org.ishafoundation.dwaraapi.tape.drive.DriveStatusDetails;
import org.ishafoundation.dwaraapi.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.tape.library.components.DataTransferElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TapeLibraryManager{
	
	static Logger logger = LoggerFactory.getLogger(CommandExecuter.class);
	
	public static MtxStatus getMtxStatus(String tapeLibraryName){
		String mtxStatusResponse = callMtxStatus(tapeLibraryName);
		MtxStatus mtxStatus = MtxStatusResponseParser.parseMtxStatusResponse(mtxStatusResponse);
		return mtxStatus;
	}
	
	private static String callMtxStatus(String tapeLibraryName) {
		String mtxStatusResponse = null;
		String mtxStatusResponseFileName = "/data/tmp/" + tapeLibraryName.replace("/", "_") + "_status.err";
		CommandLineExecutionResponse cler = CommandExecuter.executeCommand("mtx -f " + tapeLibraryName + " status", mtxStatusResponseFileName);
		mtxStatusResponse = cler.getStdOutResponse();
		return mtxStatusResponse;
	}	
	
	//		unload if any other tape - we should always check the status of the drive before unloading the tape. if the drive is busy we should not unload the tape.., (At the time of writing another thread/process is allowed to unload the tape)
	public static boolean unload(String tapeLibraryName, int seSNo, int driveSNo){
		CommandLineExecutionResponse cler = CommandExecuter.executeCommand("mtx -f " + tapeLibraryName + " unload " + seSNo + " " + driveSNo);
		logger.debug(cler.getStdOutResponse());
		return true;
	}
	
	//		load the tape to be used
	public static boolean load(String tapeLibraryName, int seSNo, int driveSNo){
		CommandLineExecutionResponse cler = CommandExecuter.executeCommand("mtx -f " + tapeLibraryName + " load " + seSNo + " " + driveSNo);
		logger.debug(cler.getStdOutResponse());
		return true;
	}
	
	
	public static List<DriveStatusDetails> getAvailableDrivesList(MtxStatus mtxStatus){
		List<DriveStatusDetails> availablDriveDetailsList = new ArrayList<DriveStatusDetails>();
		List<DataTransferElement> dteList = mtxStatus.getDteList();
		System.out.println("dteList " +  dteList);
		for (Iterator<DataTransferElement> iterator = dteList.iterator(); iterator.hasNext();) {
			DataTransferElement nthDataTransferElement = (DataTransferElement) iterator.next();
			int dataTransferElementSNo = nthDataTransferElement.getsNo(); 
			DriveStatusDetails dsd = TapeDriveManager.getDriveDetails(dataTransferElementSNo);
			dsd.setDte(nthDataTransferElement);
			if(!dsd.getMtStatus().isBusy()) { // only not busy drives are candidates
				availablDriveDetailsList.add(dsd);
			} else {
				
			}
		}
		return availablDriveDetailsList;
	}
	
	public static List<DriveStatusDetails> getAvailableDrivesList(){
		MtxStatus mtxStatus = null;// getMtxStatus(""); // FIXME : LibraryName
		String mtxStatusResponse = null;
		try {
			mtxStatusResponse = FileUtils.readFileToString(new File("C:\\Users\\prakash\\projects\\videoarchives\\bru\\POC\\BRU002\\test\\mtxResponses\\mtx_status_all_drives_available.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // will make a call to mtx and get the status realtime...
		mtxStatus = MtxStatusResponseParser.parseMtxStatusResponse(mtxStatusResponse);
		return getAvailableDrivesList(mtxStatus);
	}	
}
