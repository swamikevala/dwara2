package org.ishafoundation.dwaraapi.storage.storagetype.tape.drive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		CommandLineExecutionResponse cler = null;
		try {
			cler = commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " status");
			if(cler.isComplete())
				mtStatusResponse = cler.getStdOutResponse();

		}
		catch (Exception e) {
			String errorMsg = e.getMessage();
			logger.error("Unable to get mtstatus - " + errorMsg);
			if(errorMsg.contains("Device or resource busy"))
				mtStatusResponse = errorMsg;
			else
				throw e;
		}
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
	
	public DriveDetails setTapeHeadPositionForInitializing(String dataTransferElementName) throws Exception {
		DriveDetails dsd = null;
		
		try {
			rewind(dataTransferElementName);
			logger.trace("after setTapeHeadPosition - dataTransferElementName " + dataTransferElementName);
	
			
			dsd = new DriveDetails();
			dsd.setDriveName(dataTransferElementName);
			dsd.setMtStatus(getMtStatus(dataTransferElementName));
		}catch (Exception e) {
			logger.error("Unable to setTapeHeadPositionForInitializing " + e.getMessage());
			throw e;
		}
		return dsd;	
	}
	
	public boolean isTapeBlank(String dataTransferElementName) throws Exception {
		try {
			rewind(dataTransferElementName);
			commandLineExecuter.executeCommand("mt -f " + dataTransferElementName + " fsf " + 1);
		} catch (Exception e) {
			logger.debug("fsf failed, means blank tape for sure");
			return true;
		}
		return false;
	}
	
	public DriveDetails setTapeHeadPositionForReadingLabel(String dataTransferElementName) throws Exception {
		return setTapeHeadPositionForInitializing(dataTransferElementName);
	}
	
	public DriveDetails setTapeHeadPositionForReadingInterArtifactXml(String dataTransferElementName) throws Exception {
		DriveDetails dsd = new DriveDetails();
		dsd.setDriveName(dataTransferElementName);
		try {
			
			eod(dataTransferElementName);
			
			int blockNumber = getCurrentPositionBlockNumber(dataTransferElementName);
			
			// validating the blockNumber
			seek(dataTransferElementName, blockNumber - 2);
			
			MtStatus mtStatus = getMtStatus(dataTransferElementName);
			dsd.setMtStatus(mtStatus);
		}catch (Exception e) {
			logger.error("Unable to setTapeHeadPositionForReadingInterArtifactXml " + e.getMessage(), e);
			throw e;
		}
		return dsd;
	}

	// To write Nth medialibrary the tape head should be pointing at file Number N
	// For e.g., if 5 medialibrary already in volume and to write the 6th mediaLibrary on tape, we need to position tapeHead on FileNumber = 5 - Remember Tape fileNumbers starts with 0
	// Reference - http://etutorials.org/Linux+systems/how+linux+works/Chapter+13+Backups/13.6+Tape+Drive+Devices/
	public DriveDetails setTapeHeadPositionForWriting(String dataTransferElementName, int blockNumberToBePositioned) throws Exception {
		DriveDetails dsd = new DriveDetails();
		dsd.setDriveName(dataTransferElementName);
		try {
			
			eod(dataTransferElementName);
			
			int blockNumber = getCurrentPositionBlockNumber(dataTransferElementName);
			
			// validating the blockNumber
			if(blockNumberToBePositioned != blockNumber) {
				throw new Exception("Expected blockNumber " + blockNumberToBePositioned + ", actual " + blockNumber);
			}
			
			MtStatus mtStatus = getMtStatus(dataTransferElementName);
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
			
			int blockNumber = getCurrentPositionBlockNumber(dataTransferElementName);
			
			// validating the blockNumber
			if(blockNumberToSeek != blockNumber) {
				throw new Exception("Expected blockNumberToSeek " + blockNumberToSeek + ", actual " + blockNumber);
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
	
	private int getCurrentPositionBlockNumber(String dataTransferElementName) throws Exception {
		// after seeking mt status responds with fileNo = -1 and blockNo = -1, so we had to do this...
		CommandLineExecutionResponse  cler = tell(dataTransferElementName);
		Pattern tellRespRegExPattern = Pattern.compile("At block ([0-9]*).");
		Matcher tellRespRegExMatcher = tellRespRegExPattern.matcher(cler.getStdOutResponse());
		int blockNumber = -9;
		if(tellRespRegExMatcher.find()) {
			blockNumber = Integer.parseInt(tellRespRegExMatcher.group(1));
		}
		return blockNumber;
	}

	@Override
	public DriveDetails setTapeHeadPositionForFinalizing(String dataTransferElementName, int blockNumberToBePositioned) throws Exception {
		return setTapeHeadPositionForWriting(dataTransferElementName, blockNumberToBePositioned);
	}
	
	private CommandLineExecutionResponse rewind(String dataTransferElementName) throws Exception {
		return commandToBeExecuted("mt -f " + dataTransferElementName + " rewind", true, "Input/output error");
	}
	
	private CommandLineExecutionResponse eod(String dataTransferElementName) throws Exception{
		return commandToBeExecuted("mt -f " + dataTransferElementName + " eod", true, null); // TODO - We dont know what the error msgs will be for eod 
	}

	private CommandLineExecutionResponse seek(String dataTransferElementName, int blockNo) throws Exception{
		return commandToBeExecuted("mt -f " + dataTransferElementName + " seek " + blockNo, true, "Input/output error"); // when tape loaded but not stinit throws "Input/output error" 
		// TODO 
		// 1) Check what happens when tape not loaded..
		// 2) How do we handle for some tapes for which seek fails the first but succeeds in the subsequent attempts that Sameer anna mentioned... 
	}
	
	private CommandLineExecutionResponse tell(String dataTransferElementName) throws Exception{
		 return commandToBeExecuted("mt -f " + dataTransferElementName + " tell", true, "Input/output error");
	}
	
	
	private CommandLineExecutionResponse commandToBeExecuted(String command, boolean retry, String retryCause) throws Exception {
		CommandLineExecutionResponse commandLineExecutionResponse = null;
		try {
			commandLineExecutionResponse = commandLineExecuter.executeCommand(command);
		} catch (Exception e) {
			if(retryCause == null || e.getMessage().contains(retryCause)) {
				if(retry) {
					// TODO call stinit here
					logger.warn("Stinit seems to be not executed. Running it now");
					
					// re-execute the same command again...
					commandToBeExecuted(command, false, retryCause);
				}
			}
			else
				throw e;
		}
		
		return commandLineExecutionResponse;
	}
}
