package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.Tapeset;
import org.springframework.data.repository.CrudRepository;

public interface TapesetDao extends CrudRepository<Tapeset,Integer> {
	

}