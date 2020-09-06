package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.ProcessingFailureKey;
import org.ishafoundation.dwaraapi.db.model.transactional.ProcessingFailure;
import org.springframework.data.repository.CrudRepository;

public interface ProcessingFailureDao extends CrudRepository<ProcessingFailure,ProcessingFailureKey> {
	
	List<ProcessingFailure> findAllByJobId(int jobId);

}