package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.TArtifactVolumeImportKey;
import org.ishafoundation.dwaraapi.db.model.transactional._import.jointables.TArtifactVolumeImport;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.data.repository.CrudRepository;

public interface TArtifactVolumeImportDao extends CrudRepository<TArtifactVolumeImport,TArtifactVolumeImportKey> {
	
	List<TArtifactVolumeImport>  findAllByIdVolumeId(String volumeId);
	
	List<TArtifactVolumeImport>  findAllByIdVolumeIdAndStatusIsNot(String volumeId, Status status);
	
}