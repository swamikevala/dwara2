package org.ishafoundation.dwaraapi.process.file;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.remote.sch.RemoteCommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.remote.sch.SshSessionHelper;
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
    private RemoteCommandLineExecuter remoteCommandLineExecuter;

	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		Artifact inputArtifact = processContext.getJob().getInputArtifact();
		String inputArtifactName = inputArtifact.getName();
		
		String destinationDirPath = processContext.getOutputDestinationDirPath(); // This includes the host ip
		String destination = StringUtils.substringBefore(destinationDirPath, inputArtifactName);
		
		destinationDirPath = destination + ".copying"; // Reqmt - No need for the filepathname structur as when job fails, leaves the empty folder structure causing confusion
		
		LogicalFile logicalFile = processContext.getLogicalFile();
		
		String sshUser = "dwara"; // TODO Configure this...
		String host = StringUtils.substringBefore(destinationDirPath, ":");
        logger.info("processing rsync copy: " +  logicalFile.getAbsolutePath() + ", destination: " + destinationDirPath);
        
        RSync rsync = new RSync()
        .source(logicalFile.getAbsolutePath())
        .destination(sshUser + "@" + destinationDirPath)
        .recursive(true)
        .checksum(false);
        //.removeSourceFiles(true); // Reqmt - File gets deleted in downstream job
        
        // now moving back the file from the .copying to the original destination...
        Session jSchSession = null;
        try {
        	jSchSession = sshSessionHelper.getSession(host, sshUser);
        	
			String command1 = "mv " + StringUtils.substringAfter(destinationDirPath, ":") + File.separator + logicalFile.getName() + " " + StringUtils.substringAfter(destination, ":");
			logger.trace("executing remotely " + command1);
			remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command1, inputArtifactName + ".out_mv_qcErr");
        }finally {
			if (jSchSession != null) 
				sshSessionHelper.disconnectSession(jSchSession);
		}
        


        // TODO - .copying

        
        CollectingProcessOutput output = rsync.execute();
        logger.info(output.getStdOut());
        logger.info("Exit code: " + output.getExitCode());
        ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
        processingtaskResponse.setIsComplete(output.getExitCode() == 0);
        processingtaskResponse.setFailureReason(output.getExitCode() + ":" + output.getStdErr());
        processingtaskResponse.setStdOutResponse(output.getStdOut());

        
        /*
        if(output.getExitCode() < 0) {
            processingtaskResponse.setIsComplete(false);
        }
        else {
            //verify checksum
            rsync.dryRun(true);
            ProcessBuilder builder = rsync.builder();
            builder.redirectErrorStream(true);
            Process process = builder.start();
            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            int numFilesTransferred = -1;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
                if(line.contains("Number of files transferred")) {
                    numFilesTransferred = Integer.parseInt(line.replace("Number of files transferred: ", ""));
                }
                else if(line.contains("Number of regular files transferred")) {
                    numFilesTransferred = Integer.parseInt(line.replace("Number of regular files transferred: ", ""));
                }
            }
            is.close();
            reader.close();

            if(numFilesTransferred == 0) {
                processingtaskResponse.setIsComplete(true);
            }
            else {
                processingtaskResponse.setIsComplete(false);
            } 
        }*/
		return processingtaskResponse;
	}

}

