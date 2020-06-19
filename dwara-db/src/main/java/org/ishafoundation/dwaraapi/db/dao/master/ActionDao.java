package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.dao.master.cache.CacheableRepository;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;

public interface ActionDao extends CacheableRepository<Action> {
	
	Action findByName(String name);
	
}