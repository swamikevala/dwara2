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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("video-digi-2020-mkv-mov-gen")
@Primary
@Profile({ "!dev & !stage" })
public class Video_Digitization_MkvToMov_Convertor_TaskExecutor extends MediaTask implements IProcessingTask{
    private static final Logger logger = LoggerFactory.getLogger(Video_Digitization_MkvToMov_Convertor_TaskExecutor.class);
	
	@Value("${scheduler.statusUpdater.enabled:true}")
	private boolean isEnabled;

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
	
		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		FileUtils.forceMkdir(new File(destinationDirPath));
		
		String outputFileTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.MOV_EXTN ;// TODO How do we know if it should be mkv or mxf or what not???

		/*************** CONVERSION ***************/
		logger.info("Mov Conversion starts for " + sourceFilePathname + " - targetLocation is - " + outputFileTargetLocation);
	
		// Doing this command creation and execution in 2 steps so that the process can be referenced in memory and so if cancel command for a specific medialibrary is issued the specific process(es) can be destroyed/killed referencing this...
		// mapping only for proxy generation commands which are slightly heavy and time consuming than the thumbnail and metadata extraction...
		List<String> conversionCommandParamsList = getConversionCommand(sourceFilePathname, outputFileTargetLocation);
		CommandLineExecutionResponse conversionCommandLineExecutionResponse = createProcessAndExecuteCommand(fileId+"~"+taskName , conversionCommandParamsList);
		if(conversionCommandLineExecutionResponse.isComplete())
			logger.info("Mov Conversion successful - " + outputFileTargetLocation);
		
		// delete the mkv
		new File(sourceFilePathname).delete();
	
		// TODO : better this...
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setDestinationPathname(outputFileTargetLocation);
		processingtaskResponse.setIsComplete(conversionCommandLineExecutionResponse.isComplete());
		processingtaskResponse.setIsCancelled(conversionCommandLineExecutionResponse.isCancelled());
		processingtaskResponse.setStdOutResponse(conversionCommandLineExecutionResponse.getStdOutResponse());
		processingtaskResponse.setFailureReason(conversionCommandLineExecutionResponse.getFailureReason());

		return processingtaskResponse;
	}
	
	// ffmpeg -y -i <<sourceFilePathname>> -acodec copy -pix_fmt yuv422p -r 25 -c:v dvvideo -vf "scale=720:576" -b:v 50M <<outputFileTargetLocation>>
	// ffmpeg -y -i AC30.mkv -acodec copy -pix_fmt yuv422p -r 25 -c:v dvvideo -vf "scale=720:576" -b:v 50M AC30-2.mov
	private List<String> getConversionCommand(String sourceFilePathname, String outputFileTargetLocation) {
		List<String> compressionCommandParamsList = new ArrayList<String>();
		
		compressionCommandParamsList.add("ffmpeg");
		compressionCommandParamsList.add("-y");
		compressionCommandParamsList.add("-i");
		compressionCommandParamsList.add(sourceFilePathname);
		compressionCommandParamsList.add("-acodec");
		compressionCommandParamsList.add("copy");
		compressionCommandParamsList.add("-pix_fmt");
		compressionCommandParamsList.add("yuv422p");
		compressionCommandParamsList.add("-r");
		compressionCommandParamsList.add("25");
		compressionCommandParamsList.add("-c:v");
		compressionCommandParamsList.add("dvvideo");
		compressionCommandParamsList.add("-vf");
		//compressionCommandParamsList.add("\"scale=720:576\"");
		compressionCommandParamsList.add("[in]scale=720x576[scaled]");
		compressionCommandParamsList.add("-b:v");
		compressionCommandParamsList.add("50M");
		compressionCommandParamsList.add(outputFileTargetLocation);
	
		return compressionCommandParamsList;
	}
}
