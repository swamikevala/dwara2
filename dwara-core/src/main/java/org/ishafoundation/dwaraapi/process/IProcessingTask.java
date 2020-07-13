package org.ishafoundation.dwaraapi.process;

public interface IProcessingTask {
	
	public String execute();
	
//	public ProcessingTaskResponse execute(String taskName, String libraryName, int fileId, LogicalFile logicalFile, String category, String destinationDirPath) throws Exception;

}
