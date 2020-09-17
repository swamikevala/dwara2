package org.ishafoundation.dwaraapi.commandline.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RetriableCommandLineExecutorImpl extends CommandLineExecuterImpl{

	private static Logger logger = LoggerFactory.getLogger(RetriableCommandLineExecutorImpl.class);
	
	public CommandLineExecutionResponse executeCommandWithRetriesOnSpecificError(String command, String errorMsg) throws Exception {
		String[] commandArgs = command.split(" ");
		List<String> commandList = Arrays.asList(commandArgs);
		
		return executeCommandWithRetries(commandList, 0, errorMsg, true);
	}

	public CommandLineExecutionResponse executeCommandWithRetriesOnSpecificError(List<String> commandParamsList, String errorMsg) throws Exception {
		return executeCommandWithRetries(commandParamsList, 0, errorMsg, true);
	}
	
	public CommandLineExecutionResponse executeCommandWithRetriesOnSpecificError(List<String> commandParamsList, String errorMsg, boolean extractLastLineAsFailureReason) throws Exception {
		return executeCommandWithRetries(commandParamsList, 0, errorMsg, extractLastLineAsFailureReason);
	}
	
	public CommandLineExecutionResponse executeCommandWithRetries(List<String> commandParamsList,
			int nthRetryAttempt, String errorMsg, boolean extractLastLineAsFailureReason) throws Exception {
		CommandLineExecutionResponse commandLineExecutionResponse = null;
		try {
			commandLineExecutionResponse = executeCommand(commandParamsList, extractLastLineAsFailureReason);
		} catch (Exception e) {
			String errorMessage = e.getMessage();
			logger.error(commandParamsList + " failed " + e.getMessage());
			
			if(errorMessage.contains(errorMsg) && nthRetryAttempt <= 2){
				logger.debug("Must be a parallel mt status call... Retrying again");
				executeCommandWithRetries(commandParamsList, nthRetryAttempt + 1, errorMsg, extractLastLineAsFailureReason);
			}
			else
				throw e;
		}
		
		return commandLineExecutionResponse;
	}


}
