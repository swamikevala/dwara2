package org.ishafoundation.videopub.transcoding.photo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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

@Component("photo-proxy-gen")
@Primary
@Profile({ "!dev & !stage" })
public class Photo_LowResolution_Transcoding_TaskExecutor extends MediaTask implements IProcessingTask{
    private static final Logger logger = LoggerFactory.getLogger(Photo_LowResolution_Transcoding_TaskExecutor.class);
    
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
		
		String identifierSuffix = "";//mediaFileId+"";//  +  "-" + VideoProcessingSteps.PROXY_GENERATION.toString();
		String containerName = identifierSuffix;	
		
		FileUtils.forceMkdir(new File(destinationDirPath));
	
		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		String thumbnailTargetLocation = destinationDirPath + File.separator + fileName + ".thm";
		String proxyTargetLocation = destinationDirPath + File.separator + fileName + "_p" + ".jpg";
	
		long conversionStartTime = System.currentTimeMillis();
		
		String proxyCommand = getProxyCommand(sourceFilePathname, thumbnailTargetLocation, proxyTargetLocation);
		Process proc = commandLineExecuter.createProcess(proxyCommand);
		CommandLineExecutionResponse proxyCommandLineExecutionResponse = commandLineExecuter.executeCommand(proxyCommand, proc);
		
		long conversionEndTime = System.currentTimeMillis();
		if(proxyCommandLineExecutionResponse.isComplete()) {
			logger.info("Conversion for " + containerName + " success in " + ((conversionEndTime - conversionStartTime)/1000) + " seconds - " + thumbnailTargetLocation);
		}else {
			throw new Exception("Unable to convert " + thumbnailTargetLocation + " : because : " + proxyCommandLineExecutionResponse.getFailureReason());
		}

		// Copy the xmp file from source to derived folder...
		File xmpSidecarFile = logicalFile.getSidecarFile("xmp");
		if(xmpSidecarFile != null) {
			FileUtils.copyFile(xmpSidecarFile, new File(destinationDirPath + File.separator + fileName + ".xmp"));  
		}else {
			List<String> extractXmpCommandParamsList = extractXmpCommand(sourceFilePathname, destinationDirPath);
			CommandLineExecutionResponse extractXmpCommandLineExecutionResponse = commandLineExecuter.executeCommand(extractXmpCommandParamsList);
		}
		
		
		// TODO : better this...
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setDestinationPathname(proxyTargetLocation);
		processingtaskResponse.setIsComplete(proxyCommandLineExecutionResponse.isComplete());
		processingtaskResponse.setIsCancelled(proxyCommandLineExecutionResponse.isCancelled());
		processingtaskResponse.setStdOutResponse(proxyCommandLineExecutionResponse.getStdOutResponse());
		processingtaskResponse.setFailureReason(proxyCommandLineExecutionResponse.getFailureReason());

		return processingtaskResponse;
	}

	// convert 20190716_VVD_0206.NEF \( +clone -resize 192 -quality 50 -write 20190716_VVD_0206-s33.jpg +delete \) -resize 1536 -quality 85 20190716_VVD_0206-s32.jpg
	private String getProxyCommand(String sourceFilePathname, String thumbnailTargetLocation, String proxyTargetLocation) {
		return "convert " + sourceFilePathname + " \\( +clone -resize 192 -quality 50 -write JPEG:" + thumbnailTargetLocation + " +delete \\) -resize 1536 -quality 85 " + proxyTargetLocation;
	}

//	private List<String> getProxyCommandAsList(String sourceFilePathname, String thumbnailTargetLocation, String proxyTargetLocation) {
//		List<String> thumbnailGenerationCommandParamsList = new ArrayList<String>();
//		thumbnailGenerationCommandParamsList.add("convert");
//		thumbnailGenerationCommandParamsList.add(sourceFilePathname);
//		thumbnailGenerationCommandParamsList.add("\\(");
//		thumbnailGenerationCommandParamsList.add("+clone");
//		thumbnailGenerationCommandParamsList.add("-resize");
//		thumbnailGenerationCommandParamsList.add("192");
//		thumbnailGenerationCommandParamsList.add("-quality");
//		thumbnailGenerationCommandParamsList.add("50");
//		thumbnailGenerationCommandParamsList.add("-write");
//		thumbnailGenerationCommandParamsList.add("JPEG:" + thumbnailTargetLocation);
//		thumbnailGenerationCommandParamsList.add("+delete");
//		thumbnailGenerationCommandParamsList.add("\\)");
//		thumbnailGenerationCommandParamsList.add("-resize");
//		thumbnailGenerationCommandParamsList.add("1536");
//		thumbnailGenerationCommandParamsList.add("-quality");
//		thumbnailGenerationCommandParamsList.add("85");
//		thumbnailGenerationCommandParamsList.add(proxyTargetLocation);
//		
//		return thumbnailGenerationCommandParamsList;
//	}
	
	// exiv2 -eX ex 20190207_VVD_0101to0107-mp-e-ot1.tif
	private List<String> extractXmpCommand(String sourceFilePathname, String destinationDirPath) {
		List<String> xmpCommandParamsList = new ArrayList<String>();
		xmpCommandParamsList.add("sh");
		xmpCommandParamsList.add("-c");
		xmpCommandParamsList.add("cd");
		xmpCommandParamsList.add(destinationDirPath);
		xmpCommandParamsList.add(";");
		xmpCommandParamsList.add("exiv2");
		xmpCommandParamsList.add("-eX");
		xmpCommandParamsList.add("ex");
		xmpCommandParamsList.add(sourceFilePathname);
		
		return xmpCommandParamsList;
	}

}
