package org.ishafoundation.dwaraapi.process.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component("file-ignore")
public class FileIgnorer implements IProcessingTask {
    
    private static final Logger logger = LoggerFactory.getLogger(FileIgnorer.class);
	
	@Autowired
	private Configuration config;
	
	/*
	 * @Autowired private DomainUtil domainUtil;
	 */
	@Autowired
	private FileRepository fileRepository;
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		Artifact inputArtifact = processContext.getJob().getInputArtifact();
		String inputArtifactName = inputArtifact.getName();
		
		LogicalFile logicalFile = processContext.getLogicalFile();

    	//FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(Domain.ONE);
    	Optional<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileOptional = fileRepository.findById(processContext.getFile().getId());
    	if(fileOptional.isPresent()) {
	    	org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = fileOptional.get();
	    	fileFromDB.setDeleted(true);
	    	fileRepository.save(fileFromDB);
    	}
    	
    	// move the File to junk
    	String path = logicalFile.getAbsolutePath();
    	
    	String junkFilesStagedDirName = config.getJunkFilesStagedDirName();
    	String junkDirPrefixedFilePathname = inputArtifactName + File.separator + junkFilesStagedDirName; 

    	
    	String destPath = path.replace(inputArtifactName, junkDirPrefixedFilePathname);
    	Files.createDirectories(Paths.get(FilenameUtils.getFullPathNoEndSeparator(destPath)));
    	Files.move(Paths.get(path), Paths.get(destPath), StandardCopyOption.ATOMIC_MOVE);
    	
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setIsComplete(true);

		return processingtaskResponse;
	}
}
