package org.ishafoundation.dwaraapi.process.thread;

import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.springframework.stereotype.Component;

@Component
public class FileEntityToFileForProcessConverter {
	
	/*
	 * @Autowired private FileEntityUtil fileEntityUtil;
	 */

	TFile getTFileForProcess(org.ishafoundation.dwaraapi.db.model.transactional.TFile tfileEntity) throws Exception{
		if(tfileEntity == null)
			return null;
		
		TFile fileForProcess = new TFile();
		
		fileForProcess.setId(tfileEntity.getId());
		
		fileForProcess.setPathname(tfileEntity.getPathname());

		fileForProcess.setChecksum(tfileEntity.getChecksum());

		fileForProcess.setSize(tfileEntity.getSize());

		fileForProcess.setDeleted(tfileEntity.isDeleted());
				
		return fileForProcess;
	}

	
	File getFileForProcess(org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileEntity) throws Exception{
		if(fileEntity == null)
			return null;
		
		File fileForProcess = new File();
		
		fileForProcess.setId(fileEntity.getId());
		
		fileForProcess.setPathname(fileEntity.getPathname());

		fileForProcess.setChecksum(fileEntity.getChecksum());

		fileForProcess.setSize(fileEntity.getSize());

		fileForProcess.setDeleted(fileEntity.isDeleted());
		
		fileForProcess.setFileRef(getFileForProcess((org.ishafoundation.dwaraapi.db.model.transactional.domain.File)fileEntity.getFileRef()));
		
		return fileForProcess;
	}
}

