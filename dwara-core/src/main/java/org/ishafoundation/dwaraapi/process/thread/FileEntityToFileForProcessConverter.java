package org.ishafoundation.dwaraapi.process.thread;

import org.ishafoundation.dwaraapi.db.model.transactional.File;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.springframework.stereotype.Component;

@Component
public class FileEntityToFileForProcessConverter {
	
	/*
	 * @Autowired private FileEntityUtil fileEntityUtil;
	 */

	org.ishafoundation.dwaraapi.process.request.TFile getTFileForProcess(org.ishafoundation.dwaraapi.db.model.transactional.TFile tfileEntity) throws Exception{
		if(tfileEntity == null)
			return null;
		
		org.ishafoundation.dwaraapi.process.request.TFile fileForProcess = new org.ishafoundation.dwaraapi.process.request.TFile();
		
		fileForProcess.setId(tfileEntity.getId());
		
		fileForProcess.setPathname(tfileEntity.getPathname());

		fileForProcess.setChecksum(tfileEntity.getChecksum());

		fileForProcess.setSize(tfileEntity.getSize());

		fileForProcess.setDeleted(tfileEntity.isDeleted());
				
		return fileForProcess;
	}

	
	org.ishafoundation.dwaraapi.process.request.File getFileForProcess(org.ishafoundation.dwaraapi.db.model.transactional.File fileEntity) throws Exception{
		if(fileEntity == null)
			return null;
		
		org.ishafoundation.dwaraapi.process.request.File fileForProcess = new org.ishafoundation.dwaraapi.process.request.File();
		
		fileForProcess.setId(fileEntity.getId());
		
		fileForProcess.setPathname(fileEntity.getPathname());

		fileForProcess.setChecksum(fileEntity.getChecksum());

		fileForProcess.setSize(fileEntity.getSize());

		fileForProcess.setDeleted(fileEntity.isDeleted());
		
		fileForProcess.setFileRef(getFileForProcess((org.ishafoundation.dwaraapi.db.model.transactional.File)fileEntity.getFileRef()));
		
		return fileForProcess;
	}
}

