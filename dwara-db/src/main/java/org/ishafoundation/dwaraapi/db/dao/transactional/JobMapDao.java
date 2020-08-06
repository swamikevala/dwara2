package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.JobMapKey;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.JobMap;
import org.springframework.data.repository.CrudRepository;

public interface JobMapDao extends CrudRepository<JobMap,JobMapKey> {
	
	// gets all dependent Jobs for the job 
	List<JobMap> findAllByIdJobRefId(int jobId);
	
	// gets all prerequisite jobs for the job
	List<JobMap> findAllByIdJobId(int jobId);

}