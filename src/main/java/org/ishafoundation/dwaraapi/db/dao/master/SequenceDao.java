package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.Sequence;
import org.springframework.data.repository.CrudRepository;

public interface SequenceDao extends CrudRepository<Sequence,Integer> {
	

}