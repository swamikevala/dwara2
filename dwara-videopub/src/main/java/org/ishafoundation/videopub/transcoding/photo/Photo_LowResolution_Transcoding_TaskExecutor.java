package org.ishafoundation.videopub.transcoding.photo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("photo-proxy-gen")
@Primary
@Profile({ "!dev & !stage" })
public class Photo_LowResolution_Transcoding_TaskExecutor extends MediaTask implements IProcessingTask{
    private static final Logger logger = LoggerFactory.getLogger(Photo_LowResolution_Transcoding_TaskExecutor.class);

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
		
		String identifierSuffix = "";//mediaFileId+"";//  +  "-" + VideoProcessingSteps.PROXY_GENERATION.toString();
		String containerName = identifierSuffix;	
		
		FileUtils.forceMkdir(new File(destinationDirPath));
	
		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		String thumbnailTargetLocation = destinationDirPath + File.separator + fileName + ".thm";
		String proxyTargetLocation = destinationDirPath + File.separator + fileName + "_p" + ".jpg";
	
		long conversionStartTime = System.currentTimeMillis();
		List<String> proxyCommandList = getProxyCommandAsList(sourceFilePathname, destinationDirPath, thumbnailTargetLocation, proxyTargetLocation);
		CommandLineExecutionResponse proxyCommandLineExecutionResponse = commandLineExecuter.executeCommand(proxyCommandList);
		long conversionEndTime = System.currentTimeMillis();
		if(proxyCommandLineExecutionResponse.isComplete()) {
			logger.info("Conversion for " + containerName + " success in " + ((conversionEndTime - conversionStartTime)/1000) + " seconds - " + thumbnailTargetLocation);
		}else {
			throw new Exception("Unable to convert " + thumbnailTargetLocation + " : because : " + proxyCommandLineExecutionResponse.getFailureReason());
		}

		// Copy or extract xmp file
		File xmpSidecarFile = logicalFile.getSidecarFile("xmp");
		if(xmpSidecarFile != null) { // if xmp file already exists with the source - Just copy the xmp file from source to derived folder...
			FileUtils.copyFile(xmpSidecarFile, new File(destinationDirPath + File.separator + fileName + ".xmp"));  
		}else {
			List<String> extractXmpCommandParamsList = extractXmpCommand(sourceFilePathname, destinationDirPath);
			CommandLineExecutionResponse extractXmpCommandLineExecutionResponse = commandLineExecuter.executeCommand(extractXmpCommandParamsList);
			if(proxyCommandLineExecutionResponse.isComplete()) {
				logger.info("Xmp extraction for " + containerName + " success");
			}else {
				throw new Exception("Unable to extract xmp from " + sourceFilePathname + " : because : " + extractXmpCommandLineExecutionResponse.getFailureReason());
			}
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
	
	/*
	We can specify the total pixel area of the photo that we want using "@" suffix

	proxy: 1536x1024 = 1,572,864
	thumbnail: 192x128 = 24,576

	so the command should say "-thumbnail 1572864@" instead of "-thumbnail 1536", and similarly for the thumbnail

	pls can you put this explanation in the code comments
	*/
	private List<String> getProxyCommandAsList(String sourceFilePathname, String destinationDirPath, String thumbnailTargetLocation, String proxyTargetLocation) {
		List<String> proxyGenerationCommandParamsList = new ArrayList<String>();
		proxyGenerationCommandParamsList.add("convert");
		proxyGenerationCommandParamsList.add(sourceFilePathname);
		proxyGenerationCommandParamsList.add("(");
		proxyGenerationCommandParamsList.add("+clone");
		proxyGenerationCommandParamsList.add("-thumbnail");
		proxyGenerationCommandParamsList.add("24576@");
		proxyGenerationCommandParamsList.add("-quality");
		proxyGenerationCommandParamsList.add("82");
		proxyGenerationCommandParamsList.add("-strip");
		proxyGenerationCommandParamsList.add("-write");
		proxyGenerationCommandParamsList.add("JPEG:" + thumbnailTargetLocation);
		proxyGenerationCommandParamsList.add("+delete");
		proxyGenerationCommandParamsList.add(")");
		if(ffmpegThreadConfiguration.getPhotoProxyLowGen().getThreads() > 0) {
			proxyGenerationCommandParamsList.add("-threads");
			String ffmpegThreads = ffmpegThreadConfiguration.getPhotoProxyLowGen().getThreads() + "";
			proxyGenerationCommandParamsList.add(ffmpegThreads);
		}
		proxyGenerationCommandParamsList.add("-thumbnail");
		proxyGenerationCommandParamsList.add("1572864@");
		proxyGenerationCommandParamsList.add("-quality");
		proxyGenerationCommandParamsList.add("82");
		proxyGenerationCommandParamsList.add("-strip");
		proxyGenerationCommandParamsList.add(proxyTargetLocation);
		
		return proxyGenerationCommandParamsList;
	}
	
	// exiv2 -eX ex 20190207_VVD_0101to0107-mp-e-ot1.tif
	private List<String> extractXmpCommand(String sourceFilePathname, String destinationDirPath) {
		List<String> xmpCommandParamsList = new ArrayList<String>();
		xmpCommandParamsList.add("exiv2");
		xmpCommandParamsList.add("-eX");
		xmpCommandParamsList.add("-f");
		xmpCommandParamsList.add("-l");
		xmpCommandParamsList.add(destinationDirPath);
		xmpCommandParamsList.add("ex");
		xmpCommandParamsList.add(sourceFilePathname);
		
		return xmpCommandParamsList;
	}
}
