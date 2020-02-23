package org.ishafoundation.dwaraapi.db.dao.master.ingest;

import org.ishafoundation.dwaraapi.db.model.master.ingest.Property;
import org.springframework.data.repository.CrudRepository;

public interface PropertyDao extends CrudRepository<Property,Integer> {
	

}