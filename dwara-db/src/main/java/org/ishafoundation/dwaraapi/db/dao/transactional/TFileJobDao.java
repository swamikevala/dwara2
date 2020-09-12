package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.TFileJobKey;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;
import org.springframework.data.repository.CrudRepository;

public interface TFileJobDao extends CrudRepository<TFileJob,TFileJobKey> {
	
	List<TFileJob> findAllByJobId(int jobId);

}