package org.ishafoundation.dwaraapi.process.file;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.LogicalFile;
import org.ishafoundation.dwaraapi.process.ProcessingtaskResponse;
import org.ishafoundation.dwaraapi.process.request.Artifact;
import org.ishafoundation.dwaraapi.process.request.Artifactclass;
import org.ishafoundation.dwaraapi.process.request.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("file-delete")
public class FileDeleter implements IProcessingTask {
    
    private static final Logger logger = LoggerFactory.getLogger(FileDeleter.class);

	@Autowired
	private TFileDao tFileDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		Artifact inputArtifact = processContext.getJob().getInputArtifact();
		String inputArtifactName = inputArtifact.getName();
		Artifactclass inputArtifactclass = inputArtifact.getArtifactclass();
		String pathPrefix = inputArtifactclass.getPath();
		
		LogicalFile logicalFile = processContext.getLogicalFile();
		
		// TODO - Remove hardcoding of domain...
    	FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(Domain.ONE);

    	// deleting the logical file
    	deleteFileFromFilesystemAndUpdateDB(logicalFile, domainSpecificFileRepository, pathPrefix);

    	// deleting all its side cars
		HashMap<String, File> sidecarFileMap = logicalFile.getSidecarFiles();
		if (sidecarFileMap != null) {
			Set<String> sidecarFileSet = sidecarFileMap.keySet();
			for (String nthSideCarExtn : sidecarFileSet) {
				File nthSideCarFile = sidecarFileMap.get(nthSideCarExtn);
				
				deleteFileFromFilesystemAndUpdateDB(nthSideCarFile, domainSpecificFileRepository, pathPrefix);
			}
		}
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setIsComplete(true);

		return processingtaskResponse;
	}
	
	private void deleteFileFromFilesystemAndUpdateDB(File fileToBeDeleted, FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository, String pathPrefix){
		// flag it deleted in the DB
		String pathname = StringUtils.substringAfter(fileToBeDeleted.getAbsolutePath(), pathPrefix + File.separator);
    	org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = domainSpecificFileRepository.findByPathname(pathname);
    	fileFromDB.setDeleted(true);
    	domainSpecificFileRepository.save(fileFromDB);
    	
    	TFile tfileFromDB = tFileDao.findByPathname(pathname);
    	tfileFromDB.setDeleted(true);
    	tFileDao.save(tfileFromDB);
    	
    	// delete it in the filesystem
    	fileToBeDeleted.delete();

	}
	

}
