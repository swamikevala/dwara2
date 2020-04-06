package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;
import org.springframework.data.repository.CrudRepository;

public interface TFileJobDao extends CrudRepository<TFileJob,Integer> {
	
	List<TFileJob> findAllByJobId(int jobId);

}