package org.ishafoundation.dwaraapi.db.dao.master.ingest;

import org.ishafoundation.dwaraapi.db.model.master.ingest.Sequence;
import org.springframework.data.repository.CrudRepository;

public interface SequenceDao extends CrudRepository<Sequence,Integer> {
	

}