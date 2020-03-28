package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.Task;
import org.springframework.data.repository.CrudRepository;

public interface TaskDao extends CrudRepository<Task,Integer> {
	
	List<Task> findAllByTasksetsTasksetId(int tasksetId);
}