package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.JobFile;
import org.springframework.data.repository.CrudRepository;

public interface JobFileDao extends CrudRepository<JobFile,Integer> {
	
	List<JobFile> findAllByJobId(int jobId);
	
}