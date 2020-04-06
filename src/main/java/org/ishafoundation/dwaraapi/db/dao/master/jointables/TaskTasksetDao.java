package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;
import java.util.Optional;

import org.ishafoundation.dwaraapi.db.keys.TaskTasksetKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.TaskTaskset;
import org.springframework.data.repository.CrudRepository;

public interface TaskTasksetDao extends CrudRepository<TaskTaskset,TaskTasksetKey> {
	
	List<TaskTaskset> findAllByTasksetId(int tasksetId);
	
	List<TaskTaskset> findAllByTasksetIdAndPreTaskId(int tasksetId, int taskId);
	
	Optional<TaskTaskset> findById(TaskTasksetKey taskTasksetKey);
	
//	List<TaskTaskset> findAllByTaskTasksetKeyTasksetId(TaskTasksetKey taskTasksetKey);
//	
//	List<TaskTaskset> findAllByTaskTasksetKeyTasksetIdAndPreTaskId(TaskTasksetKey taskTasksetKey, int taskId);
	
}