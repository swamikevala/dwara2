package org.ishafoundation.dwaraapi.process.thread;

import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileEntityUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.process.request.File;
import org.ishafoundation.dwaraapi.process.request.TFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileEntityToFileForProcessConverter {
	
	@Autowired
	private FileEntityUtil fileEntityUtil;

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

	
	File getFileForProcess(org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileEntity, Domain domain) throws Exception{
		if(fileEntity == null)
			return null;
		
		File fileForProcess = new File();
		
		fileForProcess.setId(fileEntity.getId());
		
		fileForProcess.setPathname(fileEntity.getPathname());

		fileForProcess.setChecksum(fileEntity.getChecksum());

		fileForProcess.setSize(fileEntity.getSize());

		fileForProcess.setDeleted(fileEntity.isDeleted());
		
		fileForProcess.setFileRef(getFileForProcess(fileEntityUtil.getFileRef(fileEntity, domain), domain));
		
		return fileForProcess;
	}
}

