package org.ishafoundation.dwaraapi.commandline.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/*
 * This class is responsible for executing a command in terminal from the local instance running our app...
 * 
 * Local ingest server commandline execution - e.g., ffmpeg
 * 
 */
@Component
@Primary
@Profile({ "!dev & !stage" })
public class CommandLineExecuterImpl implements CommandLineExecuter{

	private static Logger logger = LoggerFactory.getLogger(CommandLineExecuterImpl.class);
	
	@Value("${commandlineExecutor.errorResponseTemporaryLocation}")
	private String commandlineExecutorErrorResponseTemporaryLocation;

	
	public Process createProcess(List<String> commandList) throws Exception {
		Process proc = null;
		try {
			logger.debug("start "+ commandList);
			ProcessBuilder pb = new ProcessBuilder(commandList);
			proc = pb.start();
		}
		catch (Exception ee) {
			logger.error("Unable to create process " + commandList + " : " + ee.getMessage(), ee);
			throw ee;
		}
		return proc;
	}
	
	public CommandLineExecutionResponse executeCommand(String command) throws Exception{
		String[] commandArgs = command.split(" ");
		List<String> commandList = Arrays.asList(commandArgs);

		CommandLineExecutionResponse commandLineExecutionResponse = executeCommand(commandList);
		if(commandLineExecutionResponse.isComplete()) {
			logger.trace(command + " executed successfully " + commandLineExecutionResponse.getStdOutResponse());
		}
		else
			logger.error(command + " execution failed " + commandLineExecutionResponse.getFailureReason());
		return commandLineExecutionResponse;
	}
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList) throws Exception {
		return executeCommand(commandList, createProcess(commandList));
	}

	public CommandLineExecutionResponse executeCommand(List<String> commandList, Process proc) throws Exception {
		boolean isComplete = false;
		String failureReason = null;
		
		CommandLineExecutionResponse commandLineExecutionResponse = new CommandLineExecutionResponse();
		
		InputStream in = null;
		InputStream err = null;
		
		try {
			in = proc.getInputStream();
			err = proc.getErrorStream();

//			String stdOutResp = copy(in);
//			String stdErrResp = copy(in);
//			
//			if(proc.exitValue() == 0) {
//				isComplete = true;
//				commandLineExecutionResponse.setStdOutResponse(stdOutResp);	
//			} else {
//				isComplete = false;
//				failureReason = getFailureReason(stdErrResp);
//				commandLineExecutionResponse.setFailureReason(failureReason);
//				throw new Exception(failureReason);
//			}
			
			StringBuffer stdOutRespBuffer = new StringBuffer();
			StringBuffer stdErrRespBuffer = new StringBuffer();

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
				failureReason = getFailureReason(stdErrRespBuffer.toString());
				commandLineExecutionResponse.setFailureReason(failureReason);
				throw new Exception(failureReason);
			}
			commandLineExecutionResponse.setIsComplete(isComplete);			

			logger.debug("end "+ commandList);
		}
		catch (Exception ee) {
			logger.error("Unable to execute command " + commandList + " : " + ee.getMessage(), ee);
			commandLineExecutionResponse.setFailureReason(ee.getMessage());
			throw ee;
		}finally {
			try {
				proc.destroy();
				in.close();
				err.close();
			} catch (IOException e) {
				logger.error("Unable to close opened Inputstreams " + commandList + " : " + e.getMessage() + " This might impact resources and cause FileDescriptor(Too many files open) problem. Check it out...", e);
			}
			
		}
		return commandLineExecutionResponse;
		
	}
	
	
	private String copy(InputStream is) throws IOException {
        int n = 0;
        byte[] tmp = new byte[1024];
        StringBuffer respBuffer = new StringBuffer();
        while (-1 != (n = is.read(tmp))) {
        	respBuffer.append(new String(tmp, 0, n));
        }
        return respBuffer.toString();
	}
	
	// Not sure how to get the last line string just by reading the stream without having to write it to a file...
	private String getFailureReason(String errorMessage){
		logger.error("Command responded with error - " + errorMessage);
		String failureReason = null;
		String filename = commandlineExecutorErrorResponseTemporaryLocation + File.separator + System.currentTimeMillis() + ".err";
		try {
			File tmpErrorFile = new File(filename);
			FileUtils.write(tmpErrorFile, errorMessage);
			List<String> nLInes = FileUtils.readLines(tmpErrorFile);
			failureReason = nLInes.get(nLInes.size() - 1);
			tmpErrorFile.delete();
		}catch (Exception e) {
			logger.error("unable to write the message to tmp file " + filename);
		}
		return failureReason;
	}
}
