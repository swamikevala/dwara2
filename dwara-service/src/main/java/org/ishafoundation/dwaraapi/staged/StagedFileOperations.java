package org.ishafoundation.dwaraapi.staged;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.configuration.FilesystemPermissionsConfiguration;
import org.ishafoundation.dwaraapi.staged.scan.Error;
import org.ishafoundation.dwaraapi.staged.scan.Errortype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StagedFileOperations {

	private static final Logger logger = LoggerFactory.getLogger(StagedFileOperations.class);
	
	@Autowired
    private CommandLineExecuter commandLineExecuter;
	
	@Autowired
    private Configuration configuration;

	@Autowired
    private FilesystemPermissionsConfiguration fileSystemPermissionsConfiguration;
	
	public enum StagingOpsAction{
		set_permissions,
		staged_rename
	}

	public Error setPermissions(String sourcePath, String artifactName) {
		Error error = null;
		if(configuration.isSetArtifactFileSystemPermissions()) {
			error = callStagingOpsScript(StagingOpsAction.set_permissions, sourcePath, artifactName, null);
		}
		return error;
	}

	public Error rename(String sourcePath, String artifactName, String newArtifactName) {
		return callStagingOpsScript(StagingOpsAction.staged_rename, sourcePath, artifactName, newArtifactName);
	}
	
	private Error callStagingOpsScript(StagingOpsAction action, String sourcePath, String artifactName, String newArtifactName) { 			
		Error error = null;
		String script = configuration.getStagingOpsScriptPath();
		File toBeActionedFile = FileUtils.getFile(sourcePath, artifactName);
		
		CommandLineExecutionResponse commandLineExecutionResponse = null;
		try {
			commandLineExecutionResponse = callStagingOpsScript(script, action, sourcePath, artifactName, newArtifactName);
			
			if(!commandLineExecutionResponse.isComplete()) {					
				String message = "Unable to " + action.name() + " " + toBeActionedFile + ". " + commandLineExecutionResponse.getFailureReason() + ". Please contact Admin";
				error = new Error();
				error.setType(Errortype.Error);
				error.setMessage(message);
				logger.warn(message);
			}
	
		} catch (Exception e) {
			String message = "Unable to " + action.name() + " " + toBeActionedFile + " : " + e.getMessage() + ". Please contact Admin";
			error = new Error();
			error.setType(Errortype.Error);
			error.setMessage(message);
	
			logger.warn(message);
		}
		
		return error;
	}
	
	
	
	// Returns something like /opt/dwara/bin/setperms -b /data/user -u pgurumurthy -c pub-video -a Shots-Of-Sadhanapada-Particpants-Volunteering-In-BSP_SPH-IYC_24-Oct-2019_Z280V_9 -g dwara -d 0775 -f 0664 -r
    private CommandLineExecutionResponse callStagingOpsScript(String script, StagingOpsAction action, String sourcePath, String artifactName, String newArtifactName) throws Exception {
		String parts[] = sourcePath.split("/");
		String user = parts[3];
		String artifactclassName = parts[5];

		List<String> setFilePermissionsCommandParamsList = new ArrayList<String>();
		setFilePermissionsCommandParamsList.add("sudo");
		setFilePermissionsCommandParamsList.add(script);
		setFilePermissionsCommandParamsList.add("-t");
		setFilePermissionsCommandParamsList.add(action.name());
		setFilePermissionsCommandParamsList.add("-b");
		setFilePermissionsCommandParamsList.add(StringUtils.substringBefore(sourcePath, user));
		setFilePermissionsCommandParamsList.add("-u");
		setFilePermissionsCommandParamsList.add(user);
		setFilePermissionsCommandParamsList.add("-c");
		setFilePermissionsCommandParamsList.add(artifactclassName);
		setFilePermissionsCommandParamsList.add("-a");
		setFilePermissionsCommandParamsList.add(artifactName);
		
		if(action == StagingOpsAction.staged_rename) {
			setFilePermissionsCommandParamsList.add("-n");
			setFilePermissionsCommandParamsList.add(newArtifactName);
		}else {
			String owner = fileSystemPermissionsConfiguration.getOwner();
			if(StringUtils.isNotBlank(owner)) {
				setFilePermissionsCommandParamsList.add("-o");
				setFilePermissionsCommandParamsList.add(owner);
			}
			setFilePermissionsCommandParamsList.add("-g");
			setFilePermissionsCommandParamsList.add(fileSystemPermissionsConfiguration.getGroup());
			setFilePermissionsCommandParamsList.add("-d");
			setFilePermissionsCommandParamsList.add(fileSystemPermissionsConfiguration.getDirectoryMode());
			setFilePermissionsCommandParamsList.add("-f");
			setFilePermissionsCommandParamsList.add(fileSystemPermissionsConfiguration.getFileMode());
			
			if(fileSystemPermissionsConfiguration.isRecursive())
				setFilePermissionsCommandParamsList.add("-r");
		}	
		
		CommandLineExecutionResponse setPermsCommandLineExecutionResponse = commandLineExecuter.executeCommand(setFilePermissionsCommandParamsList);
		return setPermsCommandLineExecutionResponse;
    }

}
