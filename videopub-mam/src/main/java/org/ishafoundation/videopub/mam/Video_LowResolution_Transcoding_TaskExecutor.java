package org.ishafoundation.videopub.mam;

import org.springframework.stereotype.Component;

@Component("previewproxy-video-transcoding")
public class Video_LowResolution_Transcoding_TaskExecutor implements IProcessingTask {

	@Override
	public String execute() {
		System.out.println("im here");
		return null;
	}

}
