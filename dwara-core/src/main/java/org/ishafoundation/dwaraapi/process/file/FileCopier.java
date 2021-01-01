package org.ishafoundation.dwaraapi.process.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("file-copy")
public class FileCopier implements IProcessingTask {
    
    private static final Logger logger = LoggerFactory.getLogger(FileCopier.class);

	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		LogicalFile logicalFile = processContext.getLogicalFile();
		String sourceFilePathname = logicalFile.getAbsolutePath();
		Path source = Paths.get(sourceFilePathname);
		
		String destPath = processContext.getOutputDestinationDirPath();
		Path newDir = Paths.get(destPath);
		Files.createDirectories(newDir);

		Path target = newDir.resolve(source.getFileName());
		File targetFile = target.toFile();
		if(target.toFile().exists()) {
			logger.trace(target + " File already exists. Deleting it.");
			targetFile.delete();
		}
		logger.trace("Now copying " + source + " to " + target);
		try {
			Files.copy(source, target);
		}catch (Exception e) {
			throw new DwaraException("Unable to copy " + source + " to " + target + " " + e.getMessage());
		}
		
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setIsComplete(true);

		return processingtaskResponse;
	}
	
	public static void main(String[] args) throws IOException {
		String sourceFilePathname = "C:\\Users\\prakash\\personal\\CV_For_KrishnaAnna_WithEmployers";
		Path source = Paths.get(sourceFilePathname);
		
		String destPath = "C:\\Users\\prakash\\personal\\delete\\this";
		Files.createDirectories(Paths.get(destPath));
		
		Path newDir = Paths.get(destPath);
		Path target = newDir.resolve(source.getFileName());
		File targetFile = target.toFile();
		if(target.toFile().exists()) {
			logger.trace(target + " File already exists. Deleting it.");
			targetFile.delete();
		}
		logger.trace("Now copying " + source + " to " + target);
    	Files.copy(source, target);
	}
}
