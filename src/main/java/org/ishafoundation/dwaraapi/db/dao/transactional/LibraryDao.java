package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.springframework.data.repository.CrudRepository;

public interface LibraryDao extends CrudRepository<Library,Integer> {
	// TODO _ Temp only - can have multiple libraries resultset for a single source library. For e.g., prev/mezz from the same source...
	//Library findByLibraryIdRef(int libraryIdRef);
	
	Library findByName(String libraryName);

}