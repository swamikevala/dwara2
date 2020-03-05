package org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg;

import org.ishafoundation.dwaraapi.model.ProxyGenCommandLineExecutionResponse;

public interface ITranscoder {

	/*
	 * @args[0] taskName - name of the task(refer task table)
	 * @args[1] fileId - The file's id
	 * @args[2] sourceFilePathname - the source file to be transcoded
	 * @args[3] destinationDirPath - the destination directory where the transcoded output need to be generated. There are some transcoding implementations that will generate the sidecar files too along with the transcoded proxy in this directory
	 */
	public ProxyGenCommandLineExecutionResponse transcode(String taskName, int fileId, String sourceFilePathname, String destinationDirPath) throws Exception;
	
}
