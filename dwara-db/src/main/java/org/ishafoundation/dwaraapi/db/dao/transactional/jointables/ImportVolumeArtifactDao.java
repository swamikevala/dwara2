package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.ImportVolumeArtifactKey;
import org.ishafoundation.dwaraapi.db.model.transactional._import.jointables.ImportVolumeArtifact;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.data.repository.CrudRepository;

public interface ImportVolumeArtifactDao extends CrudRepository<ImportVolumeArtifact,ImportVolumeArtifactKey> {
	
	List<ImportVolumeArtifact>  findAllByIdVolumeId(String volumeId);
	
	List<ImportVolumeArtifact>  findAllByIdVolumeIdAndStatusIsNot(String volumeId, Status status);
	
}