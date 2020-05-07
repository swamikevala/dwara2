package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.Tapetype;
import org.springframework.data.repository.CrudRepository;

public interface TapetypeDao extends CrudRepository<Tapetype,Integer> {
	
	Tapetype findByCapacity(String capacity);
	
	Tapetype findByName(String name);
}