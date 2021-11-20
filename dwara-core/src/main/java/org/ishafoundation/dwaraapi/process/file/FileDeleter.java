package org.ishafoundation.dwaraapi.process.file;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
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
	private FileDao fileDao;
	
	@Override
	public ProcessingtaskResponse execute(ProcessContext processContext) throws Exception {
		
		Artifact inputArtifact = processContext.getJob().getInputArtifact();
		String inputArtifactName = inputArtifact.getName();
		Artifactclass inputArtifactclass = inputArtifact.getArtifactclass();
		String pathPrefix = inputArtifactclass.getPath();
		
		LogicalFile logicalFile = processContext.getLogicalFile();
    	// deleting the logical file
    	deleteFileFromFilesystemAndUpdateDB(logicalFile, pathPrefix);

    	// deleting all its side cars
		HashMap<String, File> sidecarFileMap = logicalFile.getSidecarFiles();
		if (sidecarFileMap != null) {
			Set<String> sidecarFileSet = sidecarFileMap.keySet();
			for (String nthSideCarExtn : sidecarFileSet) {
				File nthSideCarFile = sidecarFileMap.get(nthSideCarExtn);
				
				deleteFileFromFilesystemAndUpdateDB(nthSideCarFile, pathPrefix);
			}
		}
		
		File parentFile = logicalFile.getParentFile();
		if(parentFile != null && parentFile.isDirectory() && parentFile.list().length == 0)
			parentFile.delete();
		
		ProcessingtaskResponse processingtaskResponse = new ProcessingtaskResponse();
		processingtaskResponse.setIsComplete(true);

		return processingtaskResponse;
	}
	
	private void deleteFileFromFilesystemAndUpdateDB(File fileToBeDeleted, String pathPrefix){
		// flag it deleted in the DB
		String pathname = StringUtils.substringAfter(fileToBeDeleted.getAbsolutePath(), pathPrefix + File.separator);
    	org.ishafoundation.dwaraapi.db.model.transactional.File fileFromDB = fileDao.findByPathname(pathname);
    	fileFromDB.setDeleted(true);
    	fileDao.save(fileFromDB);
    	
    	TFile tfileFromDB = tFileDao.findByPathname(pathname);
    	tfileFromDB.setDeleted(true);
    	tFileDao.save(tfileFromDB);
    	
    	// delete it in the filesystem
    	fileToBeDeleted.delete();

	}
	

}
