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
		CommandLineExecutionResponse bruCopyCommandLineExecutionResponse = retriableCommandLineExecutorImpl.executeCommandWithRetriesOnSpecificError(bruCommandParamsList, DwaraConstants.DRIVE_BUSY_ERROR);
		if(bruCopyCommandLineExecutionResponse.isComplete()) {
			commandOutput = bruCopyCommandLineExecutionResponse.getStdOutResponse();
		}else {
			logger.error("Bru command execution failed " + bruCopyCommandLineExecutionResponse.getFailureReason());
			throw new Exception("Unable to execute bru command successfully");
		}
		return commandOutput;
	}
	
	@Override
	protected String executeRestoreCommand(List<String> restoreCommandParamsList) throws Exception {
		String mtStatusResponse = null;
		CommandLineExecutionResponse cler = null;
		try {
			cler = retriableCommandLineExecutorImpl.executeCommandWithRetriesOnSpecificError(restoreCommandParamsList, DwaraConstants.DRIVE_BUSY_ERROR, false);
			if(cler.isComplete())
				mtStatusResponse = cler.getStdOutResponse();
		}
		catch (Exception e) {
			String errorMsg = e.getMessage();
			/* Something like
					bru: [W042] "V25155_25155_12290_In-The-Lap-Of-The-Master_Day1-Evening_Q-And-A-Session_AYA-IYC_07-Jul-2017_Cam6_Osmo_B-Rolls": warning - error setting owner/group: errno = 1, Operation not permitted
					^Min @ 78.0 MiB/s, out @ 60.8 MiB/s, 67.0 MiB total, buffer   2% fullbru: [W042] "V25155_25155_12290_In-The-Lap-Of-The-Master_Day1-Evening_Q-And-A-Session_AYA-IYC_07-Jul-2017_Cam6_Osmo_B-Rolls/DJI_0129.MOV": warning - error setting owner/group: errno = 1, Operation not permitted
					mbuffer: error: outputThread: error writing to <stdout> at offset 0x4310000: Broken pipe
			 */
			if(errorMsg.contains("bru: [E"))
				throw e;
			else
				mtStatusResponse = errorMsg;
		}
		return mtStatusResponse;
	}

}
