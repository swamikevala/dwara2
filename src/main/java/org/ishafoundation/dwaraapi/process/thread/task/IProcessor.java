package org.ishafoundation.dwaraapi.process.thread.task;

import org.ishafoundation.dwaraapi.model.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.model.LogicalFile;

public interface IProcessor {

	/*
	 * @args 
	 */
	public CommandLineExecutionResponse process(String taskName, String libraryName, int fileId, LogicalFile logicalFile, String category, String destinationFilePath) throws Exception;
	
}
