package org.ishafoundation.dwaraapi.test_impl.process.thread.task.mam;

import org.ishafoundation.dwaraapi.model.LogicalFile;
import org.ishafoundation.dwaraapi.model.TaskResponse;
import org.ishafoundation.dwaraapi.process.factory.TaskFactory;
import org.ishafoundation.dwaraapi.process.thread.task.ITaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "dev | test" })
public class MamUpdateTaskExecutor implements ITaskExecutor {
    static {
    	TaskFactory.register("mam_update", MamUpdateTaskExecutor.class);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(MamUpdateTaskExecutor.class);

	
	@Override
	public TaskResponse execute(String taskName, String libraryName, int fileId, LogicalFile logicalFile, String category,
			String destinationDirPath) throws Exception {
		
		TaskResponse taskResponse = new TaskResponse();
		int catdvClipId = 666999; 
		try {
			taskResponse.setIsComplete(true); 
			taskResponse.setStdOutResponse("catdvClipId - " + catdvClipId);// TODO : where/how do we update externalrefid in db ...
			taskResponse.setAppId(catdvClipId + ""); 
		} catch (Throwable e) {
			String failureReason = "insert Clip failed - " + e.getMessage();
			taskResponse.setFailureReason(failureReason);
			taskResponse.setIsComplete(false);
			logger.error(failureReason, e);
		}
		return taskResponse;		
	}

}
