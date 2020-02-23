package org.ishafoundation.dwaraapi.db.dao.master.workflow;

import org.ishafoundation.dwaraapi.db.model.master.workflow.Taskset;
import org.springframework.data.repository.CrudRepository;

public interface TasksetDao extends CrudRepository<Taskset,Integer> {
	

}