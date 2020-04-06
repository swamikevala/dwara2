package org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.video;

import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.model.TaskResponse;
import org.ishafoundation.dwaraapi.process.factory.TranscoderFactory;
import org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.ITranscoder;
import org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.MediaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VLTranscoder extends MediaTask implements ITranscoder{


    private static final Logger logger = LoggerFactory.getLogger(VLTranscoder.class);
    
	@Override
	public TaskResponse transcode(String taskName, int fileId, String sourceFilePathname,
			String destinationPath) throws Exception {
		logger.trace("destinationPath " + destinationPath);
		String clipExtn = FilenameUtils.getExtension(sourceFilePathname);

		String proxyTargetLocation = destinationPath.replace(clipExtn, ".mp4");		
		createFileInIngestServer(proxyTargetLocation);
		logger.trace("VTLR " + proxyTargetLocation);
		TaskResponse proxyGenCommandLineExecutionResponse = new TaskResponse();
		proxyGenCommandLineExecutionResponse.setDestinationPathname(proxyTargetLocation);
		proxyGenCommandLineExecutionResponse.setIsComplete(true);
		
		return proxyGenCommandLineExecutionResponse;
	}
}
