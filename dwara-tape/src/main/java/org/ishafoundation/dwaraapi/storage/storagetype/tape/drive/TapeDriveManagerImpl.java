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
	
	@Autowired
	private DeviceLockFactory deviceLockFactory;
	
	public MtStatus getMtStatus(String dataTransferElementName) throws Exception{
		logger.trace("Calling mtStatus - " + dataTransferElementName);
		String mtStatusResponse = callMtStatus(dataTransferElementName);
		MtStatus mtStatus = MtStatusResponseParser.parseMtStatusResponse(mtStatusResponse);
		return mtStatus;
	}

	/*
		While its just status requisition - atleast in VTL - when 2 threads call the mt status at the same time the response is a DEVICE BUSY for one of the threads - So synchronizing the method...
2020-09-14 15:14:04,650 DEBUG org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl [pool-2-thread-1] start [mt, -f, /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst, status]
2020-09-14 15:14:04,688 DEBUG org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl [pool-1-thread-3~!~sr-13-job-8] start [mt, -f, /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst, status]
2020-09-14 15:14:05,669 DEBUG org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl [pool-2-thread-1] exit-status: 0
2020-09-14 15:14:05,669 DEBUG org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl [pool-2-thread-1] end [mt, -f, /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst, status]
2020-09-14 15:14:05,695 DEBUG org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl [pool-1-thread-3~!~sr-13-job-8] exit-status: 1
2020-09-14 15:14:05,696 DEBUG org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl [pool-1-thread-3~!~sr-13-job-8] Command's stderr message - /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst: Device or resource busy

2020-09-14 15:15:05,079 DEBUG org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl [pool-2-thread-1] start [mt, -f, /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst, status]
2020-09-14 15:15:05,086 DEBUG org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl [pool-1-thread-1~!~sr-14-job-18] start [mt, -f, /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst, status]
2020-09-14 15:15:06,109 DEBUG org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl [pool-1-thread-1~!~sr-14-job-18] exit-status: 1
2020-09-14 15:15:06,109 DEBUG org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl [pool-1-thread-1~!~sr-14-job-18] Command's stderr message - /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst: Device or resource busy
2020-09-14 15:15:06,111 DEBUG org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl [pool-2-thread-1] exit-status: 0
2020-09-14 15:15:06,112 DEBUG org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl [pool-2-thread-1] end [mt, -f, /dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst, status]

	 */
	private String callMtStatus(String dataTransferElementName) throws Exception {
		synchronized (deviceLockFactory.getDeviceLock(dataTransferElementName)) {
			logger.trace("Executing mtStatus synchronized-ly - " + dataTransferElementName);
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
		logger.debug("Positioning tape head for reading artifact label " + dataTransferElementName);
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
		logger.trace("Now rewinding - " + dataTransferElementName);
		synchronized (deviceLockFactory.getDeviceLock(dataTransferElementName)) {
			return commandToBeExecuted("mt -f " + dataTransferElementName + " rewind", true, "Input/output error");	
		}
	}
	
	private CommandLineExecutionResponse eod(String dataTransferElementName) throws Exception{
		logger.trace("Now eoding - " + dataTransferElementName);
		synchronized (deviceLockFactory.getDeviceLock(dataTransferElementName)) {
			return commandToBeExecuted("mt -f " + dataTransferElementName + " eod", true, "Input/output error"); // TODO - We dont know what the error msgs will be for eod
		}
	}

	private CommandLineExecutionResponse seek(String dataTransferElementName, int blockNo) throws Exception{
		logger.trace("Now seeking - " + dataTransferElementName + ":" + blockNo);
		synchronized (deviceLockFactory.getDeviceLock(dataTransferElementName)) {
			return commandToBeExecuted("mt -f " + dataTransferElementName + " seek " + blockNo, true, "Input/output error"); // when tape loaded but not stinit throws "Input/output error"
		}
		// TODO 
		// 1) Check what happens when tape not loaded..
		// 2) How do we handle for some tapes for which seek fails the first but succeeds in the subsequent attempts that Sameer anna mentioned... 
	}
	
	private CommandLineExecutionResponse tell(String dataTransferElementName) throws Exception{
		logger.trace("Now tell-ing - " + dataTransferElementName);
		synchronized (deviceLockFactory.getDeviceLock(dataTransferElementName)) {
			return commandToBeExecuted("mt -f " + dataTransferElementName + " tell", true, "Input/output error");
		}
	}
	
	
	private CommandLineExecutionResponse commandToBeExecuted(String command, boolean retry, String retryCause) throws Exception {
		logger.debug("Executing " + command);
		CommandLineExecutionResponse commandLineExecutionResponse = null;
		try {
			commandLineExecutionResponse = commandLineExecuter.executeCommand(command);
		} catch (Exception e) {
			if(retryCause == null || e.getMessage().contains(retryCause)) {
				if(retry) {
					// TODO call stinit here
					logger.warn("stinit script seems to be not executed. Running it now");
					logger.info("TODO : stinit script need to be invoked here");
					// re-execute the same command again...
					logger.debug("Now re-executing the same command again " + command);
					commandToBeExecuted(command, false, retryCause);
				}
			}
			else
				throw e;
		}
		
		return commandLineExecutionResponse;
	}
}
