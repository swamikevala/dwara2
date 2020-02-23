package org.ishafoundation.dwaraapi.db.dao.master.workflow;

import org.ishafoundation.dwaraapi.db.model.master.workflow.Task;
import org.springframework.data.repository.CrudRepository;

public interface TaskDao extends CrudRepository<Task,Integer> {
	

}