package org.ishafoundation.dwaraapi.db.dao.master.workflow;

import org.ishafoundation.dwaraapi.db.model.master.workflow.Process;
import org.springframework.data.repository.CrudRepository;

public interface ProcessDao extends CrudRepository<Process,Integer> {
	

}