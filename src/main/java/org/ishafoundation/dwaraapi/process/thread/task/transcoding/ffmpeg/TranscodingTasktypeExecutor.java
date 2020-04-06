package org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg;

import org.ishafoundation.dwaraapi.model.LogicalFile;
import org.ishafoundation.dwaraapi.model.TaskResponse;
import org.ishafoundation.dwaraapi.model.TasktypeResponse;
import org.ishafoundation.dwaraapi.process.factory.TasktypeFactory;
import org.ishafoundation.dwaraapi.process.factory.TranscoderFactory;
import org.ishafoundation.dwaraapi.process.thread.task.ITasktypeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class TranscodingTasktypeExecutor implements ITasktypeExecutor {
    static {
    	TasktypeFactory.register("TRANSCODING", TranscodingTasktypeExecutor.class);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(TranscodingTasktypeExecutor.class);
		
	@Autowired
	private ApplicationContext applicationContext;	


	@Override
	public TasktypeResponse execute(String taskName, String libraryName, int fileId, LogicalFile logicalFile, String category,
			String destinationDirPath) throws Exception {
		ITranscoder transcoder = TranscoderFactory.getInstance(applicationContext, taskName);
		String sourceFilePathname = logicalFile.getAbsolutePath();
		TaskResponse taskResponse = transcoder.transcode(taskName, fileId, sourceFilePathname, destinationDirPath);
		
		TasktypeResponse tasktypeResponse = new TasktypeResponse();
		tasktypeResponse.setDestinationPathname(taskResponse.getDestinationPathname());
		tasktypeResponse.setIsComplete(taskResponse.isComplete());
		tasktypeResponse.setIsCancelled(taskResponse.isCancelled());
		tasktypeResponse.setStdOutResponse(taskResponse.getStdOutResponse());
		tasktypeResponse.setFailureReason(taskResponse.getFailureReason());

		return tasktypeResponse;
	}
}
