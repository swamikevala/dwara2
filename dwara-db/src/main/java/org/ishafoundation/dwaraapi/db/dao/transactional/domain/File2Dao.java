package org.ishafoundation.dwaraapi.db.dao.transactional.domain;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.domain.File2;

public interface File2Dao extends FileRepository<File2> {
	
	List<File2> findAllByArtifact2Id(int artifact2Id);

}