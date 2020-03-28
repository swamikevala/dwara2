package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.Property;
import org.springframework.data.repository.CrudRepository;

public interface PropertyDao extends CrudRepository<Property, Integer> {
	
}