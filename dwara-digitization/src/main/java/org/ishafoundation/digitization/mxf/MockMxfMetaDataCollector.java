package org.ishafoundation.digitization.mxf;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.PfrConstants;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("video-digi-2020-header-extract")
@Profile({ "dev | stage" })
public class MockMxfMetaDataCollector implements IProcessingTask {
    
    private static final Logger logger = LoggerFactory.getLogger(MockMxfMetaDataCollector.class);
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		LogicalFile logicalFile = processContext.getLogicalFile();
		String destinationDirPath = processContext.getOutputDestinationDirPath();

		String sourceFilePathname = logicalFile.getAbsolutePath();
		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		
		FileUtils.forceMkdir(new File(destinationDirPath));
		
		String hdrTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.HDR_EXTN;
		String ftrTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.FTR_EXTN;
		
		FileUtils.writeStringToFile(new File(hdrTargetLocation), "Some hdr content");
		FileUtils.writeStringToFile(new File(ftrTargetLocation), "Some ftr content");
		
		// TODO : better this...
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setDestinationPathname(hdrTargetLocation);
		processingtaskResponse.setIsComplete(true);
		processingtaskResponse.setIsCancelled(false);
		processingtaskResponse.setStdOutResponse("");
		processingtaskResponse.setFailureReason("");

		return processingtaskResponse;
	}

}
