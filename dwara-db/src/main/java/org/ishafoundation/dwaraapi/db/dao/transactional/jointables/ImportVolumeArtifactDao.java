package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import org.ishafoundation.dwaraapi.db.keys.ImportVolumeArtifactKey;
import org.ishafoundation.dwaraapi.db.model.transactional._import.jointables.ImportVolumeArtifact;
import org.springframework.data.repository.CrudRepository;

public interface ImportVolumeArtifactDao extends CrudRepository<ImportVolumeArtifact,ImportVolumeArtifactKey> {
	
}