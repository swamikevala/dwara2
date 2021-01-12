package org.ishafoundation.videopub.mam;

import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

//@Component("video-mam-update")
//@Profile({ "dev | stage" })
public class MockMamUpdateTaskExecutor implements IProcessingTask {
    
    private static final Logger logger = LoggerFactory.getLogger(MockMamUpdateTaskExecutor.class);

	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();

		int catdvClipId = 666999; 
		try {
			processingtaskResponse.setIsComplete(true); 
			processingtaskResponse.setStdOutResponse("catdvClipId - " + catdvClipId);// TODO : where/how do we update externalrefid in db ...
			processingtaskResponse.setAppId(catdvClipId + ""); 
		} catch (Throwable e) {
			String failureReason = "insert Clip failed - " + e.getMessage();
			processingtaskResponse.setFailureReason(failureReason);
			processingtaskResponse.setIsComplete(false);
			logger.error(failureReason, e);
		}
		return processingtaskResponse;		
	}
}
