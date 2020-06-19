package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.dao.master.cache.CacheableRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Domain;

public interface DomainDao extends CacheableRepository<Domain> {
	
	Domain findByName(String name);
	
	Domain findByDefaulttTrue();
	
}