package org.ishafoundation.videopub.transcoding.ffmpeg.video;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.PfrConstants;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.videopub.transcoding.ffmpeg.MediaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("video-preservation-gen")
@Profile({ "dev | stage" })
public class MockVideo_Prasad_Transcoding_TaskExecutor extends MediaTask implements IProcessingTask{
    private static final Logger logger = LoggerFactory.getLogger(MockVideo_Prasad_Transcoding_TaskExecutor.class);


	@Override
	public ProcessingtaskResponse execute(String taskName, String artifactclass, String inputArtifactName, String outputArtifactName,
			org.ishafoundation.dwaraapi.db.model.transactional.domain.File file, Domain domain, LogicalFile logicalFile,
			String category, String destinationDirPath) throws Exception {
		

		
		
		String sourceFilePathname = logicalFile.getAbsolutePath();
		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		
		FileUtils.forceMkdir(new File(destinationDirPath));
		String compressedFileTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.MKV_EXTN;// TODO How do we know if it should be mkv or mxf or what not???	
		String headerFileTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.PFR_HDR_EXTN;// TODO How do we know if it should be mkv or mxf or what not???	
		String cuesFileTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.INDEX_EXTN;// TODO How do we know if it should be mkv or mxf or what not???	

		
		FileUtils.writeStringToFile(new File(compressedFileTargetLocation), "Some compressed file content");
		FileUtils.writeStringToFile(new File(headerFileTargetLocation), "Some hdr content");
		FileUtils.writeStringToFile(new File(cuesFileTargetLocation), "Some cues file content");
	
		// TODO : better this...
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setDestinationPathname(compressedFileTargetLocation);
		processingtaskResponse.setIsComplete(true);
		processingtaskResponse.setIsCancelled(false);
		processingtaskResponse.setStdOutResponse("some dummy stdout response");
		processingtaskResponse.setFailureReason(null);
		
		return processingtaskResponse;
	}
}
