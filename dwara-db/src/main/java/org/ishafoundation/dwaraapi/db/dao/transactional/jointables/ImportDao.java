package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.ImportKey;
import org.ishafoundation.dwaraapi.db.model.transactional._import.jointables.Import;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.data.repository.CrudRepository;

public interface ImportDao extends CrudRepository<Import,ImportKey> {
	
	List<Import>  findAllByIdVolumeId(String volumeId);
	
	List<Import>  findAllByIdVolumeIdAndIdArtifactName(String volumeId, String artifactName);
	
	List<Import>  findAllByIdVolumeIdAndIdRequeueId(String volumeId, int requeueId);
	
	List<Import>  findAllByIdVolumeIdAndStatusIsNot(String volumeId, Status status);
	
}