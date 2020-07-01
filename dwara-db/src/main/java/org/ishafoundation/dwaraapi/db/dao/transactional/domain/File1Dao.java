package org.ishafoundation.dwaraapi.db.dao.transactional.domain;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.domain.File1;

public interface File1Dao extends FileRepository<File1> {
	
	List<File1> findAllByArtifact1Id(int artifact1Id);
	
}