package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.Taskset;
import org.springframework.data.repository.CrudRepository;

public interface TasksetDao extends CrudRepository<Taskset,Integer> {
	

}