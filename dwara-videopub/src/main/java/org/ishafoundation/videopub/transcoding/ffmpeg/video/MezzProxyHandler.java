package org.ishafoundation.videopub.transcoding.ffmpeg.video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.file.RsyncCopier;
import org.ishafoundation.dwaraapi.process.request.Artifact;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.videopub.transcoding.ffmpeg.MediaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 * Either generates or copies files from specific subfolders
 *
	artifactname_suffix	proxy_location
		
	_FX3	M4ROOT/SUB/*.MP4
	_A7S3	M4ROOT/SUB/*.MP4
	_FS7	ffmpeg
	_FX9 	on separate card
	_FX9-Proxy	private/PXROOT/Clip/*.MP4
	_Drone	ffmpeg
	_Insta360	ffmpeg
	_GoPro	ffmpeg
 */
@Component("mezz-proxy-handler")
public class MezzProxyHandler extends MediaTask implements IProcessingTask{
	
	private static final Logger logger = LoggerFactory.getLogger(MezzProxyHandler.class);

	@Autowired
	private RsyncCopier rsyncCopier;
	
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
	
		String taskName = processContext.getJob().getProcessingtaskId();
		LogicalFile logicalFile = processContext.getLogicalFile();
		Integer fileId = processContext.getFile().getId();
		
		Artifact inputArtifact = processContext.getJob().getInputArtifact();
		String inputArtifactName = inputArtifact.getName();
		
		String destinationDirPath = processContext.getOutputDestinationDirPath(); // This includes the host ip
		logger.trace("destinationDirPath " + destinationDirPath);
		
		if(inputArtifactName.toUpperCase().contains("_FX3") || inputArtifactName.toUpperCase().contains("_A7S3")) { // || inputArtifactName.contains("_FX9-Proxy"))
			if(logicalFile.getParent().contains("M4ROOT" + File.separator + "SUB"))
				return rsyncCopier.execute(processContext);
			else {
				logger.trace("Skipping file " + logicalFile.getAbsolutePath());

				// return a dummy response to cheat the system...
				ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
				processingtaskResponse.setIsComplete(true);

				return processingtaskResponse;
			}
		}
		else {
			String sourceFilePathname = logicalFile.getAbsolutePath();
			
			FileUtils.forceMkdir(new File(destinationDirPath));
			
			String fileName = FilenameUtils.getBaseName(sourceFilePathname);
			
			String proxyTargetLocation = destinationDirPath + File.separator + fileName + ".mp4";
			
			List<String> proxyGenerationCommandParamsList = getMezzProxyGenerationCommand(sourceFilePathname, proxyTargetLocation);
			CommandLineExecutionResponse proxyCommandLineExecutionResponse = createProcessAndExecuteCommand(fileId+"~"+taskName , proxyGenerationCommandParamsList);
			long proxyEndTime = System.currentTimeMillis();
		
			// TODO : better this...
			ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
			processingtaskResponse.setDestinationPathname(proxyTargetLocation);
			processingtaskResponse.setIsComplete(proxyCommandLineExecutionResponse.isComplete());
			processingtaskResponse.setIsCancelled(proxyCommandLineExecutionResponse.isCancelled());
			processingtaskResponse.setStdOutResponse(proxyCommandLineExecutionResponse.getStdOutResponse());
			processingtaskResponse.setFailureReason(proxyCommandLineExecutionResponse.getFailureReason());

			return processingtaskResponse;
		}
	}
	
	
	// ffmpeg -i 7A7S30073.MP4 -vf scale=1920:-1 -c:v hevc_videotoolbox -b:v 16m -c:a aac -b:a 128k -tag:v hvc1 test7.MP4
	
	private List<String> getMezzProxyGenerationCommand(String sourceFilePathname, String thumbnailTargetLocation) {
		List<String> proxyGenerationCommandParamsList = new ArrayList<String>();
		proxyGenerationCommandParamsList.add("/usr/local/bin/ffmpeg");
		proxyGenerationCommandParamsList.add("-y");
		proxyGenerationCommandParamsList.add("-nostdin");
		proxyGenerationCommandParamsList.add("-i");
		proxyGenerationCommandParamsList.add(sourceFilePathname);
		proxyGenerationCommandParamsList.add("-vf");
		proxyGenerationCommandParamsList.add("scale=1920:-1");
		proxyGenerationCommandParamsList.add("-c:v");
		proxyGenerationCommandParamsList.add("hevc_videotoolbox");
		proxyGenerationCommandParamsList.add("-b:v");
		proxyGenerationCommandParamsList.add("16m");
		proxyGenerationCommandParamsList.add("-c:a");
		proxyGenerationCommandParamsList.add("aac");
		proxyGenerationCommandParamsList.add("-b:a");
		proxyGenerationCommandParamsList.add("128k");
		proxyGenerationCommandParamsList.add("-tag:v");
		proxyGenerationCommandParamsList.add("hvc1");
		proxyGenerationCommandParamsList.add(thumbnailTargetLocation);
		
		return proxyGenerationCommandParamsList;
	}
}

