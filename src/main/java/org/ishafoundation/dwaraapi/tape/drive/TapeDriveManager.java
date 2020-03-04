package org.ishafoundation.dwaraapi.tape.drive;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.db.dao.TapedriveDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeDriveManager{
	
	private static final Logger logger = LoggerFactory.getLogger(TapeDriveManager.class);

	@Autowired
	private TapedriveDao tapedriveDao;
	
	@Autowired
	private CommandLineExecuter commandLineExecuter;
		
	// Swami said we can talk to the drive even more on a low level and get details like no.OfReads, writes, usage etc.,
	public DriveStatusDetails getDriveDetails(int driveSNo){
		String driveName = getDriveName(driveSNo);
		MtStatus mtStatus = getMtStatus(driveName);
		
		DriveStatusDetails dsd = new DriveStatusDetails();
		dsd.setDriveSNo(driveSNo);
		dsd.setMtStatus(mtStatus);
		dsd.setDte(null);
		
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

	
	// TODO : Need to handle multiple tape libraries...
	public String getDriveName(int driveSNo) {
		Tapedrive tapedrive = tapedriveDao.findByElementAddress(driveSNo);
		return tapedrive.getDeviceWwid();
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
		DriveStatusDetails dsd = null;
		
		try {
			String dataTransferElementName = getDriveName(dataTransferElementNo);
			
			MtStatus mtStatus = getMtStatus(dataTransferElementName);
			int currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
			int currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
			logger.trace("b4 setTapeHeadPosition - dataTransferElementName " + dataTransferElementName + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);
		
		
			eod(dataTransferElementName);

			mtStatus = getMtStatus(dataTransferElementName);
			currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
			currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
			logger.trace("after eod - dataTransferElementName " + dataTransferElementName + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);

			if(currentFileNumberTapeHeadPointingTo > 0) {
				bsf(dataTransferElementName, 1);
				fsf(dataTransferElementName, 1);
			}
			else {
				rewind(dataTransferElementName);
			}
			
			mtStatus = getMtStatus(dataTransferElementName);
			currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
			currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
			logger.trace("after setTapeHeadPosition - dataTransferElementName " + dataTransferElementName + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);
	
			
			dsd = new DriveStatusDetails();
			dsd.setDriveSNo(dataTransferElementNo);
			dsd.setMtStatus(mtStatus);
		}catch (Exception e) {
			logger.error("Unable to setTapeHeadPositionForWriting " + e.getMessage()); e.printStackTrace();
		}
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
			logger.error("Unable to setTapeHeadPositionForReading " + e.getMessage()); e.printStackTrace();
			
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

	private CommandLineExecutionResponse eod(String dataTransferElementName) throws Exception{
		return commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " eod");
	}

	
	private CommandLineExecutionResponse fsf(String dataTransferElementName, int noOfBlocks) throws Exception{
		return commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " fsf " + noOfBlocks);
	}

	private CommandLineExecutionResponse bsf(String dataTransferElementName, int noOfBlocks) throws Exception{
		return commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " bsf " + noOfBlocks);
	}

	private CommandLineExecutionResponse rewind(String dataTransferElementName) throws Exception {
		return commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " rewind");
	}
	
	private CommandLineExecutionResponse seek(String dataTransferElementName, int blockNo) throws Exception{
		return commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " seek " + blockNo);
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
