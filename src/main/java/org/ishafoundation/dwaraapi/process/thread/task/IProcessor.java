package org.ishafoundation.dwaraapi.process.thread.task;

import org.ishafoundation.dwaraapi.model.LogicalFile;
import org.ishafoundation.dwaraapi.model.ProxyGenCommandLineExecutionResponse;

public interface IProcessor {

	/*
	 * @args 
	 */
	public ProxyGenCommandLineExecutionResponse process(String taskName, int fileId, LogicalFile logicalFile, String destinationFilePath) throws Exception;
	
}
