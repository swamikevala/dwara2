package org.ishafoundation.videopub.transcoding.ffmpeg.video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.commandline.remote.sch.RemoteCommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.remote.sch.SshSessionHelper;
import org.ishafoundation.dwaraapi.commandline.remote.scp.SecuredCopier;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.Artifact;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.videopub.audio.AudioConfiguration;
import org.ishafoundation.videopub.audio.RemoteTranscodingConfiguration;
import org.ishafoundation.videopub.transcoding.ffmpeg.MediaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Session;

@Component("video-4k-hd-gen")
@Primary
@Profile({ "!dev & !stage" })
public class Video_4K_to_HD_Transcoding_TaskExecutor extends MediaTask implements IProcessingTask{

	private static final Logger logger = LoggerFactory.getLogger(Video_4K_to_HD_Transcoding_TaskExecutor.class);
	
	@Autowired
	private RemoteTranscodingConfiguration remoteTranscodingConfiguration;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private CommandLineExecuter commandLineExecuter;
	
	@Autowired
	private SshSessionHelper sshSessionHelper;
	
	@Autowired
	private RemoteCommandLineExecuter remoteCommandLineExecuter;
	
	@Autowired
	private SecuredCopier securedCopier;
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		String taskName = processContext.getJob().getProcessingtaskId();
		LogicalFile logicalFile = processContext.getLogicalFile();
		Integer fileId = processContext.getFile().getId();
		String destinationDirPath = processContext.getOutputDestinationDirPath();

		Artifact inputArtifact = processContext.getJob().getInputArtifact();
		String inputArtifactName = inputArtifact.getName();
		
		Artifact outputArtifact = processContext.getJob().getOutputArtifact();
		String outputArtifactName = outputArtifact.getName();
		
		if(logger.isTraceEnabled()) {
			logger.trace("taskName " + taskName);
			logger.trace("inputArtifactName " + inputArtifactName); // V22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
			logger.trace("outputArtifactName " + outputArtifactName); // VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
			logger.trace("fileId " + fileId);
//			logger.trace("domain " + domain.name()); 
			logger.trace("logicalFile " + logicalFile.getAbsolutePath()); // /data/ingested/V22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12/DCIM/100EOS5D/MVI_5594.MOV
//			logger.trace("category " + category); // public
			logger.trace("destinationDirPath " + destinationDirPath); // /data/transcoded/public/VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12/DCIM/100EOS5D
		}
		
		String sourceFilePathname = logicalFile.getAbsolutePath();

		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		
		if(fileName.equals(FilenameUtils.getBaseName(inputArtifactName))) {
			fileName = outputArtifactName;
		}
			
		String proxyTargetLocation = destinationDirPath + File.separator + fileName + ".mov";

		// TODO : better this...
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setDestinationPathname(proxyTargetLocation);
		
		String s2RootLocation = remoteTranscodingConfiguration.getSshRootLocation();
		String s2SourceFilePathname = sourceFilePathname.replace(configuration.getRemoteRestoreLocation(), s2RootLocation);
		String s2DestinationDirPath = destinationDirPath.replace(configuration.getRemoteRestoreLocation(), s2RootLocation);
		String s2ProxyTargetLocation = proxyTargetLocation.replace(configuration.getRemoteRestoreLocation(), s2RootLocation);
		
		String host = remoteTranscodingConfiguration.getHost();
		String sshUser = remoteTranscodingConfiguration.getSshSystemUser();
		int jobId = processContext.getJob().getId();
		
		String dirCreationCommand = "mkdir -p \"" + s2DestinationDirPath + "\"";
		processingtaskResponse = executeCommandRemotely(host, sshUser, dirCreationCommand, jobId+"a", processingtaskResponse);
		if(!processingtaskResponse.isComplete())
			throw new Exception("Unable to create dir remotely " + processingtaskResponse.getFailureReason());

		//ffmpeg -y -i 6FX36208.MP4 -c:v libx264 -profile:v high10 -b:v 50M -pix_fmt yuv420p10le -c:a aac -b:a 256k -map 0:v:0 -map 0:a? -map_metadata 0 -s 1920x1080 6FX36208.MOV
		String convCommand = "ffmpeg -y -i " + s2SourceFilePathname + " -c:v libx264 -profile:v high10 -b:v 50M -pix_fmt yuv420p10le -c:a aac -b:a 256k -map 0:v:0 -map 0:a? -map_metadata 0 -s 1920x1080" + s2ProxyTargetLocation;
		processingtaskResponse = executeCommandRemotely(host, sshUser, convCommand, jobId+"b", processingtaskResponse);
		if(!processingtaskResponse.isComplete())
			throw new Exception("Unable to conversion " + processingtaskResponse.getFailureReason());
		
		return processingtaskResponse;
	}

	private ProcessingtaskResponse executeCommandRemotely(String host, String sshUser, String command, String jobId, ProcessingtaskResponse processingtaskResponse) {
		Session jSchSession = null;
        CommandLineExecutionResponse response = null;
        try {
        	jSchSession = sshSessionHelper.getSession(host, sshUser);
			logger.trace("executing remotely " + command);
			response = remoteCommandLineExecuter.executeCommandRemotelyOnServer(jSchSession, command, jobId + ".out_err");
			if(response.isComplete()) {
		        processingtaskResponse.setIsComplete(true);
		        processingtaskResponse.setStdOutResponse(response.getStdOutResponse());
			}else {
				throw new Exception("Remotecommand executer failed");
			}
        }catch (Exception e) {
        	logger.error("Unable to execute " + command + " remotely" + e.getMessage(), e);
        	processingtaskResponse.setIsComplete(false);
        	processingtaskResponse.setFailureReason(response.getFailureReason());        	
		}finally {
			if (jSchSession != null) 
				sshSessionHelper.disconnectSession(jSchSession);
		}
        return  processingtaskResponse;
	}
}
