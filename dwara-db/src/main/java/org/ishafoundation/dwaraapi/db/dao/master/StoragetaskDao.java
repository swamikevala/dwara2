package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.reference.Storagetask;
import org.springframework.data.repository.CrudRepository;

public interface StoragetaskDao extends CrudRepository<Storagetask,Integer> {
	
//	List<Task> findAllByTasksetsTasksetId(int tasksetId);
//	
//	Task findByName(String taskName);
}