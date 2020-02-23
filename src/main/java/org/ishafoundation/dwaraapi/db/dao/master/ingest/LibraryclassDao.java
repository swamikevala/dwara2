package org.ishafoundation.dwaraapi.db.dao.master.ingest;

import org.ishafoundation.dwaraapi.db.model.master.ingest.Libraryclass;
import org.springframework.data.repository.CrudRepository;

public interface LibraryclassDao extends CrudRepository<Libraryclass,Integer> {
	
	Libraryclass findByTaskId(int taskId);

}