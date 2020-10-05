package org.ishafoundation.dwaraapi.commandline.local;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/*
 * This class is responsible for executing a command in terminal from the local instance running our app...
 * 
 * Local ingest server commandline execution - e.g., ffmpeg
 * 
 */
@Component
@Profile({ "dev | stage" })
public class MockCommandLineExecuterImpl extends RetriableCommandLineExecutorImpl{

	private static Logger logger = LoggerFactory.getLogger(MockCommandLineExecuterImpl.class);
	
	@Value("${commandlineExecutor.errorResponseTemporaryLocation}")
	private String commandlineExecutorErrorResponseTemporaryLocation;

	
	public Process createProcess(List<String> commandList) {
		Process proc = null;
		try {
			// Some dummy command...
			List<String> cl2 = new ArrayList<String>();
			cl2.add("ffmpeg");
			cl2.add("-version");
			
			ProcessBuilder pb = new ProcessBuilder(cl2);
			proc = pb.start();
		}
		catch (Exception ee) {
			logger.error("Unable to create process " + commandList + " : " + ee.getMessage(), ee);
		}
		return proc;
	}

	@Override
	public org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse executeCommand(
			List<String> commandList, Process proc) throws Exception {
		boolean isComplete = true;
		CommandLineExecutionResponse commandLineExecutionResponse = new CommandLineExecutionResponse();
		
		try {
			commandLineExecutionResponse.setStdOutResponse("Some string");	
			commandLineExecutionResponse.setIsComplete(isComplete);			
		}
		catch (Exception ee) {
			logger.error("Unable to execute command " + commandList + " : " + ee.getMessage(), ee);
			commandLineExecutionResponse.setFailureReason(ee.getMessage());
		}finally {
		}
		return commandLineExecutionResponse;
	}
	
}
