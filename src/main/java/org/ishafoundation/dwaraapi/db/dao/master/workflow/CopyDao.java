package org.ishafoundation.dwaraapi.db.dao.master.workflow;

import org.ishafoundation.dwaraapi.db.model.master.workflow.Copy;
import org.springframework.data.repository.CrudRepository;

public interface CopyDao extends CrudRepository<Copy,Integer> {
	
	Copy findByTaskId(int taskId);
}