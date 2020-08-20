package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.dao.master.cache.CacheableRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Destination;

public interface DestinationDao extends CacheableRepository<Destination>{//CrudRepository<Destination,String> {
	
	Destination findByPath(String path);
	
}