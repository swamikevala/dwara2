package org.ishafoundation.dwaraapi.commandline.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
 * This class is responsible for executing a command in terminal from the local instance running our app...
 * 
 * Local ingest server commandline execution - e.g., ffmpeg
 * 
 */
@Component
public class CommandLineExecuter {

	private static Logger logger = LoggerFactory.getLogger(CommandLineExecuter.class);
	
	@Value("${commandlineExecutor.errorResponseTemporaryLocation}")
	private String commandlineExecutorErrorResponseTemporaryLocation;

	
	public Process createProcess(List<String> commandList) {
		Process proc = null;
		try {
			logger.debug("start "+ commandList);
			ProcessBuilder pb = new ProcessBuilder(commandList);
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
		boolean isComplete = false;
		String failureReason = null;
		StringBuffer stdOutRespBuffer = new StringBuffer();
		StringBuffer stdErrRespBuffer = new StringBuffer();
		
		CommandLineExecutionResponse commandLineExecutionResponse = new CommandLineExecutionResponse();
		
		InputStream in = null;
		InputStream err = null;
		
		try {
			in = proc.getInputStream();
			err = proc.getErrorStream();
			
			byte[] tmp = new byte[1024];

			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					stdOutRespBuffer.append(new String(tmp, 0, i));
				}
				while (err.available() > 0) {
					int i = err.read(tmp, 0, 1024);
					if (i < 0)
						break;
					stdErrRespBuffer.append(new String(tmp, 0, i));
				}				
				if (!proc.isAlive()) {
					if (in.available() > 0 || err.available() > 0)
						continue;
					logger.debug("exit-status: " + proc.exitValue());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}

			if(proc.exitValue() == 0) {
				isComplete = true;
				commandLineExecutionResponse.setStdOutResponse(stdOutRespBuffer.toString());	
			} else {
				isComplete = false;
				String errorMessage = stdErrRespBuffer.toString();
				logger.error("Command responded with error - " + errorMessage);
				File tmpErrorFile = new File(commandErrorFilePathname);
				FileUtils.write(tmpErrorFile, errorMessage);
				List<String> nLInes = FileUtils.readLines(tmpErrorFile);
				failureReason = nLInes.get(nLInes.size() - 1);
				tmpErrorFile.delete();
				commandLineExecutionResponse.setFailureReason(failureReason);
			}
			commandLineExecutionResponse.setIsComplete(isComplete);			

			proc.destroy();
			logger.debug("end "+ commandList);
		}
		catch (Exception ee) {
			logger.error("Unable to execute command " + commandList + " : " + ee.getMessage(), ee);
			commandLineExecutionResponse.setFailureReason(ee.getMessage());
		}finally {
			try {
				in.close();
				err.close();
			} catch (IOException e) {
				logger.error("Unable to close opened Inputstreams " + commandList + " : " + e.getMessage() + " This might impact resources and cause FileDescriptor(Too many files open) problem. Check it out...", e);
			}
			
		}
		return commandLineExecutionResponse;
		
	}
}
