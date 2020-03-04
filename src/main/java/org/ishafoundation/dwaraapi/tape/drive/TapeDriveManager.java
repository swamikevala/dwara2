package org.ishafoundation.dwaraapi.tape.drive;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeDriveManager{
	
	private static final Logger logger = LoggerFactory.getLogger(TapeDriveManager.class);
	
	private static final String drive0Name = "/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst";
	private static final String drive1Name = "/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1684087499-nst";
	private static final String drive2Name = "/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1970448833-nst";
	
	@Autowired
	private CommandLineExecuter commandLineExecuter;
		
	// Swami said we can talk to the drive even more on a low level and get details like no.OfReads, writes, usage etc.,
	public DriveStatusDetails getDriveDetails(int driveSNo){
		String driveName = getDriveName(driveSNo);
		MtStatus mtStatus = getMtStatus(driveName);

		DriveStatusDetails dsd = new DriveStatusDetails();
		dsd.setDriveSNo(driveSNo);
		dsd.setMtStatus(mtStatus);

		if(!mtStatus.isBusy()) {

			// TODO : callLowLevelCommandAndGetTheBelowDetails(); // SWAMI' Action point...
			dsd.setNoOfReads(5);
			dsd.setNoOfWrites(50);
			dsd.setHoursOfReads(6);
			dsd.setHoursOfWrites(544); 
			dsd.setTotalUsageInHours(550);
		}
		return dsd;
	}

	
	// TODO : Talk to swami on this...
	public String getDriveName(int driveSNo) {
		
		Map<Integer, String> driveNumberToNamesMap = new HashMap<Integer, String>();
		driveNumberToNamesMap.put(0, drive0Name);
		driveNumberToNamesMap.put(1, drive1Name);
		driveNumberToNamesMap.put(2, drive2Name);

		String driveName = driveNumberToNamesMap.get(driveSNo);
		
		System.out.println("drive name for " + driveSNo + " is " + driveName);
		return driveName;
	}

	public MtStatus getMtStatus(String driveName){
		String mtStatusResponse = callMtStatus(driveName);
		//String mtStatusResponse = getHardcodedMtStatus(driveName);
		MtStatus mtStatus = MtStatusResponseParser.parseMtStatusResponse(mtStatusResponse);
		return mtStatus;
	}

	private String callMtStatus(String driveName) {
		String mtStatusResponse = null;
		String mtStatusResponseFileName = "/data/tmp/" + driveName + "_status.err";
		CommandLineExecutionResponse cler = null;
		try {
			cler = commandLineExecuter.executeCommand("mt -f " + driveName + " status", mtStatusResponseFileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mtStatusResponse = cler.getStdOutResponse();
		return mtStatusResponse;
	}

	// To write Nth medialibrary the tape head should be pointing at file Number N
	// For e.g., if 5 medialibrary already in volume and to write the 6th mediaLibrary on tape, we need to position tapeHead on FileNumber = 5 - Remember Tape fileNumbers starts with 0
	// Reference - http://etutorials.org/Linux+systems/how+linux+works/Chapter+13+Backups/13.6+Tape+Drive+Devices/
	public DriveStatusDetails setTapeHeadPositionForWriting(int dataTransferElementNo) {
		String dataTransferElementName = getDriveName(dataTransferElementNo);
		
		MtStatus mtStatus = getMtStatus(dataTransferElementName);
		int currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
		int currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
		System.out.println("b4 setTapeHeadPosition - dataTransferElementName " + dataTransferElementName + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);
		
		try {
			eod(dataTransferElementName);
			
		}catch (Exception e) {
			System.err.println("Unable to setTapeHeadPositionForWriting " + e.getMessage()); e.printStackTrace();
		}

		mtStatus = getMtStatus(dataTransferElementName);
		
		currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
		currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
		System.out.println("after setTapeHeadPosition - dataTransferElementName " + dataTransferElementName + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);
		
		DriveStatusDetails dsd = new DriveStatusDetails();
		dsd.setDriveSNo(dataTransferElementNo);
		dsd.setMtStatus(mtStatus);
		
		return dsd;
	}

	// if blockNo is not requested to be seeked...
	
	public DriveStatusDetails setTapeHeadPositionForReading(int dataTransferElementNo, int blockNumberToSeek) {
		String dataTransferElementName = null;
		MtStatus mtStatus = null;
		DriveStatusDetails dsd = null;
		try {
			dataTransferElementName = getDriveName(dataTransferElementNo);
			dsd = new DriveStatusDetails();
			dsd.setDriveSNo(dataTransferElementNo);
			
			seek(dataTransferElementName, (blockNumberToSeek - 1));
			
			// after seeking mt status responds with fileNo = -1 and blockNo = -1, so we had to do this...
			CommandLineExecutionResponse  cler = tell(dataTransferElementName);
			Pattern tellRespRegExPattern = Pattern.compile("At block ([0-9]*).");
			Matcher tellRespRegExMatcher = tellRespRegExPattern.matcher(cler.getStdOutResponse());
			int blockNumber = -9;
			if(tellRespRegExMatcher.find()) {
				blockNumber = Integer.parseInt(tellRespRegExMatcher.group(1));
			}
			mtStatus = getMtStatus(dataTransferElementName);
			// TODO - Is fileNumber needed?? mtStatus.setFileNumber(fileNumber);
			mtStatus.setBlockNumber(blockNumber);
			dsd.setMtStatus(mtStatus);
			return dsd;		
		}
		catch (Exception e) {
			System.err.println("Unable to setTapeHeadPositionForReading " + e.getMessage()); e.printStackTrace();
			
		}

//		mtStatus = getMtStatus(dataTransferElementName);
//		
//		int currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
//		int currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
//		System.out.println("after setTapeHeadPosition - dataTransferElementName " + dataTransferElementName +  ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);
//		
//		dsd.setMtStatus(mtStatus);
		
		return dsd;
	}

	private void eod(String dataTransferElementName) {
		commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " eod");
	}
	
	private void seek(String dataTransferElementName, int blockNo) {
		commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " seek " + blockNo);
	}
	
	private CommandLineExecutionResponse tell(String dataTransferElementName) throws Exception{
		 return commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " tell");
	}
	
	/**
	 * 
	 * @param args[0] - isWriting - boolean 
	 * @param args[1] - dataTransferElementNo/driveNo 
	 * @param args[2] - if writing the value on noOfArchivesAlreadyInVolume else if reading archiveNumberToBeRestored
	 * @param args[3] - if reading the block number to seek
	 */
	public static void main(String[] args) {
//		boolean isWriting = Boolean.parseBoolean(args[0]);
//		int dataTransferElementNo = Integer.parseInt(args[1]);
//		
//		System.out.println("isWriting - " + isWriting);
//		
//		if(isWriting)
//			setTapeHeadPositionForWriting(dataTransferElementNo, Integer.parseInt(args[2]));
//		else {
//			int blockNumberStartToSeek = -9;
//			try {
//				blockNumberStartToSeek = Integer.parseInt(args[3]);
//				setTapeHeadPositionForReading(dataTransferElementNo, Integer.parseInt(args[2]), blockNumberStartToSeek);	
//			}catch (Exception e) {
//				setTapeHeadPositionForReading(dataTransferElementNo, Integer.parseInt(args[2]));
//			}
//		}
	}
}
