package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.springframework.data.repository.CrudRepository;

public interface TFileDao extends CrudRepository<TFile, Integer> {

	TFile findByPathname(String pathname);
	
	List<TFile> findAllByArtifactId(int id);
	
	List<TFile> findAllByArtifactIdAndDeletedIsFalse(int id);
}