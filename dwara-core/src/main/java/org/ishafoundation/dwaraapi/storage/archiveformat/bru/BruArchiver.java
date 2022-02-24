package org.ishafoundation.dwaraapi.storage.archiveformat.bru;

import java.util.List;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.commandline.local.RetriableCommandLineExecutorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("bru"+DwaraConstants.ARCHIVER_SUFFIX)
@Profile({ "!dev & !stage" })
public class BruArchiver extends AbstractBruArchiver {

	private static final Logger logger = LoggerFactory.getLogger(BruArchiver.class);
	
	@Autowired
	private RetriableCommandLineExecutorImpl retriableCommandLineExecutorImpl;
	
	@Override
	protected String executeWriteCommand(List<String> writeCommandParamsList, String artifactName, int volumeBlocksize)
			throws Exception {
		return executeCommand(writeCommandParamsList);
	}
	
	private String executeCommand(List<String> bruCommandParamsList)
			throws Exception {
		String commandOutput = null;
		CommandLineExecutionResponse bruCopyCommandLineExecutionResponse = null;
		try {
			bruCopyCommandLineExecutionResponse = retriableCommandLineExecutorImpl.executeCommandWithRetriesOnSpecificError(bruCommandParamsList, DwaraConstants.DRIVE_BUSY_ERROR);
			if(bruCopyCommandLineExecutionResponse.isComplete())
				commandOutput = bruCopyCommandLineExecutionResponse.getStdOutResponse();
		}catch (Exception e) {
				logger.error("Bru command execution failed " + e.getMessage());
				throw e;
		}
 
		return commandOutput;
	}
	
	@Override
	protected String executeRestoreCommand(List<String> restoreCommandParamsList) throws Exception {
		return executeCommand(restoreCommandParamsList);
	}

}
