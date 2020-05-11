package org.ishafoundation.dwaraapi.z_mocks.commandline.local;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
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
public class CommandLineExecuterImpl implements CommandLineExecuter{

	private static Logger logger = LoggerFactory.getLogger(CommandLineExecuterImpl.class);
	
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
	
	public CommandLineExecutionResponse executeCommand(String command){
		String filename = command.replace(" ", "_").replace("/", "_");
		return executeCommand(command, commandlineExecutorErrorResponseTemporaryLocation + File.separator + filename + ".err");
	}
	
	public CommandLineExecutionResponse executeCommand(String command, String commandErrorFilePathname){
		String[] commandArgs = command.split(" ");
		List<String> commandList = Arrays.asList(commandArgs);

		CommandLineExecutionResponse commandLineExecutionResponse = executeCommand(commandList, commandErrorFilePathname);
		if(commandLineExecutionResponse.isComplete()) {
			logger.trace(command + " executed successfully " + commandLineExecutionResponse.getStdOutResponse());
		}
		else
			logger.error(command + " execution failed " + commandLineExecutionResponse.getFailureReason());
		return commandLineExecutionResponse;
	}
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList, String commandErrorFilePathname) {
		if(!commandErrorFilePathname.contains(File.separator))
			commandErrorFilePathname = commandlineExecutorErrorResponseTemporaryLocation + File.separator + commandErrorFilePathname;
		return executeCommand(commandList, createProcess(commandList), commandErrorFilePathname);
	}
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList, Process proc, String commandErrorFilePathname) {
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
