package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.dao.master.cache.CacheableRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Domain;
import org.springframework.data.jpa.repository.Query;

public interface DomainDao extends CacheableRepository<Domain> {
	
	Domain findByName(String name);
	
	@Query("select dom from Domain dom where dom.default_ = true")
	Domain findByDefaultTrue();
	// this wont work Domain findByDefault_True();
}