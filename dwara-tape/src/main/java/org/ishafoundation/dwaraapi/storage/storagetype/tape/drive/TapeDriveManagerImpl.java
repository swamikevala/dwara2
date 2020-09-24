package org.ishafoundation.dwaraapi.storage.storagetype.tape.drive;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.DeviceLockFactory;
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
	
	@Autowired
	private Configuration configuration;
	
	private static final String DRIVE_IO_ERROR = "Input/output error";
	private static final String DRIVE_BUSY_ERROR = "Device or resource busy";
	
	private List<String> errorList = new ArrayList<String>();
	public TapeDriveManagerImpl() {
		errorList.add(DRIVE_IO_ERROR);
		errorList.add(DRIVE_BUSY_ERROR);
	}
	
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
//		synchronized (deviceLockFactory.getDeviceLock(dataTransferElementName)) {
//			logger.trace("Executing mtStatus synchronized-ly - " + dataTransferElementName);
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
//		}
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
			List<String> errorList = new ArrayList<String>();
			errorList.add(DRIVE_BUSY_ERROR);
			executeCommandWithRetries("mt -f " + dataTransferElementName + " fsf " + 1, 0, errorList);
		} catch (Exception e) {
			logger.debug("fsf failed, means blank tape for sure");
			return true;
		}
		return false;
	}
	
	public DriveDetails setTapeHeadPositionForReadingVolumeLabel(String dataTransferElementName) throws Exception {
		return setTapeHeadPositionForInitializing(dataTransferElementName);
	}
	
	public DriveDetails setTapeHeadPositionForReadingInterArtifactXml(String dataTransferElementName, int expectedBlockNumberToBePositioned) throws Exception {
		logger.debug("Positioning tape head for reading artifact label " + dataTransferElementName);
		DriveDetails dsd = new DriveDetails();
		dsd.setDriveName(dataTransferElementName);
		try {
			
			eod(dataTransferElementName);
			
			int blockNumberAfterEOD = getCurrentPositionBlockNumber(dataTransferElementName);
			
			int blockNumberToSeek = 0;
			
			if(blockNumberAfterEOD == expectedBlockNumberToBePositioned) {
				blockNumberToSeek = blockNumberAfterEOD - 2;
			}
			else {
				//throw new BlockMismatchException("Expected blockNumber after EOD " + expectedBlockNumberToBePositioned + ", actual " + blockNumberAfterEOD);
				logger.warn("Expected blockNumber after EOD " + expectedBlockNumberToBePositioned + ", actual " + blockNumberAfterEOD + ". Falling back to seek option");
				blockNumberToSeek =  expectedBlockNumberToBePositioned - 2;
			}

			seek(dataTransferElementName, blockNumberToSeek);
			
			int blockNumberAfterSeek = getCurrentPositionBlockNumber(dataTransferElementName);
			
			// validating the blockNumber - There were few times in test env where we seeked and tried verifying but the block was wrong for some reason. So we are trying to validate this just to doubly ensure...
			if(blockNumberAfterSeek != blockNumberToSeek) {
				throw new Exception("Expected blockNumberToSeek " + blockNumberToSeek + ", blockNumberAfterSeek " + blockNumberAfterSeek);
			}
			
			MtStatus mtStatus = getMtStatus(dataTransferElementName);
			dsd.setMtStatus(mtStatus);
		}catch (Exception e) {
			logger.error("Unable to setTapeHeadPositionForReadingInterArtifactXml " + e.getMessage(), e);
			throw e;
		}
		return dsd;
	}

	public DriveDetails setTapeHeadPositionForWriting(String dataTransferElementName, int blockNumberToBePositioned) throws Exception {
		return setTapeHeadPositionForReading(dataTransferElementName, blockNumberToBePositioned);
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
		logger.debug("Current blocknumber after tell " + blockNumber);
		return blockNumber;
	}

	@Override
	public DriveDetails setTapeHeadPositionForFinalizing(String dataTransferElementName, int blockNumberToBePositioned) throws Exception {
		return setTapeHeadPositionForWriting(dataTransferElementName, blockNumberToBePositioned);
	}
	
	private CommandLineExecutionResponse rewind(String dataTransferElementName) throws Exception {
		logger.debug("Now rewinding - " + dataTransferElementName);
//		synchronized (deviceLockFactory.getDeviceLock(dataTransferElementName)) {
			return executeCommandWithRetries("mt -f " + dataTransferElementName + " rewind");	
//		}
	}
	
	private CommandLineExecutionResponse eod(String dataTransferElementName) throws Exception{
		logger.debug("Now eoding - " + dataTransferElementName);
//		synchronized (deviceLockFactory.getDeviceLock(dataTransferElementName)) {
			return executeCommandWithRetries("mt -f " + dataTransferElementName + " eod"); // TODO - We dont know what the error msgs will be for eod
//		}
	}

	private CommandLineExecutionResponse seek(String dataTransferElementName, int blockNo) throws Exception{
		logger.debug("Now seeking - " + dataTransferElementName + ":" + blockNo);
//		synchronized (deviceLockFactory.getDeviceLock(dataTransferElementName)) {
			return executeCommandWithRetries("mt -f " + dataTransferElementName + " seek " + blockNo); // when tape loaded but not stinit throws "Input/output error"
//		}
		// TODO 
		// 1) Check what happens when tape not loaded..
		// 2) How do we handle for some tapes for which seek fails the first but succeeds in the subsequent attempts that Sameer anna mentioned... 
	}
	
	private CommandLineExecutionResponse tell(String dataTransferElementName) throws Exception{
		logger.debug("Now tell-ing - " + dataTransferElementName);
//		synchronized (deviceLockFactory.getDeviceLock(dataTransferElementName)) {
			return executeCommandWithRetries("mt -f " + dataTransferElementName + " tell");
//		}
	}
	
	
	public CommandLineExecutionResponse executeCommandWithRetries(String command) throws Exception {
		return executeCommandWithRetries(command, 0, errorList);
	}

	public CommandLineExecutionResponse executeCommandWithRetries(String command, int nthRetryAttempt, List<String> errorList) throws Exception {
		logger.debug("Attempt " + nthRetryAttempt + " : " + command);
		CommandLineExecutionResponse commandLineExecutionResponse = null;
		try {
			commandLineExecutionResponse = commandLineExecuter.executeCommand(command);
		} catch (Exception e) {
			String errorMsg = e.getMessage();
			logger.error(command + " failed " + e.getMessage());
			
			if(errorList == null)
				errorList = this.errorList;
			
			if(errorMsg.contains(DRIVE_IO_ERROR) && errorList.contains(DRIVE_IO_ERROR) && nthRetryAttempt == 0) {
				String deviceName = command.split(" ")[2];
				logger.warn("stinit script seems to be not executed on device " + deviceName + ". Running it now");
				String script = configuration.getStagingOpsScriptPath();
				List<String> stinitCommandParamsList = new ArrayList<String>();
				stinitCommandParamsList.add("sudo");
				stinitCommandParamsList.add(script);
				stinitCommandParamsList.add("-t");
				stinitCommandParamsList.add("stinit");
				stinitCommandParamsList.add("-w");
				stinitCommandParamsList.add(deviceName);
				commandLineExecuter.executeCommand(stinitCommandParamsList);
				logger.warn("stinit script on device " + deviceName + " success.");
				
				// re-execute the same command again...
				logger.debug("Now re-executing the same command again " + command);
				commandLineExecutionResponse = executeCommandWithRetries(command, nthRetryAttempt + 1, errorList);
			}
			else if(errorMsg.contains(DRIVE_BUSY_ERROR) && errorList.contains(DRIVE_BUSY_ERROR)  && nthRetryAttempt <= 2){
				logger.debug("Must be a concurrent thread's mt status call... Retrying again");
				commandLineExecutionResponse = executeCommandWithRetries(command, nthRetryAttempt + 1, errorList);
			}
			else
				throw e;
		}
		
		return commandLineExecutionResponse;
	}
}
