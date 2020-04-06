package org.ishafoundation.dwaraapi.process.thread.task;

import org.ishafoundation.dwaraapi.model.LogicalFile;
import org.ishafoundation.dwaraapi.model.TaskResponse;

public interface ITaskExecutor {

	/*
	 * @args[0] taskName - name of the task, the attributes that define the process (refer task table)
	 * @args[1] inputLibraryName - name of the library that is to be processed...
	 * @args[2] fileId - The file's id that will be processed
	 * @args[3] logicalFile - the source file + side car file that need to be processed. Some processes which are depenedent on parent process need to be working on the sidecar files too...
	 * @args[4] libraryCategory - is it public or private
	 * @args[5] destinationDirPath - the destination directory. if the process has an output libraryclass configured then the output library's files go in here.
	 */
	public TaskResponse execute(String taskName, String libraryName, int fileId, LogicalFile logicalFile, String category, String destinationDirPath) throws Exception;
	
}
