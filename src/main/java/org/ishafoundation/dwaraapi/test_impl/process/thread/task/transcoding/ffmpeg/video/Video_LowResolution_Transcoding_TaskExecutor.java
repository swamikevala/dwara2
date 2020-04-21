package org.ishafoundation.dwaraapi.test_impl.process.thread.task.transcoding.ffmpeg.video;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.model.LogicalFile;
import org.ishafoundation.dwaraapi.model.TaskResponse;
import org.ishafoundation.dwaraapi.process.factory.TaskFactory;
import org.ishafoundation.dwaraapi.process.thread.task.ITaskExecutor;
import org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.MediaTask;
import org.ishafoundation.dwaraapi.process.transcoding.ffmpeg.utils.M01XmlFileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "dev | test" })
public class Video_LowResolution_Transcoding_TaskExecutor extends MediaTask implements ITaskExecutor{
    static {
    	TaskFactory.register("video_low_resolution_transcoding", Video_LowResolution_Transcoding_TaskExecutor.class);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(Video_LowResolution_Transcoding_TaskExecutor.class);
    
	@Autowired
	private M01XmlFileHandler m01xfh;	

	@Override
	public TaskResponse execute(String taskName, String libraryName, int fileId, LogicalFile logicalFile, String category,
			String destinationDirPath) throws Exception {
		String sourceFilePathname = logicalFile.getAbsolutePath();
		String clipName = FilenameUtils.getName(sourceFilePathname);
		
		String identifierSuffix = "";//mediaFileId+"";//  +  "-" + VideoProcessingSteps.PROXY_GENERATION.toString();
		String containerName = identifierSuffix;	
		
	
		long startms = System.currentTimeMillis();
		
		String m01FileLocPath = sourceFilePathname.replace("." + FilenameUtils.getExtension(sourceFilePathname), "M01.XML");
	
		String thumbnailTargetLocation = destinationDirPath + File.separator + FilenameUtils.getBaseName(sourceFilePathname) + ".jpg";
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
		String proxyTargetLocation = destinationDirPath + File.separator + FilenameUtils.getBaseName(sourceFilePathname) + ".mp4";		
		String proxyStdErrFileLoc = proxyTargetLocation.replace(".mp4", ".mp4_ffmpeg_log");
		
		String highResMetaTargetLocation = proxyTargetLocation.replace(".mp4", ".mp4_ffprobe_out");
		String metaStdErrFileLoc = proxyTargetLocation.replace(".mp4", ".mp4_ffprobe_log");
		
		createFileInIngestServer(proxyStdErrFileLoc);
		createFileInIngestServer(metaStdErrFileLoc);
		
		createFileInIngestServer(highResMetaTargetLocation);
		
		/*************** PROXY GENERATION ***************/
		createFileInIngestServer(proxyTargetLocation);
		
		// TODO : better this...
		TaskResponse tasktypeResponse = new TaskResponse();
		tasktypeResponse.setDestinationPathname(proxyTargetLocation);
		tasktypeResponse.setIsComplete(true);
		tasktypeResponse.setIsCancelled(false);
		tasktypeResponse.setStdOutResponse("some dummy stdout response");
		tasktypeResponse.setFailureReason(null);

		return tasktypeResponse;
	}
	
}
