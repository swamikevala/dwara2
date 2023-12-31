package org.ishafoundation.dwaraapi.process.file;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.commandline.remote.sch.RemoteCommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.remote.sch.SshSessionHelper;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.Artifact;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput;
import com.github.fracpete.rsync4j.RSync;
import com.jcraft.jsch.Session;

@Component("file-copy")
public class RsyncCopier implements IProcessingTask {

	private static final Logger logger = LoggerFactory.getLogger(RsyncCopier.class);
	
	@Autowired
	private SshSessionHelper sshSessionHelper;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
    private RemoteCommandLineExecuter remoteCommandLineExecuter;

	// TODO- Make this as a generic copier with local vs remote and scp vs rsync options - rsync with checksum and other options configurable as well
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		LogicalFile logicalFile = processContext.getLogicalFile();
		
		Artifact inputArtifact = processContext.getJob().getInputArtifact();
		String inputArtifactName = inputArtifact.getName();
		
		String destinationDirPath = processContext.getOutputDestinationDirPath(); // This includes the host ip
		logger.trace("destinationDirPath " + destinationDirPath);

		String sshUser = configuration.getSshSystemUser();
		String host = StringUtils.substringBefore(destinationDirPath, ":");
        
        ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
        
        int jobId = processContext.getJob().getId();
        
		String destinationFilePathname = null;
		// TODO --- REMOVE THIS AFTER DIGI IS OVER... 
		if(inputArtifact.getArtifactclass().getId().startsWith("video-digi-2020")) {
			String destination = StringUtils.substringBefore(destinationDirPath, inputArtifactName);
			logger.trace("destination " + destination);
			destinationFilePathname = destination + ".copying" + File.separator + logicalFile.getName(); // Reqmt - No need for the filepathname structur as when job fails, leaves the empty folder structure causing confusion
		}
		else {
			destinationFilePathname = destinationDirPath;
			String parentDir = FilenameUtils.getFullPathNoEndSeparator(destinationDirPath);
			String command1 = "mkdir -p \"" + parentDir + "\"";
			
			processingtaskResponse = executeCommandRemotely(host, sshUser, command1, jobId, processingtaskResponse);
			if(!processingtaskResponse.isComplete())
				throw new Exception("Unable to create dir remotely " + processingtaskResponse.getFailureReason());
		}
		logger.info("processing rsync copy: " +  logicalFile.getAbsolutePath() + ", destination: " + destinationFilePathname);
        
        RSync rsync = new RSync()
        .source(logicalFile.getAbsolutePath())
        .destination(sshUser + "@" + destinationFilePathname)
        .recursive(true)
        .checksum(configuration.isChecksumRsync())
        .bwlimit(configuration.getBwLimitRsync());
        
        //.removeSourceFiles(true); // Reqmt - File gets deleted in downstream job
        
        CollectingProcessOutput output = rsync.execute();
        
        logger.info(output.getStdOut());
        logger.info("Exit code: " + output.getExitCode());
 
		// TODO --- REMOVE THIS AFTER DIGI IS OVER...
		if(output.getExitCode() == 0 && inputArtifact.getArtifactclass().getId().startsWith("video-digi-2020")){
	        // now moving back the file from the .copying to the original destination...
	        String tmpDestination = StringUtils.substringAfter(destinationFilePathname, ":");
	        String command1 = "mv " + tmpDestination + " " + StringUtils.substringBefore(tmpDestination, ".copying");
	        
	        processingtaskResponse = executeCommandRemotely(host, sshUser, command1, jobId, processingtaskResponse);
		}else {
	        processingtaskResponse.setIsComplete(output.getExitCode() == 0);
	        processingtaskResponse.setFailureReason(output.getExitCode() + ":" + output.getStdErr());
	        processingtaskResponse.setStdOutResponse(output.getStdOut());
		}

		return processingtaskResponse;
	}

	
	private ProcessingtaskResponse executeCommandRemotely(String host, String sshUser, String command1, int jobId, ProcessingtaskResponse processingtaskResponse) {
		Session jSchSession = null;
        CommandLineExecutionResponse response = null;
        try {
        	jSchSession = sshSessionHelper.getSession(host, sshUser);
			logger.trace("executing remotely " + command1);
			response = remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command1, jobId + ".out_err");
			if(response.isComplete()) {
		        processingtaskResponse.setIsComplete(true);
		        processingtaskResponse.setStdOutResponse(response.getStdOutResponse());
			}else {
				throw new Exception("Remotecommand executer failed");
			}
        }catch (Exception e) {
        	processingtaskResponse.setIsComplete(false);
        	processingtaskResponse.setFailureReason(response.getFailureReason());
        	logger.error("Unable to execute " + command1 + " remotely" + e.getMessage(), e);
		}finally {
			if (jSchSession != null) 
				sshSessionHelper.disconnectSession(jSchSession);
		}
        return  processingtaskResponse;
	}
}

