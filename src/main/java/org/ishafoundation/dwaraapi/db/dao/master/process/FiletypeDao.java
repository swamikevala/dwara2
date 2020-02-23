package org.ishafoundation.dwaraapi.db.dao.master.process;

import org.ishafoundation.dwaraapi.db.model.master.process.Filetype;
import org.springframework.data.repository.CrudRepository;

public interface FiletypeDao extends CrudRepository<Filetype,Integer> {
	
	Filetype findByName(String name);
	
}