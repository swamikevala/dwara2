package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.reference.Requesttype;
import org.springframework.data.repository.CrudRepository;

public interface RequesttypeDao extends CrudRepository<Requesttype,Integer> {
	
	Requesttype findByName(String name);
	
}