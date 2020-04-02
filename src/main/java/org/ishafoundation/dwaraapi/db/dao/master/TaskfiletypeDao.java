package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.Taskfiletype;
import org.springframework.data.repository.CrudRepository;

public interface TaskfiletypeDao extends CrudRepository<Taskfiletype,Integer> {
	
	Taskfiletype findByName(String name);
	
	
	Taskfiletype findByExtensionsExtensionName(String extnName);
}
