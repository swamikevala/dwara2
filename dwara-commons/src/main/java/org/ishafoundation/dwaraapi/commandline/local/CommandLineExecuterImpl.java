package org.ishafoundation.dwaraapi.commandline.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
	
	public Process createProcess(String command) throws Exception {
		Process proc = null;
		try {
			logger.debug("start "+ command);
			ProcessBuilder pb = new ProcessBuilder(command);
			proc = pb.start();
		}
		catch (Exception ee) {
			logger.error("Unable to create process " + command + " : " + ee.getMessage(), ee);
			throw ee;
		}
		return proc;
	}
	
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
		return executeCommand(command, true);
	}
	
	public CommandLineExecutionResponse executeCommand(String command, boolean extractLastLineAsFailureReason) throws Exception{
		String[] commandArgs = command.split(" ");
		List<String> commandList = Arrays.asList(commandArgs);

		CommandLineExecutionResponse commandLineExecutionResponse = executeCommand(commandList, extractLastLineAsFailureReason);
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
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList, boolean extractLastLineAsFailureReason) throws Exception {
		return executeCommand(commandList, createProcess(commandList), extractLastLineAsFailureReason);
	}

	public CommandLineExecutionResponse executeCommand(String command, Process proc) throws Exception {
		return executeCommand(command, proc, true);
	}
	
	public CommandLineExecutionResponse executeCommand(String command, Process proc, boolean extractLastLineAsFailureReason) throws Exception {
		boolean isComplete = false;
		
		CommandLineExecutionResponse commandLineExecutionResponse = new CommandLineExecutionResponse();
		
		InputStream in = null;
		InputStream err = null;
		
		StringBuffer stdOutRespBuffer = new StringBuffer(); // stdout channel
		StringBuffer stdErrRespBuffer = new StringBuffer(); // stderr channel		
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

			commandLineExecutionResponse.setStdOutResponse(stdOutRespBuffer.toString());
			commandLineExecutionResponse.setExitCode(proc.exitValue());
			if(proc.exitValue() == 0) {
				isComplete = true;
			} else { // warning or error - set it as a failure...
				isComplete = false;
				String stdErrResp = stdErrRespBuffer.toString();
				String message = "Failed - no error message received";
				if(extractLastLineAsFailureReason) {
					message = getLastLine(stdErrResp);
					if(message == null)
						message = stdErrResp;
				}else {
					message = stdErrResp;
				}
				
				if(proc.exitValue() == 1 && (message.startsWith("bru: [W042]") || message.startsWith("mbuffer: error: outputThread: error writing to <stdout> at offset "))) // bru hack
					isComplete= true;
				else
					throw new Exception(message);
			}
			commandLineExecutionResponse.setIsComplete(isComplete);			

			logger.debug("end "+ command);
		}
		catch (Exception ee) {
			logger.error(command + " execution failed : " + ee.getMessage(), ee);
			commandLineExecutionResponse.setFailureReason(ee.getMessage());
			throw ee;
		}finally {
			try {
				if(StringUtils.isNotBlank(stdOutRespBuffer.toString()))
					logger.trace("Command's stdout message - " + stdOutRespBuffer.toString());
				if(StringUtils.isNotBlank(stdErrRespBuffer.toString()))
					logger.trace("Command's stderr message - " + stdErrRespBuffer.toString());

				proc.destroy();
				in.close();
				err.close();
			} catch (IOException e) {
				logger.error("Unable to close opened Inputstreams " + command + " : " + e.getMessage() + " This might impact resources and cause FileDescriptor(Too many files open) problem. Check it out...", e);
			}
			
		}
		return commandLineExecutionResponse;
	}
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList, Process proc) throws Exception {
		return executeCommand(commandList, proc, true);
	}
	
	public CommandLineExecutionResponse executeCommand(List<String> commandList, Process proc, boolean extractLastLineAsFailureReason) throws Exception {
		return executeCommand(commandList.toString(), proc, extractLastLineAsFailureReason);
	}

	// Not sure how to get the last line string just by reading the stream without having to write it to a file...
	private String getLastLine(String errorMessage){
		String failureReason = null;
		String filename = commandlineExecutorErrorResponseTemporaryLocation + File.separator + Thread.currentThread().getName() + "_" + System.currentTimeMillis() + ".err";
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
