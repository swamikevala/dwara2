package org.ishafoundation.videopub.transcoding.ffmpeg.audio;

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
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.Artifact;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.videopub.audio.AudioConfiguration;
import org.ishafoundation.videopub.transcoding.ffmpeg.MediaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Session;

@Component("audio-proxy-low-gen")
@Primary
@Profile({ "!dev & !stage" })
public class Audio_LowResolution_Transcoding_TaskExecutor extends MediaTask implements IProcessingTask{

	private static final Logger logger = LoggerFactory.getLogger(Audio_LowResolution_Transcoding_TaskExecutor.class);
	
	@Autowired
	private AudioConfiguration audioConfiguration;
	
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
		
		FileUtils.forceMkdir(new File(destinationDirPath));
		
		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		
		if(fileName.equals(FilenameUtils.getBaseName(inputArtifactName))) {
			fileName = outputArtifactName;
		}
			
		String proxyTargetLocation = destinationDirPath + File.separator + fileName + ".mp3";

		// TODO : better this...
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setDestinationPathname(proxyTargetLocation);

		Session session = null;
		String nonDwaraGeneratedProxyFilepathname = null;
		boolean proxyAlreadyAvailable = false;
		if(Boolean.TRUE.equals(audioConfiguration.getCheck())) {

			String sequenceShavedOffInputArtifactName = StringUtils.substringAfter(inputArtifactName, "_");
			
			String rootLocation = null;
			if(Boolean.TRUE.equals(audioConfiguration.getRemote())) 
				rootLocation = audioConfiguration.getSshRootLocation();
			else
				rootLocation = audioConfiguration.getLocalRootLocation();
			
			nonDwaraGeneratedProxyFilepathname = rootLocation + File.separator + sequenceShavedOffInputArtifactName + StringUtils.substringAfter(sourceFilePathname, sequenceShavedOffInputArtifactName);
			nonDwaraGeneratedProxyFilepathname = nonDwaraGeneratedProxyFilepathname.replace(FilenameUtils.getName(nonDwaraGeneratedProxyFilepathname), FilenameUtils.getBaseName(nonDwaraGeneratedProxyFilepathname) + ".mp3");
			
			if(Boolean.TRUE.equals(audioConfiguration.getRemote())) { 
				session = sshSessionHelper.getSession(audioConfiguration.getHost(), audioConfiguration.getSshSystemUser());
			
				String command = "test -f " + nonDwaraGeneratedProxyFilepathname + " && echo \"YES\" || echo \"NO\"";
				logger.debug("File exist command - " + command);
				
				// check if file exist
				proxyAlreadyAvailable = fileExist(session, command, fileId + ".out_audio_proxy_check");
			}else {
				if(new File(nonDwaraGeneratedProxyFilepathname).exists())
					proxyAlreadyAvailable = true;	
			}
			
			logger.debug("proxyAlreadyAvailable - " + proxyAlreadyAvailable);
//			if(!proxyAlreadyAvailable) {
//				command = "test -d " + nonDwaraGeneratedProxyFilepathname + " && echo \"YES\" || echo \"NO\""; // check if directory exist
//				logger.debug("Directory exist command - " + command);
//				
//				proxyAlreadyAvailable = abc(session, command, fileId + ".out_audio_proxy_check");
//			}
		}
		
		
		if(proxyAlreadyAvailable) {
			// TODO put it in destination location.. move or copy???
			if(Boolean.TRUE.equals(audioConfiguration.getRemote())) {
				logger.info("Now Copying the Proxy file from " + audioConfiguration.getHost() + "@" + nonDwaraGeneratedProxyFilepathname + " to " +  proxyTargetLocation);
				securedCopier.copyFrom(session, nonDwaraGeneratedProxyFilepathname, proxyTargetLocation);
			}else {
				logger.info("Now Copying the Proxy file from " + nonDwaraGeneratedProxyFilepathname + " to " +  proxyTargetLocation);
				FileUtils.copyFile(new File(nonDwaraGeneratedProxyFilepathname), new File(proxyTargetLocation));
			}
			processingtaskResponse.setIsComplete(true);
		}
		else {
			/*************** PROXY GENERATION ***************/
			long proxyStartTime = System.currentTimeMillis();
			logger.info("Proxy Generation start - targetLocation is - " + proxyTargetLocation);
		
			// Doing this command creation and execution in 2 steps so that the process can be referenced in memory and so if cancel command for a specific medialibrary is issued the specific process(es) can be destroyed/killed referencing this...
			// mapping only for proxy generation commands which are slightly heavy and time consuming than the thumbnail and metadata extraction...
			List<String> proxyGenerationCommandParamsList = getProxyGenCommand(sourceFilePathname, proxyTargetLocation);
			CommandLineExecutionResponse proxyCommandLineExecutionResponse = createProcessAndExecuteCommand(fileId+"~"+taskName , proxyGenerationCommandParamsList);
			long proxyEndTime = System.currentTimeMillis();

			processingtaskResponse.setIsComplete(proxyCommandLineExecutionResponse.isComplete());
			processingtaskResponse.setIsCancelled(proxyCommandLineExecutionResponse.isCancelled());
			processingtaskResponse.setStdOutResponse(proxyCommandLineExecutionResponse.getStdOutResponse());
			processingtaskResponse.setFailureReason(proxyCommandLineExecutionResponse.getFailureReason());
		}

		return processingtaskResponse;
	}

	
	private boolean fileExist(Session session, String command, String commandOutputFilePathName) {
		
		boolean proxyAlreadyAvailable = false;
		CommandLineExecutionResponse cler = null;
		if(Boolean.TRUE.equals(audioConfiguration.getRemote())) {
			try {
				cler = remoteCommandLineExecuter.executeCommandRemotelyOnServer(session, command, commandOutputFilePathName);
				logger.debug("Remote file exist - executed successfully");
			} catch (Exception e) {
				logger.error("Remote file exist - Unable to execute" + e.getMessage());
			}
		}else {
			try {
				cler = commandLineExecuter.executeCommand(command);
				logger.debug("File exist - executed successfully");
			} catch (Exception e) {
				logger.error("File exist - Unable to execute" + e.getMessage());
			}
		}
			
		if(cler.isComplete()) {
			String resp = cler.getStdOutResponse();
			logger.debug(resp);
			if(resp.trim().equals("YES"))
				proxyAlreadyAvailable = true;
			else {
	    		if (session != null) 
	    			sshSessionHelper.disconnectSession(session);
			}
		}

		return proxyAlreadyAvailable;
	}

	// ffmpeg -y -nostdin -i <<sourceFilePathname>> -b:a 64k <<proxyTargetLocation>> 
	// ffmpeg -y -nostdin -i file.wav -b:a 64k file.mp3
	// ffmpeg -y -nostdin -i "Tel Dub.wav" -b:a 64000 "output/Tel Dub.mp3" 
	private List<String> getProxyGenCommand(String sourceFilePathname, String proxyTargetLocation) {
		List<String> proxyGenerationCommandParamsList = new ArrayList<String>();
		
		proxyGenerationCommandParamsList.add("ffmpeg");
		proxyGenerationCommandParamsList.add("-y");
		proxyGenerationCommandParamsList.add("-nostdin");
		proxyGenerationCommandParamsList.add("-i");
		proxyGenerationCommandParamsList.add(sourceFilePathname);
		proxyGenerationCommandParamsList.add("-b:a");
		proxyGenerationCommandParamsList.add("64k");
		proxyGenerationCommandParamsList.add(proxyTargetLocation);
	
		return proxyGenerationCommandParamsList;
	}
}
