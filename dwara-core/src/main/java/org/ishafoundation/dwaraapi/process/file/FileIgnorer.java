package org.ishafoundation.dwaraapi.process.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.Artifact;
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
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		Artifact inputArtifact = processContext.getJob().getInputArtifact();
		String inputArtifactName = inputArtifact.getName();
		
		String taskName = processContext.getJob().getProcessingtaskId();
		LogicalFile logicalFile = processContext.getLogicalFile();
		org.ishafoundation.dwaraapi.process.request.File file = processContext.getFile();
		String destinationDirPath = processContext.getOutputDestinationDirPath();

		// TODO - Have to call this as API
    	FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(Domain.ONE);
    	org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = domainSpecificFileRepository.findById(file.getId()).get();
    	fileFromDB.setDeleted(true);
    	domainSpecificFileRepository.save(fileFromDB);
		
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
