package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.reference.Action;
import org.springframework.data.repository.CrudRepository;

public interface ActionDao extends CrudRepository<Action, String> {
	
	//Action findByName(String name);
	
}