package org.ishafoundation.videopub.transcoding.ffmpeg.video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.videopub.transcoding.ffmpeg.M01XmlFileHandler;
import org.ishafoundation.videopub.transcoding.ffmpeg.MediaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("video-proxy-low-gen")
@Primary
@Profile({ "!dev & !stage" })
public class Video_LowResolution_Transcoding_TaskExecutor extends MediaTask implements IProcessingTask{
    private static final Logger logger = LoggerFactory.getLogger(Video_LowResolution_Transcoding_TaskExecutor.class);
    
	@Autowired
	private M01XmlFileHandler m01xfh;	

	@Override
	public ProcessingtaskResponse execute(String taskName, String inputArtifactName, String outputArtifactName,
			org.ishafoundation.dwaraapi.db.model.transactional.domain.File file, Domain domain, LogicalFile logicalFile,
			String category, String destinationDirPath) throws Exception {
		
		if(logger.isTraceEnabled()) {
			logger.trace("taskName " + taskName);
			logger.trace("inputArtifactName " + inputArtifactName);
			logger.trace("outputArtifactName " + outputArtifactName);
			logger.trace("fileId " + file.getId());
			logger.trace("domain " + domain.name());
			logger.trace("logicalFile " + logicalFile.getAbsolutePath());
			logger.trace("category " + category);
			logger.trace("destinationDirPath " + destinationDirPath);
		}
		String sourceFilePathname = logicalFile.getAbsolutePath();
		String clipName = FilenameUtils.getName(sourceFilePathname);
		
		String identifierSuffix = "";//mediaFileId+"";//  +  "-" + VideoProcessingSteps.PROXY_GENERATION.toString();
		String containerName = identifierSuffix;	
		
	
		long startms = System.currentTimeMillis();
		
		String m01FileLocPath = sourceFilePathname.replace("." + FilenameUtils.getExtension(sourceFilePathname), "M01.XML");
	
		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		if(StringUtils.isNotBlank(FilenameUtils.getExtension(inputArtifactName))) // means the source file is the artifact itself and not a directory
			fileName = FilenameUtils.getBaseName(outputArtifactName);
		
		String thumbnailTargetLocation = destinationDirPath + File.separator + fileName + ".jpg";
	
		/*************** THUMBNAIL GENERATION ***************/
		long thumbnailStartTime = System.currentTimeMillis();
		logger.info("Thumbnail Generation start for " + containerName + " - targetLocation is - " + thumbnailTargetLocation);
		
		FileUtils.forceMkdir(new File(destinationDirPath));
		
		List<String> thumbnailGenerationCommandParamsList = getThumbnailGenerationCommand(sourceFilePathname, thumbnailTargetLocation);
		CommandLineExecutionResponse thumbnailCommandLineExecutionResponse = commandLineExecuter.executeCommand(thumbnailGenerationCommandParamsList);
		
		long thumbnailEndTime = System.currentTimeMillis();
		if(thumbnailCommandLineExecutionResponse.isComplete()) {
			logger.info("Thumbnail for " + containerName + " created successfully in " + ((thumbnailEndTime - thumbnailStartTime)/1000) + " seconds - " + thumbnailTargetLocation);
		}else {
			throw new Exception("Unable to generate thumbnail " + thumbnailTargetLocation + " : because : " + thumbnailCommandLineExecutionResponse.getFailureReason());
		}
		
		/*
		 * targetFileLocPathsuffix will be something like /proxies/1825_SOI-Concert_Nandi_IYC_20-Jan-11_Cam1/123-345-PROXYGENERATION/KHS10267_01.MP4
		 * we had to add the containerName(mlID-cardId-processName say e.g., something like 123-345-PROXYGENERATION) as XAVC file names across card folders are same...
		 * 
		 */
		String proxyTargetLocation = destinationDirPath + File.separator + fileName + ".mp4";		
		
		String highResMetaTargetLocation = proxyTargetLocation.replace(".mp4", ".mp4_ffprobe_out");
		
		/*************** METADATA EXTRACTION ***************/
		long metaStartTime = System.currentTimeMillis();
		logger.info("Meta Generation start for " + containerName + " - targetLocation is - " + highResMetaTargetLocation);
		
		List<String> metaDataExtractionCommandParamsList = getMetaDataExtractionCommand(sourceFilePathname);
		CommandLineExecutionResponse metaCommandLineExecutionResponse = commandLineExecuter.executeCommand(metaDataExtractionCommandParamsList);
		
		long metaEndTime = System.currentTimeMillis();
		if(metaCommandLineExecutionResponse.isComplete()) {
			try {
				FileUtils.write(new File(highResMetaTargetLocation), metaCommandLineExecutionResponse.getStdOutResponse(), "UTF-8");
				logger.info("Meta data file for " + containerName + " created successfully in " + ((metaEndTime - metaStartTime)/1000) + " seconds - " + highResMetaTargetLocation);
			} catch (Exception e) {
				throw new Exception("Unable to save Meta data file for " + containerName + " - " + highResMetaTargetLocation + e.getMessage());
			}
		}else {
			throw new Exception("Unable to save meta data file " + highResMetaTargetLocation + " : because : " + metaCommandLineExecutionResponse.getFailureReason());
		}	
		
		/*************** PROXY GENERATION ***************/
		long proxyStartTime = System.currentTimeMillis();
		logger.info("Proxy Generation start for " + containerName + " - targetLocation is - " + proxyTargetLocation);
	
		String timeCode = "00000000";
		if(m01FileLocPath != null && new File(m01FileLocPath).exists()) {
			try {
				timeCode = m01xfh.getTimeCode(new File(m01FileLocPath));
			} catch (Throwable e) {
				logger.warn("Defaulting to " + timeCode + " as unable to fetch timecode from " + m01FileLocPath + " : " + e.getMessage()); // For Mobile videos there isnt a M01.XML file...
			}
		}
		String reversedTimeCode = getReversedTimeCode(timeCode);
		
	//		String proxyGenerationCmd = "ffmpeg -y -i \"" + sourceFilePathname + "\" -preset slow -strict -2 -f mp4 -timecode " + reversedTimeCode + " -vcodec libx264 \"-b:v\" 520000 -r 25.0 -pix_fmt yuv420p -crf 18 -vf \"[in]scale=640x360[scaled]\" -acodec aac -ar 16000 \"-b:a\" 80000 -ac 2 \"" + proxyTargetLocation + "\"";
	//		CommandLineExecutionResponse proxyCommandLineExecutionResponse = commandLineExecuter.executeCommand(proxyGenerationCmd, proxyStdErrFileLoc);
	
		// Doing this command creation and execution in 2 steps so that the process can be referenced in memory and so if cancel command for a specific medialibrary is issued the specific process(es) can be destroyed/killed referencing this...
		// mapping only for proxy generation commands which are slightly heavy and time consuming than the thumbnail and metadata extraction...
		List<String> proxyGenerationCommandParamsList = getProxyGenCommand(sourceFilePathname, reversedTimeCode, proxyTargetLocation);
		CommandLineExecutionResponse proxyCommandLineExecutionResponse = createProcessAndExecuteCommand(file.getId()+"~"+taskName , proxyGenerationCommandParamsList);
		long proxyEndTime = System.currentTimeMillis();
	
		// TODO : better this...
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setDestinationPathname(proxyTargetLocation);
		processingtaskResponse.setIsComplete(proxyCommandLineExecutionResponse.isComplete());
		processingtaskResponse.setIsCancelled(proxyCommandLineExecutionResponse.isCancelled());
		processingtaskResponse.setStdOutResponse(proxyCommandLineExecutionResponse.getStdOutResponse());
		processingtaskResponse.setFailureReason(proxyCommandLineExecutionResponse.getFailureReason());

		return processingtaskResponse;
	}
	
	private List<String> getThumbnailGenerationCommand(String sourceFilePathname, String thumbnailTargetLocation) {
		List<String> thumbnailGenerationCommandParamsList = new ArrayList<String>();
		thumbnailGenerationCommandParamsList.add("ffmpeg");
		thumbnailGenerationCommandParamsList.add("-y");
		thumbnailGenerationCommandParamsList.add("-i");
		thumbnailGenerationCommandParamsList.add(sourceFilePathname);
		thumbnailGenerationCommandParamsList.add("-ss");
		thumbnailGenerationCommandParamsList.add("0");
		thumbnailGenerationCommandParamsList.add("-vframes");
		thumbnailGenerationCommandParamsList.add("1");
		thumbnailGenerationCommandParamsList.add("-s");
		thumbnailGenerationCommandParamsList.add("192x108");
		thumbnailGenerationCommandParamsList.add(thumbnailTargetLocation);
		
		return thumbnailGenerationCommandParamsList;
	}
	
	private List<String> getMetaDataExtractionCommand(String sourceFilePathname) {
		List<String> metaDataExtractionCommandParamsList = new ArrayList<String>();
		metaDataExtractionCommandParamsList.add("ffprobe");
		metaDataExtractionCommandParamsList.add("-i");
		metaDataExtractionCommandParamsList.add(sourceFilePathname);
		metaDataExtractionCommandParamsList.add("-hide_banner");
		metaDataExtractionCommandParamsList.add("-v");
		metaDataExtractionCommandParamsList.add("quiet");
		metaDataExtractionCommandParamsList.add("-print_format");
		metaDataExtractionCommandParamsList.add("json");
		metaDataExtractionCommandParamsList.add("-show_format");
		metaDataExtractionCommandParamsList.add("-show_streams");			
	
		return metaDataExtractionCommandParamsList;
	}
	
	private List<String> getProxyGenCommand(String sourceFilePathname, String reversedTimeCode, String proxyTargetLocation) {
		List<String> proxyGenerationCommandParamsList = new ArrayList<String>();
		proxyGenerationCommandParamsList.add("ffmpeg");
		proxyGenerationCommandParamsList.add("-y");
		proxyGenerationCommandParamsList.add("-i");
		proxyGenerationCommandParamsList.add(sourceFilePathname);
		proxyGenerationCommandParamsList.add("-preset");
		proxyGenerationCommandParamsList.add("slow");
		proxyGenerationCommandParamsList.add("-strict");
		proxyGenerationCommandParamsList.add("-2");
		proxyGenerationCommandParamsList.add("-f");
		proxyGenerationCommandParamsList.add("mp4");
		proxyGenerationCommandParamsList.add("-timecode");
		proxyGenerationCommandParamsList.add(reversedTimeCode);
		proxyGenerationCommandParamsList.add("-vcodec");
		proxyGenerationCommandParamsList.add("libx264");
		proxyGenerationCommandParamsList.add("-b:v");
		proxyGenerationCommandParamsList.add("520000");
		proxyGenerationCommandParamsList.add("-r");
		proxyGenerationCommandParamsList.add("25.0");
		proxyGenerationCommandParamsList.add("-pix_fmt");
		proxyGenerationCommandParamsList.add("yuv420p");
		proxyGenerationCommandParamsList.add("-crf");
		proxyGenerationCommandParamsList.add("18");
		proxyGenerationCommandParamsList.add("-vf");
		proxyGenerationCommandParamsList.add("[in]scale=640x360[scaled]");
		proxyGenerationCommandParamsList.add("-acodec");
		proxyGenerationCommandParamsList.add("aac");
		proxyGenerationCommandParamsList.add("-ar");
		proxyGenerationCommandParamsList.add("16000");
		proxyGenerationCommandParamsList.add("-b:a");
		proxyGenerationCommandParamsList.add("80000");
		proxyGenerationCommandParamsList.add("-ac");
		proxyGenerationCommandParamsList.add("2");
		proxyGenerationCommandParamsList.add(proxyTargetLocation);
	
		return proxyGenerationCommandParamsList;
	}
	
	private String getReversedTimeCode(String timeCodeFromM01XmlFile){	
		
	    String timeCode1stN2ndDigit = timeCodeFromM01XmlFile.substring(0, 2);
	    String timeCode3rdN4thDigit = timeCodeFromM01XmlFile.substring(2, 4);
	    String timeCode5thN6thDigit = timeCodeFromM01XmlFile.substring(4, 6);
	    String timeCode7thN8thDigit = timeCodeFromM01XmlFile.substring(6, 8);
	    
	    String reversedTimeCode = timeCode7thN8thDigit + ":" + timeCode5thN6thDigit + ":" + timeCode3rdN4thDigit + ":" + timeCode1stN2ndDigit;
	    return reversedTimeCode;
	}
}
