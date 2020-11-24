package org.ishafoundation.digitization.mxf;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("MxfExcluder")
public class MxfExcluder implements IProcessingTask {
    
    private static final Logger logger = LoggerFactory.getLogger(MxfExcluder.class);
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private Configuration config;
	
	@Override
	public ProcessingtaskResponse execute(String taskName, String inputArtifactClass, String inputArtifactName, String outputArtifactName,
			org.ishafoundation.dwaraapi.db.model.transactional.domain.File file, Domain domain, LogicalFile logicalFile, String category, String destinationDirPath) throws Exception {
		
		// Mark the file deleted...
    	FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
    	file.setDeleted(true);
    	domainSpecificFileRepository.save(file);
		
    	// move the file to junk
    	String filePathname = file.getPathname();
    	String junkFilesStagedDirName = config.getJunkFilesStagedDirName();
    	String junkDirPrefixedFilePathname = junkFilesStagedDirName + File.separator + filePathname; 
    	String path = logicalFile.getAbsolutePath();
    	String destPath = path.replace(filePathname, junkDirPrefixedFilePathname);
    	Files.move(Paths.get(path), Paths.get(destPath), StandardCopyOption.ATOMIC_MOVE);
    	
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setIsComplete(true);

		return processingtaskResponse;
	}
}
