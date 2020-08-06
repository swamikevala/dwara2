package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.cache.CacheableRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Destination;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.springframework.data.repository.CrudRepository;

public interface DestinationDao extends CrudRepository<Destination,String> {
	
	List<Destination> findAllByArtifactclassDestinationArtifactclassId(int artifactclassId);

	Destination findByName(String destinationpathName);
}