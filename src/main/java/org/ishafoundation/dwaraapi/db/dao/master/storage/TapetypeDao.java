package org.ishafoundation.dwaraapi.db.dao.master.storage;

import org.ishafoundation.dwaraapi.db.model.master.storage.Tapetype;
import org.springframework.data.repository.CrudRepository;

public interface TapetypeDao extends CrudRepository<Tapetype,Integer> {
	

}