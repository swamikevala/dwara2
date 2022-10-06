package org.ishafoundation.videopub.transcoding.ffmpeg.video;

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
import org.ishafoundation.videopub.transcoding.ffmpeg.M01XmlFileHandler;
import org.ishafoundation.videopub.transcoding.ffmpeg.MediaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("video-proxy-mezz-gen")
@Primary
@Profile({ "!dev & !stage" })
public class Video_MezzanineProxy_Transcoding_TaskExecutor extends MediaTask implements IProcessingTask{
    private static final Logger logger = LoggerFactory.getLogger(Video_MezzanineProxy_Transcoding_TaskExecutor.class);
    
	@Autowired
	private M01XmlFileHandler m01xfh;	
	
	@Autowired
	private FfmpegThreadConfiguration ffmpegThreadConfiguration;
	
	private String processingtaskName = "video-proxy-mezz-gen";

	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		String taskName = processContext.getJob().getProcessingtaskId();
		LogicalFile logicalFile = processContext.getLogicalFile();
		Integer fileId = processContext.getFile().getId();
		String destinationDirPath = processContext.getOutputDestinationDirPath();
		
		if(logger.isTraceEnabled()) {
			logger.trace("taskName " + taskName);
//			logger.trace("inputArtifactName " + inputArtifactName); // V22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
//			logger.trace("outputArtifactName " + outputArtifactName); // VM22205_Test_5D-Camera_Mahabharat_Day7-Morning_Isha-Samskriti-Singing_AYA_17-Feb-12
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
	
		String fileName = FilenameUtils.getBaseName(sourceFilePathname);
		
		/*
		 * targetFileLocPathsuffix will be something like /proxies/1825_SOI-Concert_Nandi_IYC_20-Jan-11_Cam1/123-345-PROXYGENERATION/KHS10267_01.MP4
		 * we had to add the containerName(mlID-cardId-processName say e.g., something like 123-345-PROXYGENERATION) as XAVC file names across card folders are same...
		 * 
		 */
		String proxyTargetLocation = destinationDirPath + File.separator + fileName + ".mov";		
		
		
		/*************** PROXY GENERATION ***************/
		long proxyStartTime = System.currentTimeMillis();
		logger.info("Proxy Generation start for " + containerName + " - targetLocation is - " + proxyTargetLocation);
		
	//		String proxyGenerationCmd = "ffmpeg -y -i \"" + sourceFilePathname + "\" -c:v prores -profile:v 0 -c:a copy -map 0:v:0 -map 0:a? -map_metadata 0 -vf \"[in]scale=1920x1080[scaled]\" + proxyTargetLocation + "\"";
	//		CommandLineExecutionResponse proxyCommandLineExecutionResponse = commandLineExecuter.executeCommand(proxyGenerationCmd, proxyStdErrFileLoc);
	
		// Doing this command creation and execution in 2 steps so that the process can be referenced in memory and so if cancel command for a specific medialibrary is issued the specific process(es) can be destroyed/killed referencing this...
		// mapping only for proxy generation commands which are slightly heavy and time consuming than the thumbnail and metadata extraction...
		List<String> proxyGenerationCommandParamsList = getProxyGenCommand(sourceFilePathname, proxyTargetLocation);
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
	
	//"ffmpeg -y -i \"" + sourceFilePathname + "\" -c:v prores -profile:v 0 -c:a copy -map 0:v:0 -map 0:a? -map_metadata 0 -vf \"[in]scale=1920x1080[scaled]\" + proxyTargetLocation + "\"";
	
	// ffmpeg -i 2FX90013.MXF -c:v prores -profile:v 0 -c:a copy -map 0:v:0 -map 0:a? -map_metadata 0 -s 1920:1080 M_2FX90013.MOV
	// Latest command from Maa Jeevapushpa on 6th Oct
	// ffmpeg -y -i ./Original/2FX9UNCCD0256.MXF -c:v libx265 -x265-params profile=main10:pmode=1:pools=16 -b:v 12M -pix_fmt yuv420p10le -c:a aac -b:a 256k -map 0:v:0 -map 0:a? -map_metadata 0 -s 1920x1080 -vtag hvc1 ./HEVC_HD_MP4_ACC/2FX9UNCCD0256.MOV
	
	private List<String> getProxyGenCommand(String sourceFilePathname, String proxyTargetLocation) {
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
//		proxyGenerationCommandParamsList.add("-noautorotate");
//		proxyGenerationCommandParamsList.add("-nostdin");
		proxyGenerationCommandParamsList.add("-i");
		proxyGenerationCommandParamsList.add(sourceFilePathname);
		proxyGenerationCommandParamsList.add("-c:v");
		proxyGenerationCommandParamsList.add("libx265");
		proxyGenerationCommandParamsList.add("-x265-params");
		proxyGenerationCommandParamsList.add("profile=main10:pmode=1:pools=16");
		proxyGenerationCommandParamsList.add("-b:v");
		proxyGenerationCommandParamsList.add("12M");
		proxyGenerationCommandParamsList.add("-pix_fmt");
		proxyGenerationCommandParamsList.add("yuv420p10le");
		proxyGenerationCommandParamsList.add("-c:a");
		proxyGenerationCommandParamsList.add("aac");
		proxyGenerationCommandParamsList.add("-b:a");
		proxyGenerationCommandParamsList.add("256k");
		proxyGenerationCommandParamsList.add("-map");
		proxyGenerationCommandParamsList.add("0:v:0");
		proxyGenerationCommandParamsList.add("-map");
		proxyGenerationCommandParamsList.add("0:a?");
		proxyGenerationCommandParamsList.add("-map_metadata");
		proxyGenerationCommandParamsList.add("0");
		proxyGenerationCommandParamsList.add("-s");
		proxyGenerationCommandParamsList.add("1920:1080");
		proxyGenerationCommandParamsList.add("-vtag");
		proxyGenerationCommandParamsList.add("hvc1");		
		if(ffmpegThreadConfiguration.getVideoProxyMezzanine().getThreads() > 0) {
			proxyGenerationCommandParamsList.add("-threads");
			String ffmpegThreads = ffmpegThreadConfiguration.getVideoProxyMezzanine().getThreads() + "";
			proxyGenerationCommandParamsList.add(ffmpegThreads);
		}
		proxyGenerationCommandParamsList.add(proxyTargetLocation);
	
		return proxyGenerationCommandParamsList;
	}
}
