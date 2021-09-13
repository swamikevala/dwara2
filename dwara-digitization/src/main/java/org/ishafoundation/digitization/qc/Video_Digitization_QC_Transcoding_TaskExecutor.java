package org.ishafoundation.digitization.qc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.PfrConstants;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.configuration.FfmpegThreadConfiguration;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.ishafoundation.videopub.transcoding.ffmpeg.MediaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("video-digi-2020-qc-gen")
@Primary
//@Profile({ "!dev & !stage" })
public class Video_Digitization_QC_Transcoding_TaskExecutor extends MediaTask implements IProcessingTask{
    private static final Logger logger = LoggerFactory.getLogger(Video_Digitization_QC_Transcoding_TaskExecutor.class);
    
//	@Value("${ffmpeg.video-digi-2020-qc-gen.threads}")
//	private String ffmpegThreads;
    
	@Autowired
	private FfmpegThreadConfiguration ffmpegThreadConfiguration;
	
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
		
		String compressedFileTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.MKV_EXTN;	
		
		/*************** COMPRESSION ***************/
		long proxyStartTime = System.currentTimeMillis();
		logger.info("Compression starts for " + sourceFilePathname + " - targetLocation is - " + compressedFileTargetLocation);
	
		// Doing this command creation and execution in 2 steps so that the process can be referenced in memory and so if cancel command for a specific medialibrary is issued the specific process(es) can be destroyed/killed referencing this...
		// mapping only for proxy generation commands which are slightly heavy and time consuming than the thumbnail and metadata extraction...
		List<String> compressionCommandParamsList = getCompressionCommand(sourceFilePathname, compressedFileTargetLocation);
		CommandLineExecutionResponse compressionCommandLineExecutionResponse = createProcessAndExecuteCommand(fileId+"~"+taskName , compressionCommandParamsList);
		if(compressionCommandLineExecutionResponse.isComplete())
			logger.info("Compression successful - " + compressedFileTargetLocation);
		
		// TODO : better this...
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setDestinationPathname(compressedFileTargetLocation);
		processingtaskResponse.setIsComplete(compressionCommandLineExecutionResponse.isComplete());
		processingtaskResponse.setIsCancelled(compressionCommandLineExecutionResponse.isCancelled());
		processingtaskResponse.setStdOutResponse(compressionCommandLineExecutionResponse.getStdOutResponse());
		processingtaskResponse.setFailureReason(compressionCommandLineExecutionResponse.getFailureReason());

		return processingtaskResponse;
	}
	
	//ffmpeg -i "$filename" -loglevel error -c:v libx264 -crf 8 -c:a aac -map 0 -strict -2 /data/ffv1-converted/$ou
	private List<String> getCompressionCommand(String sourceFilePathname, String compressedFileTargetLocation) {
		List<String> compressionCommandParamsList = new ArrayList<String>();
		compressionCommandParamsList.add("ffmpeg");
		compressionCommandParamsList.add("-y");
		compressionCommandParamsList.add("-i");
		compressionCommandParamsList.add(sourceFilePathname);
		if(ffmpegThreadConfiguration.getVideoDigi2020QcGen().getThreads() > 0) {
			compressionCommandParamsList.add("-threads");
			String ffmpegThreads = ffmpegThreadConfiguration.getVideoDigi2020QcGen().getThreads() + "";
			compressionCommandParamsList.add(ffmpegThreads);
		}
		compressionCommandParamsList.add("-loglevel");
		compressionCommandParamsList.add("error");
		compressionCommandParamsList.add("-c:v");
		compressionCommandParamsList.add("libx264");
		compressionCommandParamsList.add("-crf");
		compressionCommandParamsList.add("8");
		compressionCommandParamsList.add("-c:a");
		compressionCommandParamsList.add("aac");
		compressionCommandParamsList.add("-map");
		compressionCommandParamsList.add("0");
		compressionCommandParamsList.add("-strict");
		compressionCommandParamsList.add("-2");
		compressionCommandParamsList.add(compressedFileTargetLocation);
	
		return compressionCommandParamsList;
	}
}
