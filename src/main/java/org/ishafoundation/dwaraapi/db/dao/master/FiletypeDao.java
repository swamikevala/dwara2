package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Filetype;
import org.springframework.data.repository.CrudRepository;

public interface FiletypeDao extends CrudRepository<Filetype,Integer> {
	
	Filetype findByName(String name);
	
	
	Filetype findByExtensionsExtensionName(String extnName);
}
