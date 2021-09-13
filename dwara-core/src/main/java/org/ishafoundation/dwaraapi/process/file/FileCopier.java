package org.ishafoundation.dwaraapi.process.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("file-copy-old")
public class FileCopier implements IProcessingTask {
    
    private static final Logger logger = LoggerFactory.getLogger(FileCopier.class);

	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		LogicalFile logicalFile = processContext.getLogicalFile();
		String sourceFilePathname = logicalFile.getAbsolutePath();
		Path source = Paths.get(sourceFilePathname);
		
		String destPath = processContext.getOutputDestinationDirPath();
		Path newDir = Paths.get(destPath);
		Path target = newDir.resolve(source.getFileName());
		
		String tmpDestPath = destPath + File.separator + ".copying";
		Path tmpNewDir = Paths.get(tmpDestPath);
		Path tmpTarget = tmpNewDir.resolve(source.getFileName());

		File tmpTargetFile = tmpTarget.toFile();
		if(tmpTargetFile.exists()) {
			logger.trace(tmpTarget + " file already exists. Deleting it.");
			tmpTargetFile.delete();
		}
		
		logger.trace("Now copying " + source + " to " + tmpTarget);
		Files.createDirectories(tmpNewDir);
		try {
			Files.copy(source, tmpTarget);
		}catch (Exception e) {
			throw new DwaraException("Unable to copy " + source + " to " + tmpTarget + " " + e.getMessage());
		}

		File targetFile = target.toFile();
		if(targetFile.exists()) {
			logger.trace(target + " file already exists. Deleting it.");
			targetFile.delete();
		}
		Files.move(tmpTarget, target, StandardCopyOption.ATOMIC_MOVE);
		
		Files.delete(tmpNewDir);
		
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
