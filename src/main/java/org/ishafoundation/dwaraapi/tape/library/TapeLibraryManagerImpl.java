package org.ishafoundation.dwaraapi.tape.library;

import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.tape.library.status.MtxStatus;
import org.ishafoundation.dwaraapi.tape.library.status.MtxStatusResponseParser;
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
	@Override
	public boolean load(String tapeLibraryName, int seSNo, int driveSNo){
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("mtx -f " + tapeLibraryName + " load " + seSNo + " " + driveSNo);
		logger.debug(cler.getStdOutResponse());
		return true;
	}

	//		unload if any other tape - we should always check the status of the drive before unloading the tape. if the drive is busy we should not unload the tape.., (At the time of writing another thread/process is allowed to unload the tape)
	@Override
	public boolean unload(String tapeLibraryName, int seSNo, int driveSNo){
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("mtx -f " + tapeLibraryName + " unload " + seSNo + " " + driveSNo);
		logger.debug(cler.getStdOutResponse());
		return true;
	}
	
	@Override
	protected MtxStatus getMtxStatus(String tapeLibraryName){
		String mtxStatusResponse = callMtxStatus(tapeLibraryName);
		MtxStatus mtxStatus = MtxStatusResponseParser.parseMtxStatusResponse(mtxStatusResponse);
		return mtxStatus;
	}
	
	private String callMtxStatus(String tapeLibraryName) {
		String mtxStatusResponse = null;

		// will make a call to mtx and get the status realtime...
		String mtxStatusResponseFileName = tapeLibraryName.replace("/", "_") + "_status.err";
		CommandLineExecutionResponse cler = commandLineExecuter.executeCommand("mtx -f " + tapeLibraryName + " status", mtxStatusResponseFileName);
		mtxStatusResponse = cler.getStdOutResponse();

		return mtxStatusResponse;
	}	

}
