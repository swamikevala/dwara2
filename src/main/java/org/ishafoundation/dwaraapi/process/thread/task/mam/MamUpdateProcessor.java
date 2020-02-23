package org.ishafoundation.dwaraapi.process.thread.task.mam;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.ishafoundation.dwaraapi.model.LogicalFile;
import org.ishafoundation.dwaraapi.model.ProxyGenCommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.process.factory.ProcessFactory;
import org.ishafoundation.dwaraapi.process.thread.task.IProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MamUpdateProcessor implements IProcessor {
    static {
    	ProcessFactory.register("MAM_UPDATE", MamUpdateProcessor.class);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(MamUpdateProcessor.class);

	@Override
	public ProxyGenCommandLineExecutionResponse process(String taskName, int fileId, LogicalFile logicalFile,
			String destinationFilePath) throws Exception {
		HashMap<String, File> logicalFileMap = logicalFile.getSidecarFiles();
		System.out.println("standalone - " + logicalFile.getSidecarFile("jpg"));
		
		
		for (Iterator<String> iterator = logicalFileMap.keySet().iterator(); iterator.hasNext();) {
			String extnAsKey = (String) iterator.next();
			System.out.println("processing the clip here using sidecar file - " +logicalFileMap.get(extnAsKey));
		}
		logger.trace("MAM completed");
		return null;
	}
		
}
