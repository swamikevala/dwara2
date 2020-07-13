package org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status;

import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.AbstractTapeLibraryManagerImpl;
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

	
	//		load the tape to be used
	public boolean load(String tapeLibraryName, int seSNo, int driveSNo) throws Exception{
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("mtx -f " + tapeLibraryName + " load " + seSNo + " " + driveSNo);
		logger.trace(cler.getStdOutResponse());
		return true;
	}

	//		unload if any other tape - we should always check the status of the drive before unloading the tape. if the drive is busy we should not unload the tape.., (At the time of writing another thread/process is allowed to unload the tape)
	public boolean unload(String tapeLibraryName, int seSNo, int driveSNo) throws Exception{
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("mtx -f " + tapeLibraryName + " unload " + seSNo + " " + driveSNo);
		logger.trace(cler.getStdOutResponse());
		return true;
	}
	
	public MtxStatus getMtxStatus(String tapeLibraryName) throws Exception {
		String mtxStatusResponse = callMtxStatus(tapeLibraryName);
		MtxStatus mtxStatus = MtxStatusResponseParser.parseMtxStatusResponse(mtxStatusResponse);
		return mtxStatus;
	}
	
	private String callMtxStatus(String tapeLibraryName) throws Exception {
		String mtxStatusResponse = null;

		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("mtx -f " + tapeLibraryName + " status");
		mtxStatusResponse = cler.getStdOutResponse();

		return mtxStatusResponse;
	}
}
