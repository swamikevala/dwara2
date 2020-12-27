package org.ishafoundation.digitization.preservation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.PfrConstants;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.videopub.transcoding.ffmpeg.MediaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("video-mkv-pfr-metadata-extract")
@Primary
@Profile({ "!dev & !stage" })
public class Video_Digitization_MetadataExtractor_TaskExecutor extends MediaTask implements IProcessingTask{
    private static final Logger logger = LoggerFactory.getLogger(Video_Digitization_MetadataExtractor_TaskExecutor.class);
    
    private Pattern clusterPositionRegexPattern = Pattern.compile("Cluster at ([0-9]*)");
    private Pattern videoTrackIdentifierRegexPattern = Pattern.compile("Track ID ([0-9]*): video");
    
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
	
		String taskName = processContext.getJob().getProcessingtaskId();
		LogicalFile logicalFile = processContext.getLogicalFile();
		org.ishafoundation.dwaraapi.process.request.File file = processContext.getFile();
		String destinationDirPath = processContext.getOutputDestinationDirPath();

		if(logger.isTraceEnabled()) {
			logger.trace("taskName " + taskName);
//			logger.trace("inputArtifactName " + inputArtifactName); // V22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
//			logger.trace("outputArtifactName " + outputArtifactName); // VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
			logger.trace("fileId " + file.getId());
//			logger.trace("domain " + domain.name()); 
			logger.trace("logicalFile " + logicalFile.getAbsolutePath()); // /data/ingested/V22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12/DCIM/100EOS5D/MVI_5594.MOV
//			logger.trace("category " + category); // public
			logger.trace("destinationDirPath " + destinationDirPath); // /data/transcoded/public/VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12/DCIM/100EOS5D
		}
		String sourceFilePathname = logicalFile.getAbsolutePath();
		long startms = System.currentTimeMillis();
	
		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		
		FileUtils.forceMkdir(new File(destinationDirPath));
		
		String headerFileTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.HDR_EXTN;// TODO How do we know if it should be mkv or mxf or what not???	
		String cuesFileTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.INDEX_EXTN;// TODO How do we know if it should be mkv or mxf or what not???	
		
	
		/*************** HEADER EXTRACTION ***************/
		List<String> clusterPositionCommandParamsList = getClusterPositionCommand(sourceFilePathname);
		CommandLineExecutionResponse clusterPositionCommandLineExecutionResponse = commandLineExecuter.executeCommand(clusterPositionCommandParamsList);

		String mkvInfo = clusterPositionCommandLineExecutionResponse.getStdOutResponse();
		Matcher clusterPositionRegexMatcher = clusterPositionRegexPattern.matcher(mkvInfo);
		int clusterPosition = 0;
		if(clusterPositionRegexMatcher.find()) {
			clusterPosition = Integer.parseInt(clusterPositionRegexMatcher.group(1).trim());
		}
		
		//dd if=sample.mkv bs=2455 count=1 of=sample.hdr
		CommandLineExecutionResponse ddExtractCommandLineExecutionResponse = commandLineExecuter.executeCommand("dd if=" + sourceFilePathname + " bs=" + clusterPosition + " count=1 of=" + headerFileTargetLocation);
		if(ddExtractCommandLineExecutionResponse.isComplete())
			logger.info("Header saved in " + headerFileTargetLocation);
		
		/*************** CUES EXTRACTION ***************/
		// identify the video track no.
		CommandLineExecutionResponse videoTrackIdentifierCommandLineExecutionResponse = commandLineExecuter.executeCommand("mkvmerge --identify " + sourceFilePathname);
		String trackIndentifierResponse = videoTrackIdentifierCommandLineExecutionResponse.getStdOutResponse();
		Matcher videoTrackIdentifierRegexMatcher = videoTrackIdentifierRegexPattern.matcher(trackIndentifierResponse);
		int videoTrackNumber = 0;
		if(videoTrackIdentifierRegexMatcher.find()) {
			videoTrackNumber = Integer.parseInt(videoTrackIdentifierRegexMatcher.group(1).trim());
		}else {
			throw new Exception("Unable to find video track no...");
		}

		// mkvextract sample.mkv cues 4:sample-cues2.txt
		CommandLineExecutionResponse cuesExtractionCommandLineExecutionResponse = commandLineExecuter.executeCommand("mkvextract " + sourceFilePathname + " cues " + videoTrackNumber + ":" + cuesFileTargetLocation+".tmp");
		List<String> cuesEntriesList = FileUtils.readLines(new File(cuesFileTargetLocation+".tmp"));
		CuesFileParser cfp = new CuesFileParser();
		FileUtils.write(new File(cuesFileTargetLocation), cfp.parseCuesResponse(cuesEntriesList));
		new File(cuesFileTargetLocation+".tmp").delete();
		
		// TODO : better this...
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setDestinationPathname(cuesFileTargetLocation);
		processingtaskResponse.setIsComplete(cuesExtractionCommandLineExecutionResponse.isComplete());
		processingtaskResponse.setIsCancelled(cuesExtractionCommandLineExecutionResponse.isCancelled());
		processingtaskResponse.setStdOutResponse(cuesExtractionCommandLineExecutionResponse.getStdOutResponse());
		processingtaskResponse.setFailureReason(cuesExtractionCommandLineExecutionResponse.getFailureReason());

		return processingtaskResponse;
	}
	
	// mkvinfo -P sample.mkv
	private List<String> getClusterPositionCommand(String compressedFileLocation) {
		List<String> clusterPositionCommandParamsList = new ArrayList<String>();
		clusterPositionCommandParamsList.add("mkvinfo");
		clusterPositionCommandParamsList.add("-P");
		clusterPositionCommandParamsList.add(compressedFileLocation);
		
		return clusterPositionCommandParamsList;
	}
}
