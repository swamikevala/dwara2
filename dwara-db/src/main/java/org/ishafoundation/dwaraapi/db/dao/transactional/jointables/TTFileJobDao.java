package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.TTFileJobKey;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TTFileJob;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.data.repository.CrudRepository;

public interface TTFileJobDao extends CrudRepository<TTFileJob,TTFileJobKey> {
	
	List<TTFileJob> findAllByJobId(int jobId);
	List<TTFileJob> findAllByJobIdAndStatus(int jobId, Status status);
	List<TTFileJob> findAllByIdFileId( int fileId);
	
}