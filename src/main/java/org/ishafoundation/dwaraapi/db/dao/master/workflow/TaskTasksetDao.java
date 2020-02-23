package org.ishafoundation.dwaraapi.db.dao.master.workflow;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.workflow.TaskTaskset;
import org.springframework.data.repository.CrudRepository;

public interface TaskTasksetDao extends CrudRepository<TaskTaskset,Integer> {
	
	TaskTaskset findByTasksetIdAndTaskId(int tasksetId, int taskId);
	
	List<TaskTaskset> findAllByTasksetIdAndPreTaskId(int taskId, int tasksetId);
	
	List<TaskTaskset> findAllByTasksetId(int tasksetId);
}