package org.ishafoundation.dwaraapi.db.dao.master.ingest;

import org.ishafoundation.dwaraapi.db.model.master.ingest.RequesttypeLibraryclass;
import org.springframework.data.repository.CrudRepository;

public interface RequesttypeLibraryclassDao extends CrudRepository<RequesttypeLibraryclass,Integer> {
	
	RequesttypeLibraryclass findByRequesttypeIdAndLibraryclassId(int requesttypeId, int libraryclassId);

}