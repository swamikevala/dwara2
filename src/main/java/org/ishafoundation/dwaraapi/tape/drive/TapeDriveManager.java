package org.ishafoundation.dwaraapi.tape.drive;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.tape.CommandExecuter;

public class TapeDriveManager{
		
	// Swami said we can talk to the drive ever more on a low level and get details like no.OfReads, writes, usage etc.,
	public static DriveStatusDetails getDriveDetails(int driveSNo){
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

	static final String drive0Name = "/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst";
	static final String drive1Name = "/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1684087499-nst";
	static final String drive2Name = "/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1970448833-nst";
	
	// TODO : Talk to swami on this...
	public static String getDriveName(int driveSNo) {
		
		Map<Integer, String> driveNumberToNamesMap = new HashMap<Integer, String>();
		driveNumberToNamesMap.put(0, drive0Name);
		driveNumberToNamesMap.put(1, drive1Name);
		driveNumberToNamesMap.put(2, drive2Name);

		String driveName = driveNumberToNamesMap.get(driveSNo);
		
		System.out.println("drive name for " + driveSNo + " is " + driveName);
		return driveName;
	}

	public static MtStatus getMtStatus(String driveName){
		// FIXME String mtStatusResponse = callMtStatus(driveName);
		String mtStatusResponse = getHardcodedMtStatus(driveName);
		MtStatus mtStatus = MtStatusResponseParser.parseMtStatusResponse(mtStatusResponse);
		return mtStatus;
	}
	
	private static String getHardcodedMtStatus(String driveName) {
		String mtStatusResponse = null;
		String filePath = null;
		if(driveName.equals(drive0Name))
			filePath = "C:\\Users\\prakash\\projects\\videoarchives\\bru\\POC\\BRU002\\test\\mtResponses\\mt_status_busy.txt";
		else if(driveName.equals(drive1Name) || driveName.equals(drive2Name))
			filePath = "C:\\Users\\prakash\\projects\\videoarchives\\bru\\POC\\BRU002\\test\\mtResponses\\mt_status_no_tape_loaded.txt";
		else if(driveName.equals(""))
			filePath = "C:\\Users\\prakash\\projects\\videoarchives\\bru\\POC\\BRU002\\test\\mtResponses\\mt_status_tape_loaded.txt";
		
		try {
			mtStatusResponse = FileUtils.readFileToString(new File(filePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // will make a call to mtx and get the status realtime...
		return mtStatusResponse;
		
	}
	
	private static String callMtStatus(String driveName) {
		String mtStatusResponse = null;
		String mtStatusResponseFileName = "/data/tmp/" + driveName + "_status.err";
		CommandLineExecutionResponse cler = null;
		try {
			cler = CommandExecuter.executeCommand("mt -f " + driveName + " status", mtStatusResponseFileName);
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
	public static DriveStatusDetails setTapeHeadPositionForWriting(int dataTransferElementNo, int noOfArchivesAlreadyInVolume) {
		String dataTransferElementName = TapeDriveManager.getDriveName(dataTransferElementNo);
		
		MtStatus mtStatus = TapeDriveManager.getMtStatus(dataTransferElementName);
		int currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
		int currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
		System.out.println("b4 setTapeHeadPosition - dataTransferElementName " + dataTransferElementName + ", noOfArchivesAlreadyInVolume " + noOfArchivesAlreadyInVolume + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);
		
		try {
			// Usecases
			// For writing - currentFileNumberTapeHeadPointingTo cannot be > noOfArchivesAlreadyInVolume 
			if(currentFileNumberTapeHeadPointingTo == noOfArchivesAlreadyInVolume) {
					if(currentBlockNoTapeHeadPointingTo == 0) { // 1) Volume untouched after last write - Tape Head will be pointing to the start of file N, where N is noOfArchivesAlreadyInVolume the file Number to be used. 
						// do nothing...
						System.out.println("tape head already in right position");
					}else if(currentBlockNoTapeHeadPointingTo != 0) {
						System.out.println("tape head not in right position - fileNo ok but blockNo diff");
						System.out.println("is this even possible? is currentBlockNoTapeHeadPointingTo -1 or > 0" + currentBlockNoTapeHeadPointingTo);
						
						if(currentFileNumberTapeHeadPointingTo == 0) {
							System.out.println("check when is this possible with FileNumber 0 and blocknumber != 0");
							System.out.println("will be rewinding");
							rewind(dataTransferElementName);
						}else {
							System.out.println("will be bsfing 1 and fsfing 1");
							bsf(dataTransferElementName);
							fsf(dataTransferElementName);
						}
					}
			}
			else if(currentFileNumberTapeHeadPointingTo < noOfArchivesAlreadyInVolume){// Volume had been used for some restore - TapeHead will be pointing to some random fileMark
				if(currentFileNumberTapeHeadPointingTo == -1) { // what is the usecase... after seek for eg., fresh tape for eg., we have no way of knowing which file Number the tape head is. So safest way is to rewind
					System.out.println("fileNo is -1 -- identify what the usecase is");
					System.out.println("will be rewinding and fsfing 1");
					rewind(dataTransferElementName);
					fsf(dataTransferElementName, noOfArchivesAlreadyInVolume);
				}
				else { // >=0 and ofcourse < noOfArchivesAlreadyInVolume
					System.out.println("tape head not in right position");
					int fileNoToPosition = noOfArchivesAlreadyInVolume - currentFileNumberTapeHeadPointingTo;
					System.out.println("will be fsfing " + fileNoToPosition);
					fsf(dataTransferElementName, fileNoToPosition);
				}
			}
		}catch (Exception e) {
			System.err.println("Unable to setTapeHeadPositionForWriting " + e.getMessage()); e.printStackTrace();
		}

		mtStatus = TapeDriveManager.getMtStatus(dataTransferElementName);
		
		currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
		currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
		System.out.println("after setTapeHeadPosition - dataTransferElementName " + dataTransferElementName + ", noOfArchivesAlreadyInVolume " + noOfArchivesAlreadyInVolume + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);
		
		DriveStatusDetails dsd = new DriveStatusDetails();
		dsd.setDriveSNo(dataTransferElementNo);
		dsd.setMtStatus(mtStatus);
		
		return dsd;
	}

	// if blockNo is not requested to be seeked...
	public static DriveStatusDetails setTapeHeadPositionForReading(int dataTransferElementNo, int archiveNumberToBeRestored) {
		return setTapeHeadPositionForReading(dataTransferElementNo, archiveNumberToBeRestored, -9);
	}
	
	public static DriveStatusDetails setTapeHeadPositionForReading(int dataTransferElementNo, int archiveNumberToBeRestored, int blockNumberToSeek) {
		String dataTransferElementName = TapeDriveManager.getDriveName(dataTransferElementNo);
		
		MtStatus mtStatus = TapeDriveManager.getMtStatus(dataTransferElementName);
		int currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
		int currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
		System.out.println("b4 setTapeHeadPosition - dataTransferElementName " + dataTransferElementName + ", archiveNumberToBeRestored " + archiveNumberToBeRestored + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);

		DriveStatusDetails dsd = new DriveStatusDetails();
		dsd.setDriveSNo(dataTransferElementNo);

		try {
			if(currentFileNumberTapeHeadPointingTo == -1) { 
				System.out.println("fileNo is -1 -- identify what the usecase is");
				System.out.println("will be rewinding and fsfing 1");
				rewind(dataTransferElementName);
				fsf(dataTransferElementName, archiveNumberToBeRestored);
			}			
			else if(currentFileNumberTapeHeadPointingTo > archiveNumberToBeRestored){// Volume had been used for some restore - TapeHead will be pointing to some random fileMark
				if(archiveNumberToBeRestored < 2) {
					System.out.println("will be rewinding");
					rewind(dataTransferElementName);
					fsf(dataTransferElementName, archiveNumberToBeRestored);
				}else {
					System.out.println("will be bsfing " + (currentFileNumberTapeHeadPointingTo - archiveNumberToBeRestored + 1));
					bsf(dataTransferElementName, currentFileNumberTapeHeadPointingTo - archiveNumberToBeRestored + 1);
					fsf(dataTransferElementName);
				}
			}else if(currentFileNumberTapeHeadPointingTo < archiveNumberToBeRestored) {
				// >=0 and ofcourse < noOfArchivesAlreadyInVolume
				System.out.println("tape head not in right position");
				int fileNoToPosition = archiveNumberToBeRestored - currentFileNumberTapeHeadPointingTo;
				System.out.println("will be fsfing " + fileNoToPosition);
				fsf(dataTransferElementName, fileNoToPosition);
			}
			
			if(currentBlockNoTapeHeadPointingTo != 0) {
				System.out.println("tape head not in right position - fileNo ok but blockNo diff");
				if(blockNumberToSeek != -9) {
					seek(dataTransferElementName, (blockNumberToSeek - 1));
					
					// after seeking mt status responds with fileNo = -1 and blockNo = -1, so we had to do this...
					CommandLineExecutionResponse  cler = tell(dataTransferElementName);
					Pattern tellRespRegExPattern = Pattern.compile("At block ([0-9]*).");
					Matcher tellRespRegExMatcher = tellRespRegExPattern.matcher(cler.getStdOutResponse());
					int blockNumber = -9;
					if(tellRespRegExMatcher.find()) {
						blockNumber = Integer.parseInt(tellRespRegExMatcher.group(1));
					}
					mtStatus.setBlockNumber(blockNumber);
					dsd.setMtStatus(mtStatus);
					return dsd;
				} else {
					if(currentFileNumberTapeHeadPointingTo == archiveNumberToBeRestored) {
						if(currentFileNumberTapeHeadPointingTo == 0) {
							System.out.println("check when is this possible with FileNumber 0 and blocknumber != 0");
							System.out.println("will be rewinding");
							rewind(dataTransferElementName);
						}else {
							System.out.println("will be bsfing 1 and fsfing 1");
							bsf(dataTransferElementName);
							fsf(dataTransferElementName);
						}
					}
				}
			}
		}catch (Exception e) {
			System.err.println("Unable to setTapeHeadPositionForReading " + e.getMessage()); e.printStackTrace();
		}

		mtStatus = TapeDriveManager.getMtStatus(dataTransferElementName);
		
		currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
		currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
		System.out.println("after setTapeHeadPosition - dataTransferElementName " + dataTransferElementName + ", archiveNumberToBeRestored " + archiveNumberToBeRestored + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);
		
		dsd.setMtStatus(mtStatus);
		
		return dsd;
	}
	
//	setTapeHeadPositionForReading(){
//			if(currentFileNumberTapeHeadPointingTo == 0) {
//				System.out.println("currentFileNumberTapeHeadPointingTo == 0");
//				if(currentBlockNoTapeHeadPointingTo != 0) {
//					System.out.println("will be rewinding");
//					rewind(dataTransferElementName);
//				}
//				System.out.println("will be fsfing " + (noOfArchivesAlreadyInVolume - currentFileNumberTapeHeadPointingTo));
//				fsf(dataTransferElementName, (noOfArchivesAlreadyInVolume - currentFileNumberTapeHeadPointingTo));
//			}
//			else {
//				System.out.println("currentFileNumberTapeHeadPointingTo != 0 and < noOfArchivesAlreadyInVolume");
//				if(currentBlockNoTapeHeadPointingTo == 0) {
//					System.out.println("currentBlockNoTapeHeadPointingTo == 0");
//					System.out.println("will be fsfing " + (noOfArchivesAlreadyInVolume - currentFileNumberTapeHeadPointingTo));
//					fsf(dataTransferElementName, (noOfArchivesAlreadyInVolume - currentFileNumberTapeHeadPointingTo));
//				}
//				else if(currentBlockNoTapeHeadPointingTo != 0) {
//					System.out.println("will be bsfing 1");
//					bsf(dataTransferElementName);
//					System.out.println("will be fsfing " + ((noOfArchivesAlreadyInVolume - currentFileNumberTapeHeadPointingTo) + 1));
//					fsf(dataTransferElementName, (noOfArchivesAlreadyInVolume - currentFileNumberTapeHeadPointingTo) + 1); // +1 as we had to bsf
//				}
//			}
//	}

	private static void bsf(String dataTransferElementName) throws Exception {
		bsf(dataTransferElementName, 1);
	}

	//		 mt -f /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst fsf 2
	//		 mt -f /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst seek 23562
	private static void fsf(String dataTransferElementName) throws Exception {
		fsf(dataTransferElementName, 1);
	}
	
	private static void fsf(String dataTransferElementName, int noOfBlocks) throws Exception{
		CommandExecuter.executeCommand("mt -f " + dataTransferElementName + " fsf " + noOfBlocks);
	}


	//		 mt -f /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bsf 2	
	private static void bsf(String dataTransferElementName, int noOfBlocks) throws Exception{
		CommandExecuter.executeCommand("mt -f " + dataTransferElementName + " bsf " + noOfBlocks);
	}
	
	//		 mt -f /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst rewind
	private static void rewind(String dataTransferElementName) throws Exception {
		CommandExecuter.executeCommand("mt -f " + dataTransferElementName + " rewind");
	}
	
	private static void seek(String dataTransferElementName, int blockNo) {
		CommandExecuter.executeCommand("mt -f " + dataTransferElementName + " seek " + blockNo);
	}
	
	private static void bsf2Fsf1(String dataTransferElementName) throws Exception{
		bsf(dataTransferElementName, 2); 
		fsf(dataTransferElementName);
	}
	
	private static CommandLineExecutionResponse tell(String dataTransferElementName) throws Exception{
		 return CommandExecuter.executeCommand("mt -f " + dataTransferElementName + " tell");
	}
	
	/**
	 * 
	 * @param args[0] - isWriting - boolean 
	 * @param args[1] - dataTransferElementNo/driveNo 
	 * @param args[2] - if writing the value on noOfArchivesAlreadyInVolume else if reading archiveNumberToBeRestored
	 * @param args[3] - if reading the block number to seek
	 */
	public static void main(String[] args) {
		boolean isWriting = Boolean.parseBoolean(args[0]);
		int dataTransferElementNo = Integer.parseInt(args[1]);
		
		System.out.println("isWriting - " + isWriting);
		
		if(isWriting)
			setTapeHeadPositionForWriting(dataTransferElementNo, Integer.parseInt(args[2]));
		else {
			int blockNumberStartToSeek = -9;
			try {
				blockNumberStartToSeek = Integer.parseInt(args[3]);
				setTapeHeadPositionForReading(dataTransferElementNo, Integer.parseInt(args[2]), blockNumberStartToSeek);	
			}catch (Exception e) {
				setTapeHeadPositionForReading(dataTransferElementNo, Integer.parseInt(args[2]));
			}
		}
	}
}
