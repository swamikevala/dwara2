package org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg;

import org.ishafoundation.dwaraapi.model.LogicalFile;
import org.ishafoundation.dwaraapi.model.ProxyGenCommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.process.factory.ProcessFactory;
import org.ishafoundation.dwaraapi.process.factory.TranscoderFactory;
import org.ishafoundation.dwaraapi.process.thread.task.IProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class TranscodingProcessor implements IProcessor {
    static {
    	ProcessFactory.register("TRANSCODING", TranscodingProcessor.class);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(TranscodingProcessor.class);
		
	@Autowired
	private ApplicationContext applicationContext;	


	@Override
	public ProxyGenCommandLineExecutionResponse process(String taskName, int fileId, LogicalFile logicalFile,
			String destinationFilePath) throws Exception {
		ITranscoder transcoder = TranscoderFactory.getInstance(applicationContext, taskName);
		String sourceFilePathname = logicalFile.getAbsolutePath();
		ProxyGenCommandLineExecutionResponse proxyGenCommandLineExecutionResponse = transcoder.transcode(taskName, fileId, sourceFilePathname, destinationFilePath);
		return proxyGenCommandLineExecutionResponse;
	}
}
