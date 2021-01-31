package org.ishafoundation.dwaraapi.commandline.remote.sch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

/*
 * This class is responsible for executing a command in remote server terminal from our app...
 * 
 * Remote catdv server commandline execution - 
 * 			e.g., untar the zip file and mv it to the proxy location thats tarred and secured copied to the catdv server... 
 * 				also used for deleting the medialibrary specific proxy folder on cancellation/delete  
 * 
 */
@Component
public class RemoteCommandLineExecuter {

	private static Logger logger = LoggerFactory.getLogger(RemoteCommandLineExecuter.class);
	
	@Value("${commandlineExecutor.errorResponseTemporaryLocation}")
	private String commandlineExecutorErrorResponseTemporaryLocation;
	
	public CommandLineExecutionResponse executeCommandRemotelyOnServer(Session session, String command, String commandOutputFilePathName) {
		logger.debug("executing command remotely - " + command);
		boolean isComplete = false;
		String failureReason = null;
		StringBuffer stdOutRespBuffer = new StringBuffer();
		if(!commandOutputFilePathName.contains(File.separator))
			commandOutputFilePathName = commandlineExecutorErrorResponseTemporaryLocation + File.separator + commandOutputFilePathName;
		CommandLineExecutionResponse commandLineExecutionResponse = new CommandLineExecutionResponse();
		
		try {
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(new FileOutputStream(commandOutputFilePathName));

			InputStream in = channel.getInputStream(); // data that we can read.
			// An OutputStream is one where you write data to. Nothing to be written unlike scp... so no need for an OutputStream...
			channel.connect();
			byte[] tmp = new byte[1024];
			
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					stdOutRespBuffer.append(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					logger.info("exit-status: " + channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}

			if(channel.getExitStatus() == 0) {
				isComplete = true;
				commandLineExecutionResponse.setStdOutResponse(stdOutRespBuffer.toString());	
			} else {
				isComplete = false;
				File tmpErrorFile = new File(commandOutputFilePathName);
				List<String> nLInes = FileUtils.readLines(tmpErrorFile);
				failureReason = nLInes.get(nLInes.size() - 1);
				commandLineExecutionResponse.setFailureReason(failureReason);
				tmpErrorFile.delete();
			}
			commandLineExecutionResponse.setIsComplete(isComplete);
			channel.disconnect();
		} catch (Exception ee) {
			logger.debug("Unable to execute command " + command + " : " + ee.getMessage(), ee);
			commandLineExecutionResponse.setFailureReason(ee.getMessage());
		}
		
		return commandLineExecutionResponse;
	}

}
