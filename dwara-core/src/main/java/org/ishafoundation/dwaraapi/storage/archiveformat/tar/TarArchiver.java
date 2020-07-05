package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import java.util.List;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("tar"+DwaraConstants.ARCHIVER_SUFFIX)
@Profile({ "!dev & !stage" })
public class TarArchiver extends AbstractTarArchiver {
	
	private static final Logger logger = LoggerFactory.getLogger(TarArchiver.class);

	@Override
	protected String executeCommand(List<String> tarCommandParamsList, String artifactName, int volumeBlocksize)
			throws Exception {

		String commandOutput = null;
//		CommandLineExecutionResponse tarCopyCommandLineExecutionResponse = commandLineExecuter.executeCommand(tarCommandParamsList, commandlineExecutorErrorResponseTemporaryFilename + ".err"); // TODO Fix this output file...
//		if(tarCopyCommandLineExecutionResponse.isComplete()) {
//			commandOutput = tarCopyCommandLineExecutionResponse.getStdOutResponse();
//		}else {
//			logger.error("tar command execution failed " + tarCopyCommandLineExecutionResponse.getFailureReason());
//			throw new Exception("Unable to execute tar command successfully");
//		}
		return commandOutput;
	}
}
