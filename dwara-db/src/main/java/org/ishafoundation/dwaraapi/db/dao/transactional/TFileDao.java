package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.springframework.data.repository.CrudRepository;

public interface TFileDao extends CrudRepository<TFile, Integer> {

	List<TFile> findAllByArtifactId(int id);
}