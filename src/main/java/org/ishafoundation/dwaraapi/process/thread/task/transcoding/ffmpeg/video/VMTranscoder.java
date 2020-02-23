package org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.video;

import org.ishafoundation.dwaraapi.model.ProxyGenCommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.process.factory.TranscoderFactory;
import org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.MediaTask;
import org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.ITranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VMTranscoder extends MediaTask implements ITranscoder{
    static {
    	TranscoderFactory.register("Video Transcoding Medium Resolution", VMTranscoder.class);
    }

    private static final Logger logger = LoggerFactory.getLogger(VMTranscoder.class);

	@Override
	public ProxyGenCommandLineExecutionResponse transcode(String taskName, int fileId, String sourceFilePathname,
			String destinationPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


    
//    public ProxyGenCommandLineExecutionResponse transcode(Job job, int fileId, String sourceLibraryName, String sourceLocation, String destinationRoot, String outputLibraryNamePrefix) throws Exception {
//		String clipName = FilenameUtils.getName(sourceLocation);
//		
//		String identifierSuffix = "";//mediaFileId+"";//  +  "-" + VideoProcessingSteps.PROXY_GENERATION.toString();
//		String containerName = identifierSuffix;	
//		
//		String folderLocSuffix = outputLibraryNamePrefix + sourceLibraryName + File.separator + containerName + File.separator +  FilenameUtils.getBaseName(clipName);
//
//		String proxyTargetLocation = destinationRoot + File.separator + folderLocSuffix + ".mp4";		
//		createFileInIngestServer(proxyTargetLocation);
//		logger.trace("VTMR " + proxyTargetLocation);
//		ProxyGenCommandLineExecutionResponse proxyGenCommandLineExecutionResponse = new ProxyGenCommandLineExecutionResponse();
//		proxyGenCommandLineExecutionResponse.setDestinationPathname(proxyTargetLocation);
//		proxyGenCommandLineExecutionResponse.setIsComplete(true);
//		
//		return proxyGenCommandLineExecutionResponse;
//	}


}
