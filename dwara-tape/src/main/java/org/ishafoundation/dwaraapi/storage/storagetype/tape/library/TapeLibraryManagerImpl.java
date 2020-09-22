package org.ishafoundation.dwaraapi.storage.storagetype.tape.library;

import java.util.List;

import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.StorageElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.status.MtxStatus;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.status.MtxStatusResponseParser;
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
public class TapeLibraryManagerImpl extends AbstractTapeLibraryManagerImpl{
	
	private static Logger logger = LoggerFactory.getLogger(TapeLibraryManagerImpl.class);
	
	@Autowired
	private CommandLineExecuter commandLineExecuter;
	/*
	 * Commenting out synchronisation as the physical tape library behaves as follows...
	order of commands entered: 1. unload, 2. unload, 3. status
	2 & 3 just wait... no message
	when 1 finishes then 3 is executed
	
	@Autowired
	private DeviceLockFactory deviceLockFactory;
	*/
	// TODO Hardcoded stuff... Move it as configurable
	private int retryInterval = 60000; // 60 secs = 1 mt
	private int retryAttempts = 10;
	
	
	public MtxStatus getMtxStatus(String tapeLibraryName) throws Exception {
		String mtxStatusResponse = callMtxStatus(tapeLibraryName);
		MtxStatus mtxStatus = MtxStatusResponseParser.parseMtxStatusResponse(mtxStatusResponse);
		return mtxStatus;
	}
	
	private String callMtxStatus(String tapeLibraryName) throws Exception {
		logger.trace("Now enquiring status of library " + tapeLibraryName);
//		synchronized (deviceLockFactory.getDeviceLock(tapeLibraryName)) {
			String mtxStatusResponse = null;
			CommandLineExecutionResponse cler = execute("mtx -f " + tapeLibraryName + " status", 0);
			mtxStatusResponse = cler.getStdOutResponse();
	
			return mtxStatusResponse;
//		}
	}
	
	//		load the tape to be used
	public boolean load(String tapeLibraryName, int seSNo, int driveSNo) throws Exception{
		logger.trace("Now loading media from Storage Element " + seSNo + " to drive " + driveSNo + " on " + tapeLibraryName);
//		synchronized (deviceLockFactory.getDeviceLock(tapeLibraryName)) {
			// TODO Handle the exception...
			CommandLineExecutionResponse cler;
			try {
				cler = execute("mtx -f " + tapeLibraryName + " load " + seSNo + " " + driveSNo, 0);
			} catch (Exception e) {
				// TODO : Regex this or even better look for mtx: Request Sense: Sense Key=Illegal Request
				if(e.getMessage().contains("MOVE MEDIUM from Element Address")) {			//MOVE MEDIUM from Element Address 1021 to 5 Failed
					throw new Exception("Generation not supported.", e);
				}
				throw e;
			}
			
			logger.trace(cler.getStdOutResponse());
			return true;
//		}
	}

	//		unload if any other tape - we should always check the status of the drive before unloading the tape. if the drive is busy we should not unload the tape.., (At the time of writing another thread/process is allowed to unload the tape)
	public boolean unload(String tapeLibraryName, int driveSNo) throws Exception{
		logger.trace("Now getting an empty slot");
		int seSNo = -9;
		MtxStatus mtxStatus = getMtxStatus(tapeLibraryName);
		List<StorageElement> storageElementsList = mtxStatus.getSeList();
		
		for (StorageElement storageElement : storageElementsList) {
			logger.trace("checking " + storageElement.getsNo());
			if(storageElement.isEmpty()) {
				seSNo = storageElement.getsNo();
				logger.trace("selected " + seSNo);
				break;
			}
		}

		if(seSNo == -9) {
			String errorMsg = "Can this happen really???. No empty storagelements huh??? unbelievable";
			logger.error(errorMsg);
			throw new Exception(errorMsg);
		}
		
		unload(tapeLibraryName, seSNo, driveSNo);

		return true;
	}

	@Override
	public boolean unload(String tapeLibraryName, int storageElementSNo, int dataTransferElementSNo) throws Exception {
		logger.trace("Now unloading - " + " from drive " + dataTransferElementSNo + " to slot " + storageElementSNo + " on " + tapeLibraryName);
//		synchronized (deviceLockFactory.getDeviceLock(tapeLibraryName)) {
			CommandLineExecutionResponse cler = execute("mtx -f " + tapeLibraryName + " unload " + storageElementSNo + " " + dataTransferElementSNo, 0);
			logger.trace(cler.getStdOutResponse());
			return true;
//		}
	}

	private CommandLineExecutionResponse execute(String command, int retryCount) throws Exception {
		logger.debug("Executing command - " + command);
		CommandLineExecutionResponse commandLineExecutionResponse = null;
		try {
			commandLineExecutionResponse = commandLineExecuter.executeCommand(command);
		} catch (Exception e) {
			if(e.getMessage().contains("READ ELEMENT STATUS Command Failed")) {
				if(retryCount > retryAttempts) {
					logger.debug("retryCount " + retryCount);
					logger.info("Library not ready. Perhaps the magazine is open or scanning in progress. Will try again after " + retryInterval);
					Thread.sleep(retryInterval);
					
					// re-execute the same command again...
					execute(command, retryCount + 1);
				}
			}
			else
				throw e;
		}
		
		return commandLineExecutionResponse;
	}
}
