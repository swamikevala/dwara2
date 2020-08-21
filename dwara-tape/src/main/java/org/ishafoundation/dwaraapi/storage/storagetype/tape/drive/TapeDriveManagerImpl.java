package org.ishafoundation.dwaraapi.storage.storagetype.tape.drive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.MtStatus;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.MtStatusResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Component
@Primary
//@Profile("default") works
@Profile({ "!dev & !stage" })
public class TapeDriveManagerImpl implements TapeDriveManager{
	
	private static final Logger logger = LoggerFactory.getLogger(TapeDriveManagerImpl.class);
	
	@Autowired
	private CommandLineExecuter commandLineExecuter;
	
	public MtStatus getMtStatus(String dataTransferElementName) throws Exception{
		String mtStatusResponse = callMtStatus(dataTransferElementName);
		MtStatus mtStatus = MtStatusResponseParser.parseMtStatusResponse(mtStatusResponse);
		return mtStatus;
	}

	private String callMtStatus(String dataTransferElementName) throws Exception {
		String mtStatusResponse = null;
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " status");
		if(cler.isComplete())
			mtStatusResponse = cler.getStdOutResponse();
		return mtStatusResponse;
	}

	
	public DriveDetails getDriveDetails(String dataTransferElementName) throws Exception {
		MtStatus mtStatus = getMtStatus(dataTransferElementName);
		
		DriveDetails dsd = new DriveDetails();
//		dsd.setTapelibraryName(tapelibraryName);
//		dsd.setDriveSNo(dataTransferElementNo);
		dsd.setDriveName(dataTransferElementName);
		dsd.setMtStatus(mtStatus);
//		dsd.setDte(null);
		
		if(!mtStatus.isBusy()) {

//			// TODO : callLowLevelCommandAndGetTheBelowDetails(); // SWAMI' Action point...
//			dsd.setNoOfReads(5);
//			dsd.setNoOfWrites(50);
//			dsd.setHoursOfReads(6);
//			dsd.setHoursOfWrites(544); 
//			dsd.setTotalUsageInHours(550);
		}
		return dsd;
	}
	
	public boolean isTapeBlank(String dataTransferElementName) throws Exception {
		rewind(dataTransferElementName);
		try {
			fsf(dataTransferElementName, 1);
//			// if fsf fails its a blank tape for sure...
//			String failureReason = fsfCommandLineExecutionResponse.getFailureReason();
//
//			if(StringUtils.isNotBlank(failureReason)){ // when fsf command fails that means its a blank tape
//				return true;
//			}
		} catch (Exception e) {
			logger.debug("fsf failed, means blank tape for sure");
			return true;
		}
		return false;
	}
	
	// To write Nth medialibrary the tape head should be pointing at file Number N
	// For e.g., if 5 medialibrary already in volume and to write the 6th mediaLibrary on tape, we need to position tapeHead on FileNumber = 5 - Remember Tape fileNumbers starts with 0
	// Reference - http://etutorials.org/Linux+systems/how+linux+works/Chapter+13+Backups/13.6+Tape+Drive+Devices/
	public DriveDetails setTapeHeadPositionForWriting(String dataTransferElementName, int fileNumberToBePositioned) throws Exception {
		DriveDetails dsd = new DriveDetails();
		dsd.setDriveName(dataTransferElementName);
		
		
		try {
			MtStatus mtStatus = getMtStatus(dataTransferElementName);
			int currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
			int currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
			logger.trace("b4 setTapeHeadPositionForWriting - dataTransferElementName " + dataTransferElementName + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);
		
			if(currentFileNumberTapeHeadPointingTo == fileNumberToBePositioned && currentBlockNoTapeHeadPointingTo == 0) {
				logger.trace("Tape head already in position");
				dsd.setMtStatus(mtStatus);
				return dsd;
			}
			
			eod(dataTransferElementName);

			mtStatus = getMtStatus(dataTransferElementName);
			currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
			currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
			logger.trace("after eod - dataTransferElementName " + dataTransferElementName + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);

			if(currentFileNumberTapeHeadPointingTo != fileNumberToBePositioned) {
				logger.warn("something wrong with eod, currentFileNumberTapeHeadPointingTo(" + currentFileNumberTapeHeadPointingTo + ") != expectedFileNumberToBePositioned(" + fileNumberToBePositioned  + ")- dataTransferElementName " + dataTransferElementName);
				logger.info("rewinding and fsfing " + fileNumberToBePositioned);
				rewind(dataTransferElementName);
				fsf(dataTransferElementName, fileNumberToBePositioned);
			}
			else {
				// TODO : Check if we need this...
				// eod returns a nonzero blocknumber, so safely doing this...
				bsf(dataTransferElementName, 1);
				fsf(dataTransferElementName, 1);
			}
		
			mtStatus = getMtStatus(dataTransferElementName);
			currentFileNumberTapeHeadPointingTo = mtStatus.getFileNumber();
			currentBlockNoTapeHeadPointingTo = mtStatus.getBlockNumber();
			logger.info("after setTapeHeadPositionForWriting - dataTransferElementName " + dataTransferElementName + ", currentFileNumberTapeHeadPointingTo " + currentFileNumberTapeHeadPointingTo + ", currentBlockNoTapeHeadPointingTo " + currentBlockNoTapeHeadPointingTo);

			if(currentFileNumberTapeHeadPointingTo != fileNumberToBePositioned && currentBlockNoTapeHeadPointingTo != 0)
				throw new Exception("Expected fileNumber " + fileNumberToBePositioned + ", actual " + currentFileNumberTapeHeadPointingTo + ", expected block 0, actual " + currentBlockNoTapeHeadPointingTo);
			dsd.setMtStatus(mtStatus);
		}catch (Exception e) {
			logger.error("Unable to setTapeHeadPositionForWriting " + e.getMessage(), e);
			throw e;
		}
		return dsd;
	}

	// if blockNo is not requested to be seeked...
	public DriveDetails setTapeHeadPositionForReading(String dataTransferElementName, int blockNumberToSeek)
			throws Exception {
		MtStatus mtStatus = null;
		DriveDetails dsd = null;
		try {
			dsd = new DriveDetails();
			dsd.setDriveName(dataTransferElementName);
			seek(dataTransferElementName, blockNumberToSeek);
			
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
			logger.error("Unable to setTapeHeadPositionForReading " + e.getMessage());
			throw e;
		}
	}
	
	public DriveDetails setTapeHeadPositionForInitializing(String dataTransferElementName) throws Exception {
		DriveDetails dsd = null;
		
		try {
			rewind(dataTransferElementName);
			logger.trace("after setTapeHeadPosition - dataTransferElementName " + dataTransferElementName);
	
			
			dsd = new DriveDetails();
			dsd.setDriveName(dataTransferElementName);
			dsd.setMtStatus(getMtStatus(dataTransferElementName));
		}catch (Exception e) {
			logger.error("Unable to setTapeHeadPositionForWriting " + e.getMessage());
			throw e;
		}
		return dsd;	
	}
	
	public DriveDetails setTapeHeadPositionForReadingLabel(String dataTransferElementName) throws Exception {
		return setTapeHeadPositionForInitializing(dataTransferElementName);
	}

	public DriveDetails setTapeHeadPositionForFinalizing(String dataTransferElementName) throws Exception {
		// TODO Auto-generated method stub
		return null;
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

}
