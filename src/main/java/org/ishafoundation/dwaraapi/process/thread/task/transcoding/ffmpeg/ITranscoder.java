package org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg;

import org.ishafoundation.dwaraapi.model.ProxyGenCommandLineExecutionResponse;

public interface ITranscoder {

	/*
	 * @args 
	 */
	public ProxyGenCommandLineExecutionResponse transcode(String taskName, int fileId, String sourceFilePathname, String destinationPath) throws Exception;
	
}
