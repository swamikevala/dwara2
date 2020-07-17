package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.springframework.data.repository.CrudRepository;

public interface ProcessingtaskDao extends CrudRepository<Processingtask,Integer> {
	
//	List<Processingtask> findAllByTasksetsTasksetId(int tasksetId);
//	
	Processingtask findById(String processingtaskName);
}