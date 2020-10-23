package org.ishafoundation.videopub.transcoding.ffmpeg.video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.PfrConstants;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.videopub.transcoding.ffmpeg.MediaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("video-preservation-gen")
@Primary
@Profile({ "!dev & !stage" })
public class Video_Prasad_Transcoding_TaskExecutor extends MediaTask implements IProcessingTask{
    private static final Logger logger = LoggerFactory.getLogger(Video_Prasad_Transcoding_TaskExecutor.class);
    
    private Pattern clusterPositionRegexPattern = Pattern.compile("Cluster at ([0-9]*)");
    private Pattern videoTrackIdentifierRegexPattern = Pattern.compile("Track ID ([0-9]*): video");
    
	@Override
	public ProcessingtaskResponse execute(String taskName, String artifactclass, String inputArtifactName, String outputArtifactName,
			org.ishafoundation.dwaraapi.db.model.transactional.domain.File file, Domain domain, LogicalFile logicalFile,
			String category, String destinationDirPath) throws Exception {
		
		if(logger.isTraceEnabled()) {
			logger.trace("taskName " + taskName);
			logger.trace("inputArtifactName " + inputArtifactName); // V22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
			logger.trace("outputArtifactName " + outputArtifactName); // VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
			logger.trace("fileId " + file.getId());
			logger.trace("domain " + domain.name()); 
			logger.trace("logicalFile " + logicalFile.getAbsolutePath()); // /data/ingested/V22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12/DCIM/100EOS5D/MVI_5594.MOV
			logger.trace("category " + category); // public
			logger.trace("destinationDirPath " + destinationDirPath); // /data/transcoded/public/VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12/DCIM/100EOS5D
		}
		String sourceFilePathname = logicalFile.getAbsolutePath();
		long startms = System.currentTimeMillis();
	
		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		
		FileUtils.forceMkdir(new File(destinationDirPath));
		
		String compressedFileTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.MKV_EXTN;// TODO How do we know if it should be mkv or mxf or what not???	
		String headerFileTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.HDR_EXTN;// TODO How do we know if it should be mkv or mxf or what not???	
		String cuesFileTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.INDEX_EXTN;// TODO How do we know if it should be mkv or mxf or what not???	
		
		/*************** COMPRESSION ***************/
		long proxyStartTime = System.currentTimeMillis();
		logger.info("Compression starts for " + sourceFilePathname + " - targetLocation is - " + compressedFileTargetLocation);
	
		// Doing this command creation and execution in 2 steps so that the process can be referenced in memory and so if cancel command for a specific medialibrary is issued the specific process(es) can be destroyed/killed referencing this...
		// mapping only for proxy generation commands which are slightly heavy and time consuming than the thumbnail and metadata extraction...
		List<String> compressionCommandParamsList = getCompressionCommand(sourceFilePathname, compressedFileTargetLocation);
		CommandLineExecutionResponse compressionCommandLineExecutionResponse = createProcessAndExecuteCommand(file.getId()+"~"+taskName , compressionCommandParamsList);
		long proxyEndTime = System.currentTimeMillis();
	
		/*************** HEADER EXTRACTION ***************/
		List<String> clusterPositionCommandParamsList = getClusterPositionCommand(compressedFileTargetLocation);
		CommandLineExecutionResponse clusterPositionCommandLineExecutionResponse = commandLineExecuter.executeCommand(clusterPositionCommandParamsList);

		String mkvInfo = clusterPositionCommandLineExecutionResponse.getStdOutResponse();
		Matcher clusterPositionRegexMatcher = clusterPositionRegexPattern.matcher(mkvInfo);
		int clusterPosition = 0;
		if(clusterPositionRegexMatcher.find()) {
			clusterPosition = Integer.parseInt(clusterPositionRegexMatcher.group(1).trim());
		}
		
		//dd if=sample.mkv bs=2455 count=1 of=sample.hdr
		CommandLineExecutionResponse ddExtractCommandLineExecutionResponse = commandLineExecuter.executeCommand("dd if=" + compressedFileTargetLocation + " bs=" + clusterPosition + " count=1 of=" + headerFileTargetLocation);
		if(ddExtractCommandLineExecutionResponse.isComplete())
			logger.info("Header saved in " + headerFileTargetLocation);
		
		/*************** CUES EXTRACTION ***************/
		// identify the video track no.
		CommandLineExecutionResponse videoTrackIdentifierCommandLineExecutionResponse = commandLineExecuter.executeCommand("mkvmerge --identify " + compressedFileTargetLocation);
		String trackIndentifierResponse = videoTrackIdentifierCommandLineExecutionResponse.getStdOutResponse();
		Matcher videoTrackIdentifierRegexMatcher = videoTrackIdentifierRegexPattern.matcher(trackIndentifierResponse);
		int videoTrackNumber = 0;
		if(videoTrackIdentifierRegexMatcher.find()) {
			videoTrackNumber = Integer.parseInt(videoTrackIdentifierRegexMatcher.group(1).trim());
		}else {
			throw new Exception("Unable to find video track no...");
		}

		// mkvextract sample.mkv cues 4:sample-cues2.txt
		CommandLineExecutionResponse cuesExtractionCommandLineExecutionResponse = commandLineExecuter.executeCommand("mkvextract " + compressedFileTargetLocation + " cues " + videoTrackNumber + ":" + cuesFileTargetLocation+".tmp");
		String strResponse = FileUtils.readFileToString(new File(cuesFileTargetLocation+".tmp"));
		CuesFileParser cfp = new CuesFileParser();
		FileUtils.write(new File(cuesFileTargetLocation), cfp.parseCuesResponse(strResponse));
		new File(cuesFileTargetLocation+".tmp").delete();
		
		// TODO : better this...
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setDestinationPathname(compressedFileTargetLocation);
		processingtaskResponse.setIsComplete(compressionCommandLineExecutionResponse.isComplete());
		processingtaskResponse.setIsCancelled(compressionCommandLineExecutionResponse.isCancelled());
		processingtaskResponse.setStdOutResponse(compressionCommandLineExecutionResponse.getStdOutResponse());
		processingtaskResponse.setFailureReason(compressionCommandLineExecutionResponse.getFailureReason());

		return processingtaskResponse;
	}
	
	
	//ffmpeg -i N0023_CHENNAI_PROGRAM_INTRO_2.mxf -acodec copy -vcodec ffv1 -level 3 -coder 1 -context 1 -g 1 -slicecrc 1 -map 0:a -map 0:v N0023.mkv
	private List<String> getCompressionCommand(String sourceFilePathname, String compressedFileTargetLocation) {
		List<String> compressionCommandParamsList = new ArrayList<String>();
		compressionCommandParamsList.add("ffmpeg");
		compressionCommandParamsList.add("-i");
		compressionCommandParamsList.add(sourceFilePathname);
		compressionCommandParamsList.add("-acodec");
		compressionCommandParamsList.add("copy");
		compressionCommandParamsList.add("-vcodec");
		compressionCommandParamsList.add("ffv1");
		compressionCommandParamsList.add("-level");
		compressionCommandParamsList.add("3");
		compressionCommandParamsList.add("-coder");
		compressionCommandParamsList.add("1");
		compressionCommandParamsList.add("-context");
		compressionCommandParamsList.add("1");
		compressionCommandParamsList.add("-g");
		compressionCommandParamsList.add("1");
		compressionCommandParamsList.add("-slicecrc");
		compressionCommandParamsList.add("1");
		compressionCommandParamsList.add("-map");
		compressionCommandParamsList.add("0:v");
		compressionCommandParamsList.add("-map");
		compressionCommandParamsList.add("0:a");
		compressionCommandParamsList.add(compressedFileTargetLocation);
	
		return compressionCommandParamsList;
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
