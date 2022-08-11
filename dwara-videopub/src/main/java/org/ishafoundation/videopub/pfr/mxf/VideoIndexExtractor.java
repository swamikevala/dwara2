package org.ishafoundation.videopub.pfr.mxf;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.videopub.pfr.PFRFile;
import org.ishafoundation.videopub.pfr.PFRFileMXF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("video-index-extract")
@Primary
@Profile({ "!dev & !stage" })
public class VideoIndexExtractor implements IProcessingTask {
    
    private static final Logger logger = LoggerFactory.getLogger(VideoIndexExtractor.class);
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		LogicalFile logicalFile = processContext.getLogicalFile();
		String destinationDirPath = processContext.getOutputDestinationDirPath();
		
		String sourceFilePathname = logicalFile.getAbsolutePath();
		
		FileUtils.forceMkdir(new File(destinationDirPath));
		
		File mxfFile = new File(sourceFilePathname);
		PFRFile pfrFile = new PFRFileMXF(mxfFile);
		
		// extract metadata files
		pfrFile.extractHeader(destinationDirPath);
		pfrFile.extractIndex(destinationDirPath);
		pfrFile.extractFooter(destinationDirPath);

		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setDestinationPathname(destinationDirPath);
		processingtaskResponse.setIsComplete(true);
		processingtaskResponse.setIsCancelled(false);
		processingtaskResponse.setStdOutResponse("");
		processingtaskResponse.setFailureReason("");

		return processingtaskResponse;
	}
	
}
