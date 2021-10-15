package org.ishafoundation.videopub.transcoding.ffmpeg.video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.configuration.FfmpegThreadConfiguration;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
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
	
	@Autowired
	private FfmpegThreadConfiguration ffmpegThreadConfiguration;
	
	private String processingtaskName = "video-proxy-low-gen";

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
		
		long startms = System.currentTimeMillis();
		
		String m01FileLocPath = sourceFilePathname.replace("." + FilenameUtils.getExtension(sourceFilePathname), "M01.XML");
	
		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		String thumbnailTargetLocation = destinationDirPath + File.separator + fileName + ".jpg";
	
		/*************** THUMBNAIL GENERATION ***************/
		long thumbnailStartTime = System.currentTimeMillis();
		logger.info("Thumbnail Generation start for " + containerName + " - targetLocation is - " + thumbnailTargetLocation);
		
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
		
		String highResMetaTargetLocation =  destinationDirPath + File.separator + fileName + ".mp4_ffprobe_out"; //proxyTargetLocation.replace(FilenameUtils.getExtension(proxyTargetLocation), "mp4_ffprobe_out");
		
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
		CommandLineExecutionResponse proxyCommandLineExecutionResponse = createProcessAndExecuteCommand(fileId+"~"+taskName , proxyGenerationCommandParamsList);
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

	// ffmpeg -y -i <<sourceFilePathname>> -map 0:v:0 -map 0:a:0? -map 0:a:1? -preset slow -strict -2 -f mp4 -timecode <<reversedTimeCode>> -vcodec libx264 -b:v 520000 -r 25.0 -pix_fmt yuv420p -crf 18 -vf [in]scale=640x360[scaled] -acodec aac -ar 16000 -b:a 80000 -ac 1 <<proxyTargetLocation>> 
	// ffmpeg -y -i "Tel Dub.mp4" -map 0:v:0 -map 0:a:0? -map 0:a:1? -preset slow -strict -2 -f mp4 -timecode <<reversedTimeCode>> -vcodec libx264 -b:v 520000 -r 25.0 -pix_fmt yuv420p -crf 18 -vf [in]scale=640x360[scaled] -acodec aac -ar 16000 -b:a 80000 -ac 1 "output/Tel Dub.mp4" 
	private List<String> getProxyGenCommand(String sourceFilePathname, String reversedTimeCode, String proxyTargetLocation) {
		List<String> proxyGenerationCommandParamsList = new ArrayList<String>();
		
		// HACK - processcontext.getpriority will not reflect for already queued processing jobs if the priority is changed dynamically... hence taking this route
//		ThreadPoolExecutor executor = (ThreadPoolExecutor) IProcessingTask.taskName_executor_map.get(processingtaskName);
//		BasicThreadFactory factory = (BasicThreadFactory) executor.getThreadFactory();
//
//		proxyGenerationCommandParamsList.add("nice");
//		proxyGenerationCommandParamsList.add("-n");
//		proxyGenerationCommandParamsList.add(factory.getPriority()+"");
		proxyGenerationCommandParamsList.add("ffmpeg");
		proxyGenerationCommandParamsList.add("-y");
		proxyGenerationCommandParamsList.add("-i");
		proxyGenerationCommandParamsList.add(sourceFilePathname);
		proxyGenerationCommandParamsList.add("-map");
		proxyGenerationCommandParamsList.add("0:v:0");
		proxyGenerationCommandParamsList.add("-map");
		proxyGenerationCommandParamsList.add("0:a:0?");
		proxyGenerationCommandParamsList.add("-map");
		proxyGenerationCommandParamsList.add("0:a:1?");
		if(ffmpegThreadConfiguration.getVideoProxyLowGen().getThreads() > 0) {
			proxyGenerationCommandParamsList.add("-threads");
			String ffmpegThreads = ffmpegThreadConfiguration.getVideoProxyLowGen().getThreads() + "";
			proxyGenerationCommandParamsList.add(ffmpegThreads);
		}
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
		proxyGenerationCommandParamsList.add("1");
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
