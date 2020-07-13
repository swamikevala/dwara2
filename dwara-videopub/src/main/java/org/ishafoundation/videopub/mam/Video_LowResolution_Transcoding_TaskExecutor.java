package org.ishafoundation.videopub.mam;

import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("previewproxy-video-transcoding")
public class Video_LowResolution_Transcoding_TaskExecutor implements IProcessingTask {

	private static final Logger logger = LoggerFactory.getLogger(Video_LowResolution_Transcoding_TaskExecutor.class);
	
	@Override
	public String execute() {
		logger.trace("im here");
		return null;
	}

}
