package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.TaskTasksetKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.TaskTaskset;
import org.springframework.data.repository.CrudRepository;

public interface TaskTasksetDao extends CrudRepository<TaskTaskset,TaskTasksetKey> {
	
	List<TaskTaskset> findAllByTasksetId(int tasksetId);
	
}