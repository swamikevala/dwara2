package org.ishafoundation.digitization.preservation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

@Component("video-digi-2020-preservation-gen")
@Primary
@Profile({ "!dev & !stage" })
public class Video_Digitization_Transcoding_TaskExecutor extends MediaTask implements IProcessingTask{
    private static final Logger logger = LoggerFactory.getLogger(Video_Digitization_Transcoding_TaskExecutor.class);
    
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
	
		String taskName = processContext.getJob().getProcessingtaskId();
		LogicalFile logicalFile = processContext.getLogicalFile();
		Integer fileId = processContext.getFile().getId();
		String destinationDirPath = processContext.getOutputDestinationDirPath();

		if(logger.isTraceEnabled()) {
			logger.trace("taskName " + taskName);
//			logger.trace("inputArtifactName " + inputArtifactName); // V22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
//			logger.trace("outputArtifactName " + outputArtifactName); // VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
			logger.trace("fileId " + fileId);
//			logger.trace("domain " + domain.name()); 
			logger.trace("logicalFile " + logicalFile.getAbsolutePath()); // /data/ingested/V22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12/DCIM/100EOS5D/MVI_5594.MOV
//			logger.trace("category " + category); // public
			logger.trace("destinationDirPath " + destinationDirPath); // /data/transcoded/public/VL22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12/DCIM/100EOS5D
		}
		String sourceFilePathname = logicalFile.getAbsolutePath();
		long startms = System.currentTimeMillis();
	
		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		
		FileUtils.forceMkdir(new File(destinationDirPath));
		
		String compressedFileTmpTargetLocation = destinationDirPath + File.separator + fileName + "_tmp" + PfrConstants.MKV_EXTN ;// TODO How do we know if it should be mkv or mxf or what not???
		String compressedFileTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.MKV_EXTN;// TODO How do we know if it should be mkv or mxf or what not???	
		
		/*************** COMPRESSION ***************/
		long proxyStartTime = System.currentTimeMillis();
		logger.info("Compression starts for " + sourceFilePathname + " - targetLocation is - " + compressedFileTmpTargetLocation);
	
		// Doing this command creation and execution in 2 steps so that the process can be referenced in memory and so if cancel command for a specific medialibrary is issued the specific process(es) can be destroyed/killed referencing this...
		// mapping only for proxy generation commands which are slightly heavy and time consuming than the thumbnail and metadata extraction...
		List<String> compressionCommandParamsList = getCompressionCommand(sourceFilePathname, compressedFileTmpTargetLocation);
		CommandLineExecutionResponse compressionCommandLineExecutionResponse = createProcessAndExecuteCommand(fileId+"~"+taskName , compressionCommandParamsList);
		if(compressionCommandLineExecutionResponse.isComplete())
			logger.info("Compression successful - " + compressedFileTmpTargetLocation);
		
		// DU-241 - The ffmpeg output has the audio tracks NOT in proper aligment in a cluster. So we have to run this
		// TODO - the mkvmerge command has a lowerlimit of 100ms which is putting more than 1 frame in a cluster. We ideally want just 1 frame/cluster. Swami liasing with the mkvtool developer...
		// TODO - Not able to put 5 frames in one cluster...
		// TODO - handle ntsc vs pal on cluster-length 
		logger.info("Now aligning the tracks in cluster");
		String mkvmergeCommand = "mkvmerge -o " + compressedFileTargetLocation + " " + compressedFileTmpTargetLocation + " --cluster-length 5";
		CommandLineExecutionResponse mkvmergeCommandLineExecutionResponse = commandLineExecuter.executeCommand(mkvmergeCommand);
		if(mkvmergeCommandLineExecutionResponse.isComplete()) {
			logger.info("Alignment successful - " + compressedFileTargetLocation);
			new File(compressedFileTmpTargetLocation).delete();
		}
		
		long proxyEndTime = System.currentTimeMillis();
	
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
}
