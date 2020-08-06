package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import org.ishafoundation.dwaraapi.db.keys.ArtifactclassDestinationKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassDestination;
import org.springframework.data.repository.CrudRepository;

public interface ArtifactclassDestinationDao extends CrudRepository<ArtifactclassDestination,ArtifactclassDestinationKey> {
	
	
}
