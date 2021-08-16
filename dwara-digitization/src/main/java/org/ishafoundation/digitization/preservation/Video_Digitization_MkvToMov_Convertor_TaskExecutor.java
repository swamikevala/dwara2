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
		List<String> tagList = processContext.getTags();

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


		/*************** DETERMINE TYPE ***************/
		/* Instead of relying on tags for determining the video type we are now probing the file for the type 		
		String videoDigiType = "dv-pal";
		for (String nthTag : tagList) {
			if(nthTag.startsWith("video-digi-type")) {
				videoDigiType = nthTag.split(":")[1];
			}
		}
		*/
		
		List<String> typeDeterminationCommandParamsList = getTypeDeterminationCommand(sourceFilePathname);
		CommandLineExecutionResponse typeDeterminationCommandLineExecutionResponse = createProcessAndExecuteCommand(fileId+"~"+taskName+"~Type" , typeDeterminationCommandParamsList);
		String videoType = null;
		if(typeDeterminationCommandLineExecutionResponse.isComplete()) {
			String response = typeDeterminationCommandLineExecutionResponse.getStdOutResponse().trim();
			/*
				hdv - 1920x1080
				dv-pal - 720x608
				dv-ntsc - 720x512
	 		*/
			if(response.equals("1920x1080"))
				videoType = "hdv";
			else if(response.equals("720x608"))
				videoType = "dv-pal";
			else if(response.equals("720x512"))
				videoType = "dv-ntsc";
			
			if(videoType == null)
				throw new Exception("Unable to determine video type for dimension " + response);
			
			logger.info("Video type determination successful - " + videoType);
		}
		else
			throw new Exception("Unable to determine video type");


		if(videoType.equals("hdv"))
			throw new Exception("hdv is not supported for mov conversion yet");
				
		/*************** CONVERSION ***************/
		String outputFileTargetLocation = destinationDirPath + File.separator + fileName + PfrConstants.MOV_EXTN ;// TODO How do we know if it should be mkv or mxf or what not???
		logger.info("Mov Conversion starts for " + sourceFilePathname + " - targetLocation is - " + outputFileTargetLocation);
		
		List<String> conversionCommandParamsList = getConversionCommand(sourceFilePathname, outputFileTargetLocation, videoType);
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

	// ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 A1353.mkv
	// ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 <<sourceFilePathname>>
	private List<String> getTypeDeterminationCommand(String sourceFilePathname) {
		List<String> typeDeterminationCommandParamsList = new ArrayList<String>();
		
		typeDeterminationCommandParamsList.add("ffprobe");
		typeDeterminationCommandParamsList.add("-v");
		typeDeterminationCommandParamsList.add("error");
		typeDeterminationCommandParamsList.add("-select_streams");
		typeDeterminationCommandParamsList.add("v:0");
		typeDeterminationCommandParamsList.add("-show_entries");
		typeDeterminationCommandParamsList.add("stream=width,height");
		typeDeterminationCommandParamsList.add("-of");
		typeDeterminationCommandParamsList.add("csv=s=x:p=0");
		typeDeterminationCommandParamsList.add(sourceFilePathname);
	
		return typeDeterminationCommandParamsList;
	}

	
	/*
		pal
			ffmpeg -y -i E221.mkv -acodec copy -pix_fmt yuv422p -r 25 -c:v dvvideo -vf "crop=720:576:0:32" -b:v 50M E221-crop.mov
			ffmpeg -y -i E221.mkv -map 0:v:0 -map 0:a:0? -map 0:a:1? -acodec copy -pix_fmt yuv422p -r 25 -c:v dvvideo -vf crop=720:576:0:32,setdar=4/3 -b:v 50M E221-v3.mov
			
		ntsc
			ffmpeg -y -i N880.mkv -acodec copy -pix_fmt yuv411p -r 30000/1001 -c:v dvvideo -vf "crop=720:480:0:32" -b:v 50M N880-11-crop.mov
			ffmpeg -y -i N880.mkv -map 0:v:0 -map 0:a:0? -map 0:a:1? -acodec copy -pix_fmt yuv422p -r 30000/1001 -c:v dvvideo -vf crop=720:480:0:32,setdar=4/3 -b:v 50M N880-11-v3.mov
	*/
	
	// ffmpeg -y -i <<sourceFilePathname>> -acodec copy -pix_fmt <<pixFmt>> -r <<r>> -c:v dvvideo -vf "crop=<<dimension>>" -b:v 50M <<outputFileTargetLocation>>
	private List<String> getConversionCommand(String sourceFilePathname, String outputFileTargetLocation, String videoType) {
		
		// by default set it to pal
		String pixFmt = "yuv422p";
		String r = "25";
		String cropDimension = "720:576:0:32";

		if(videoType.equals("dv-ntsc")) {
			pixFmt = "yuv411p";
			r = "30000/1001";
			cropDimension = "720:480:0:32";
		}

		
		List<String> compressionCommandParamsList = new ArrayList<String>();
		
		compressionCommandParamsList.add("ffmpeg");
		compressionCommandParamsList.add("-y");
		compressionCommandParamsList.add("-i");
		compressionCommandParamsList.add(sourceFilePathname);
		compressionCommandParamsList.add("-map");
		compressionCommandParamsList.add("0:v:0");
		compressionCommandParamsList.add("-map");
		compressionCommandParamsList.add("0:a:0?");
		compressionCommandParamsList.add("-map");
		compressionCommandParamsList.add("0:a:1?");
		compressionCommandParamsList.add("-acodec");
		compressionCommandParamsList.add("copy");
		compressionCommandParamsList.add("-pix_fmt");
		compressionCommandParamsList.add(pixFmt);
		compressionCommandParamsList.add("-r");
		compressionCommandParamsList.add(r);
		compressionCommandParamsList.add("-c:v");
		compressionCommandParamsList.add("dvvideo");
		compressionCommandParamsList.add("-vf");
		compressionCommandParamsList.add("crop=" + cropDimension + ",setdar=4/3");
		//compressionCommandParamsList.add("[in]crop=" + cropDimension + ",setdar=4/3[cropped]");
		compressionCommandParamsList.add("-b:v");
		compressionCommandParamsList.add("50M");
		compressionCommandParamsList.add(outputFileTargetLocation);
	
		return compressionCommandParamsList;
	}
}
