package org.ishafoundation.dwaraapi.db.dao.master.storage;

import org.ishafoundation.dwaraapi.db.model.master.storage.Tapeset;
import org.springframework.data.repository.CrudRepository;

public interface TapesetDao extends CrudRepository<Tapeset,Integer> {
	

}