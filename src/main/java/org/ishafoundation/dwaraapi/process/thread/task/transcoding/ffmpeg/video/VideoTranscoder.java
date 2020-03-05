package org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.model.ProxyGenCommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.process.factory.TranscoderFactory;
import org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.ITranscoder;
import org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg.MediaTask;
import org.ishafoundation.dwaraapi.process.transcoding.ffmpeg.utils.M01XmlFileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VideoTranscoder extends MediaTask implements ITranscoder{
    static {
    	TranscoderFactory.register("Video Transcoding Low Resolution", VideoTranscoder.class);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(VideoTranscoder.class);
    
	@Autowired
	private M01XmlFileHandler m01xfh;	
	
	@Override
	public ProxyGenCommandLineExecutionResponse transcode(String taskName, int fileId, String sourceFilePathname,
			String destinationDirPath) throws Exception {
		String clipName = FilenameUtils.getName(sourceFilePathname);
		
		String identifierSuffix = "";//mediaFileId+"";//  +  "-" + VideoProcessingSteps.PROXY_GENERATION.toString();
		String containerName = identifierSuffix;	
		

		long startms = System.currentTimeMillis();
		
		String m01FileLocPath = sourceFilePathname.replace("." + FilenameUtils.getExtension(sourceFilePathname), "M01.XML");

		String thumbnailTargetLocation = destinationDirPath + File.separator + FilenameUtils.getBaseName(sourceFilePathname) + ".jpg";
		String thumbnailStdErrFileLoc = thumbnailTargetLocation.replace(".jpg", ".jpg_ffmpeg_log");

		/*************** THUMBNAIL GENERATION ***************/
		long thumbnailStartTime = System.currentTimeMillis();
		logger.info("Thumbnail Generation start for " + containerName + " - targetLocation is - " + thumbnailTargetLocation);
		
		createFileInIngestServer(thumbnailStdErrFileLoc);
		
//			String cmd = "ffmpeg -y -i \"" + sourceFilePathname + "\" -ss 0 -vframes 1 -s 192x108 \""+ thumbnailTargetLocation + "\"";
//			CommandLineExecutionResponse thumbnailCommandLineExecutionResponse = commandLineExecuter.executeCommand(cmd, thumbnailStdErrFileLoc);

		List<String> thumbnailGenerationCommandParamsList = getThumbnailGenerationCommand(sourceFilePathname, thumbnailTargetLocation);
		CommandLineExecutionResponse thumbnailCommandLineExecutionResponse = commandLineExecuter.executeCommand(thumbnailGenerationCommandParamsList, thumbnailStdErrFileLoc);
		
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
		String proxyTargetLocation = destinationDirPath + File.separator + FilenameUtils.getBaseName(sourceFilePathname) + ".mp4";		
		String proxyStdErrFileLoc = proxyTargetLocation.replace(".mp4", ".mp4_ffmpeg_log");
		
		String highResMetaTargetLocation = proxyTargetLocation.replace(".mp4", ".mp4_ffprobe_out");
		String metaStdErrFileLoc = proxyTargetLocation.replace(".mp4", ".mp4_ffprobe_log");
		
		/*************** METADATA EXTRACTION ***************/
		long metaStartTime = System.currentTimeMillis();
		logger.info("Meta Generation start for " + containerName + " - targetLocation is - " + highResMetaTargetLocation);
		
		createFileInIngestServer(proxyStdErrFileLoc);
		createFileInIngestServer(metaStdErrFileLoc);
		
//			String metaDataExtractionCmd = "ffprobe -i \"" + sourceFilePathname + "\" -hide_banner -v quiet -print_format json -show_format -show_streams";
//			CommandLineExecutionResponse metaCommandLineExecutionResponse = commandLineExecuter.executeCommand(metaDataExtractionCmd, metaStdErrFileLoc);
		List<String> metaDataExtractionCommandParamsList = getMetaDataExtractionCommand(sourceFilePathname);
		CommandLineExecutionResponse metaCommandLineExecutionResponse = commandLineExecuter.executeCommand(metaDataExtractionCommandParamsList, metaStdErrFileLoc);
		
		long metaEndTime = System.currentTimeMillis();
		if(metaCommandLineExecutionResponse.isComplete()) {
			try {
				FileUtils.write(new File(highResMetaTargetLocation), metaCommandLineExecutionResponse.getStdOutResponse(), "UTF-8");
				logger.info("Meta data file for " + containerName + " created successfully in " + ((metaEndTime - metaStartTime)/1000) + " seconds - " + highResMetaTargetLocation);
			} catch (Exception e) {
				throw new Exception("Unable to save Meta data file for " + containerName + " - " + highResMetaTargetLocation + e.getMessage());
			}
		}else {
			// TODO swallow it than throwing error If meta file isnt there its still ok to insert a clip with no technical meta...???
			//logger.error("Unable to save meta data file for " + containerName + " - " + highResMetaTargetLocation + " : because : " +commandLineExecutionResponse.getFailureReason());
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
		
//			String proxyGenerationCmd = "ffmpeg -y -i \"" + sourceFilePathname + "\" -preset slow -strict -2 -f mp4 -timecode " + reversedTimeCode + " -vcodec libx264 \"-b:v\" 520000 -r 25.0 -pix_fmt yuv420p -crf 18 -vf \"[in]scale=640x360[scaled]\" -acodec aac -ar 16000 \"-b:a\" 80000 -ac 2 \"" + proxyTargetLocation + "\"";
//			CommandLineExecutionResponse proxyCommandLineExecutionResponse = commandLineExecuter.executeCommand(proxyGenerationCmd, proxyStdErrFileLoc);

		// Doing this command creation and execution in 2 steps so that the process can be referenced in memory and so if cancel command for a specific medialibrary is issued the specific process(es) can be destroyed/killed referencing this...
		// mapping only for proxy generation commands which are slightly heavy and time consuming than the thumbnail and metadata extraction...
		List<String> proxyGenerationCommandParamsList = getProxyGenCommand(sourceFilePathname, reversedTimeCode, proxyTargetLocation);
		CommandLineExecutionResponse proxyCommandLineExecutionResponse = createProcessAndExecuteCommand(fileId+"~"+taskName , proxyGenerationCommandParamsList, proxyStdErrFileLoc);
		long proxyEndTime = System.currentTimeMillis();

		// TODO : better this...
		ProxyGenCommandLineExecutionResponse proxyGenCommandLineExecutionResponse = new ProxyGenCommandLineExecutionResponse();
		proxyGenCommandLineExecutionResponse.setDestinationPathname(proxyTargetLocation);
		proxyGenCommandLineExecutionResponse.setIsComplete(proxyCommandLineExecutionResponse.isComplete());
		proxyGenCommandLineExecutionResponse.setIsCancelled(proxyCommandLineExecutionResponse.isCancelled());
		proxyGenCommandLineExecutionResponse.setStdOutResponse(proxyCommandLineExecutionResponse.getStdOutResponse());
		proxyGenCommandLineExecutionResponse.setFailureReason(proxyCommandLineExecutionResponse.getFailureReason());
		return proxyGenCommandLineExecutionResponse;
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
