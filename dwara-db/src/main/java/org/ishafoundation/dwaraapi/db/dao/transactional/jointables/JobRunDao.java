package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import org.ishafoundation.dwaraapi.db.keys.JobRunKey;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.JobRun;
import org.springframework.data.repository.CrudRepository;

public interface JobRunDao extends CrudRepository<JobRun,JobRunKey> {
	
	long countByJobId(int jobId);
	
}