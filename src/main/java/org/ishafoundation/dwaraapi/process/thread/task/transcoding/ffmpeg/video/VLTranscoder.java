package org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.video;

import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.model.ProxyGenCommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.process.factory.TranscoderFactory;
import org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.ITranscoder;
import org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.MediaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VLTranscoder extends MediaTask implements ITranscoder{
    static {
    	TranscoderFactory.register("Video Transcoding Low Resolution", VLTranscoder.class);
    }

    private static final Logger logger = LoggerFactory.getLogger(VLTranscoder.class);
    
	@Override
	public ProxyGenCommandLineExecutionResponse transcode(String taskName, int fileId, String sourceFilePathname,
			String destinationPath) throws Exception {
		String clipExtn = FilenameUtils.getExtension(sourceFilePathname);

		String proxyTargetLocation = destinationPath.replace(clipExtn, ".mp4");		
		createFileInIngestServer(proxyTargetLocation);
		logger.trace("VTLR " + proxyTargetLocation);
		ProxyGenCommandLineExecutionResponse proxyGenCommandLineExecutionResponse = new ProxyGenCommandLineExecutionResponse();
		proxyGenCommandLineExecutionResponse.setDestinationPathname(proxyTargetLocation);
		proxyGenCommandLineExecutionResponse.setIsComplete(true);
		
		return proxyGenCommandLineExecutionResponse;
	}
}
