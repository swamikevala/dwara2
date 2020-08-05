package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.cache.CacheableRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Destinationpath;

public interface DestinationpathDao extends CacheableRepository<Destinationpath> {
	
	List<Destinationpath> findAllByArtifactclassDestinationpathArtifactclassId(int artifactclassId);

	Destinationpath findByName(String destinationpathName);
}