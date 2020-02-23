package org.ishafoundation.dwaraapi.process.thread.task.transcoding.ffmpeg;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MediaTask {
	
	Logger logger = LoggerFactory.getLogger(MediaTask.class);
	
	// Collection of running processes are hold here so if cancel command for a specific medialibrary is issued the process can be destroyed/killed...
	public static Map<CommandLineProcessMapKeyBean, CommandLineProcessMapValueBean> RUNNING_MEDIAPROCESSING_PROCESSES_MAP = Collections.synchronizedMap(new HashMap<CommandLineProcessMapKeyBean, CommandLineProcessMapValueBean>());
	
	@Autowired
	protected CommandLineExecuter commandLineExecuter;	
	
	
	protected CommandLineExecutionResponse createProcessAndExecuteCommand(String mediaFileId, List<String> commandParamsList, String stdErrFileLoc){
		Process proc = commandLineExecuter.createProcess(commandParamsList);
		CommandLineProcessMapKeyBean commandLineProcessMapKeyBean = new CommandLineProcessMapKeyBean();
		// TODO : commandLineProcessMapKeyBean.setLibraryId(job.getLibraryId());
		commandLineProcessMapKeyBean.setMediaFileId(mediaFileId);
		
		CommandLineProcessMapValueBean commandLineProcessMapValueBean = new CommandLineProcessMapValueBean();
		commandLineProcessMapValueBean.setProcess(proc);
		
		RUNNING_MEDIAPROCESSING_PROCESSES_MAP.put(commandLineProcessMapKeyBean, commandLineProcessMapValueBean);
		CommandLineExecutionResponse commandLineExecutionResponse = commandLineExecuter.executeCommand(commandParamsList, proc, stdErrFileLoc);
		commandLineExecutionResponse.setIsCancelled(commandLineProcessMapValueBean.isKillProcessInitiated());
		RUNNING_MEDIAPROCESSING_PROCESSES_MAP.remove(commandLineProcessMapKeyBean);
		
		return commandLineExecutionResponse;
	}
	
	protected void createFileInIngestServer(String fileLoc) throws Exception {
		// TODO : ***!!!~~~ Ideally this should be a SCP to the ingest server... but since dwara app and ingest server are going to be in same instance - creating files locally and not remotely
		FileUtils.write(new File(fileLoc), "");
	}

}
