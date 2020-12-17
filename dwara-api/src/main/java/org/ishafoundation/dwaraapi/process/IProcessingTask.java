package org.ishafoundation.dwaraapi.process;

import org.ishafoundation.dwaraapi.process.request.ProcessContext;

public interface IProcessingTask {

	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception;

}
