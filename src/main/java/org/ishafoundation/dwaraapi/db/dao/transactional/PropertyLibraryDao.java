package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.PropertyLibrary;
import org.springframework.data.repository.CrudRepository;

public interface PropertyLibraryDao extends CrudRepository<PropertyLibrary,Integer> {
	

}