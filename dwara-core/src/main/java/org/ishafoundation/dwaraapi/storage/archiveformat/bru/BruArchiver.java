package org.ishafoundation.dwaraapi.storage.archiveformat.bru;

import java.util.List;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("bru"+DwaraConstants.ARCHIVER_SUFFIX)
@Profile({ "!dev & !stage" })
public class BruArchiver extends AbstractBruArchiver {

	private static final Logger logger = LoggerFactory.getLogger(BruArchiver.class);

	@Override
	protected String executeWriteCommand(List<String> writeCommandParamsList, String artifactName, int volumeBlocksize)
			throws Exception {
		return executeCommand(writeCommandParamsList);
	}

	@Override
	protected String executeRestoreCommand(List<String> restoreCommandParamsList) throws Exception {
		return executeCommand(restoreCommandParamsList);
	}
	
	
	protected String executeCommand(List<String> bruCommandParamsList)
			throws Exception {
		String commandOutput = null;
//		CommandLineExecutionResponse bruCopyCommandLineExecutionResponse = commandLineExecuter.executeCommand(bruCommandParamsList, commandlineExecutorErrorResponseTemporaryFilename + ".err"); // TODO Fix this output file...
//		if(bruCopyCommandLineExecutionResponse.isComplete()) {
//			commandOutput = bruCopyCommandLineExecutionResponse.getStdOutResponse();
//		}else {
//			logger.error("Bru command execution failed " + bruCopyCommandLineExecutionResponse.getFailureReason());
//			throw new Exception("Unable to execute bru command successfully");
//		}
		return commandOutput;
	}
}
