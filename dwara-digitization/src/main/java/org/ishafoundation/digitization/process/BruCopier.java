package org.ishafoundation.digitization.process;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput;
import com.github.fracpete.rsync4j.RSync;

@Component("bru-copier")
public class BruCopier implements IProcessingTask {

	private static final Logger logger = LoggerFactory.getLogger(BruCopier.class);
		
	@Autowired
	private Configuration configuration;

	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
        //logger.info("processing bru copier: " +  inputArtifactName + ", output: " + outputArtifactName + ", destination: " + destinationDirPath);
        
		String destinationDirPath = "/Users/administrator/Desktop";
//		String destinationDirPath = processContext.getOutputDestinationDirPath();
//		String destinationDirPath = TODO - Get this from config...
        LogicalFile logicalFile = processContext.getLogicalFile();
        
        RSync rsync = new RSync()
        .source(logicalFile.getAbsolutePath())
        .destination(destinationDirPath)
        .recursive(true)
        .checksum(true);

        CollectingProcessOutput output = rsync.execute();
        logger.info(output.getStdOut());
        logger.info("Exit code: " + output.getExitCode());
        ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
        if(output.getExitCode() < 0) {
            processingtaskResponse.setIsComplete(false);
        }
        else {
            //verify checksum
            rsync.dryRun(true);
            ProcessBuilder builder = rsync.builder();
            builder.redirectErrorStream(true);
            Process process = builder.start();
            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            int numFilesTransferred = -1;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
                if(line.contains("Number of files transferred")) {
                    numFilesTransferred = Integer.parseInt(line.replace("Number of files transferred: ", ""));
                }
                else if(line.contains("Number of regular files transferred")) {
                    numFilesTransferred = Integer.parseInt(line.replace("Number of regular files transferred: ", ""));
                }
            }
            is.close();
            reader.close();

            if(numFilesTransferred == 0) {
                processingtaskResponse.setIsComplete(true);
            }
            else {
                processingtaskResponse.setIsComplete(false);
            }
        }
		return processingtaskResponse;
	}

}

