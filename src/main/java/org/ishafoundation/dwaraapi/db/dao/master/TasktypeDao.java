package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.Tasktype;
import org.springframework.data.repository.CrudRepository;

public interface TasktypeDao extends CrudRepository<Tasktype,Integer> {
	

}