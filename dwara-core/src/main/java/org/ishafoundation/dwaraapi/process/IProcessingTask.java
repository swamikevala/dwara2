package org.ishafoundation.dwaraapi.process;

import java.util.HashMap;
import java.util.concurrent.Executor;

import org.ishafoundation.dwaraapi.enumreferences.Domain;

public interface IProcessingTask {
	
	public static HashMap<String, Executor> taskName_executor_map = new HashMap<String, Executor>();
	/*
	 * @args[0] taskName - name of the task, the attributes that define the process (refer task table)
	 * @args[1] inputArtifactName - name of the artifact that is to be processed...
	 * @args[2] outputArtifactName - The file's id that will be processed
	 * @args[3] fileId - The file's id that will be processed
	 * @args[4] logicalFile - the source file + side car file that need to be processed. Some processes which are depenedent on parent process need to be working on the sidecar files too...
	 * @args[5] libraryCategory - is it public or private
	 * @args[6] destinationDirPath - the destination directory. if the process has an output libraryclass configured then the output library's files go in here.
	 */
	public ProcessingtaskResponse execute(String taskName, String inputArtifactName, String outputArtifactName, org.ishafoundation.dwaraapi.db.model.transactional.domain.File file, Domain domain, LogicalFile logicalFile, String category, String destinationDirPath) throws Exception;

}
