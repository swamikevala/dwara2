package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional._import.Import;
import org.springframework.data.repository.CrudRepository;

public interface ImportDao extends CrudRepository<Import, Integer> {
	
	List<Import> findAllByVolumeId(String volumeId);

}