package org.ishafoundation.dwaraapi.process;

import java.util.HashMap;
import java.util.concurrent.Executor;

import org.ishafoundation.dwaraapi.process.request.ProcessContext;

public interface IProcessingTask {
	
	public static HashMap<String, Executor> taskName_executor_map = new HashMap<String, Executor>();

	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception;

}
