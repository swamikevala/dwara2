package org.ishafoundation.videopub.transcoding.ffmpeg.video;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.videopub.transcoding.ffmpeg.MediaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("video-proxy-low-gen")
@Profile({ "dev | stage" })
public class MockVideo_LowResolution_Transcoding_TaskExecutor extends MediaTask implements IProcessingTask{
    private static final Logger logger = LoggerFactory.getLogger(MockVideo_LowResolution_Transcoding_TaskExecutor.class);


	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		LogicalFile logicalFile = processContext.getLogicalFile();
		org.ishafoundation.dwaraapi.process.request.File file = processContext.getFile();
		String destinationDirPath = processContext.getOutputDestinationDirPath();
		
		String sourceFilePathname = logicalFile.getAbsolutePath();
		String clipName = FilenameUtils.getName(sourceFilePathname);
		
		String identifierSuffix = "";//mediaFileId+"";//  +  "-" + VideoProcessingSteps.PROXY_GENERATION.toString();
		String containerName = identifierSuffix;	
		
	
		long startms = System.currentTimeMillis();
		
		String m01FileLocPath = sourceFilePathname.replace("." + FilenameUtils.getExtension(sourceFilePathname), "M01.XML");
		String baseName = FilenameUtils.getBaseName(sourceFilePathname);
		
		String thumbnailTargetLocation = destinationDirPath + File.separator + baseName + ".jpg";
		String thumbnailStdErrFileLoc = thumbnailTargetLocation.replace(".jpg", ".jpg_ffmpeg_log");
	
		/*************** THUMBNAIL GENERATION ***************/
		long thumbnailStartTime = System.currentTimeMillis();
		logger.info("Thumbnail Generation start for " + containerName + " - targetLocation is - " + thumbnailTargetLocation);
		
		createFileInIngestServer(thumbnailStdErrFileLoc);
		
		createFileInIngestServer(thumbnailTargetLocation);
		
		/*
		 * targetFileLocPathsuffix will be something like /proxies/1825_SOI-Concert_Nandi_IYC_20-Jan-11_Cam1/123-345-PROXYGENERATION/KHS10267_01.MP4
		 * we had to add the containerName(mlID-cardId-processName say e.g., something like 123-345-PROXYGENERATION) as XAVC file names across card folders are same...
		 * 
		 */
		String proxyTargetLocation = destinationDirPath + File.separator + baseName + ".mp4";		
		String proxyStdErrFileLoc = proxyTargetLocation.replace(".mp4", ".mp4_ffmpeg_log");
		
		String highResMetaTargetLocation = proxyTargetLocation.replace(".mp4", ".mp4_ffprobe_out");
		String metaStdErrFileLoc = proxyTargetLocation.replace(".mp4", ".mp4_ffprobe_log");
		
		createFileInIngestServer(proxyStdErrFileLoc);
		createFileInIngestServer(metaStdErrFileLoc);
		
		createFileInIngestServer(highResMetaTargetLocation);
		
		/*************** PROXY GENERATION ***************/
		createFileInIngestServer(proxyTargetLocation);
	
	
		// TODO : better this...
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setDestinationPathname(proxyTargetLocation);
		processingtaskResponse.setIsComplete(true);
		processingtaskResponse.setIsCancelled(false);
		processingtaskResponse.setStdOutResponse("some dummy stdout response");
		processingtaskResponse.setFailureReason(null);
		
		return processingtaskResponse;
	}
}
