package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.dao.master.cache.CacheableRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;

public interface LocationDao extends CacheableRepository<Location> {

//	@Query("select loc from Location loc where loc.default_ = true")
//	Location findByDefaultTrue();
}