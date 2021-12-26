package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional._import.jointables.Import;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.data.repository.CrudRepository;

public interface ImportDao extends CrudRepository<Import, Integer> {
	
	List<Import>  findAllByVolumeId(String volumeId);
	
	List<Import>  findAllByVolumeIdAndArtifactName(String volumeId, String artifactName);
	
	List<Import>  findAllByVolumeIdAndRequeueId(String volumeId, int requeueId);
	
	List<Import>  findAllByVolumeIdAndStatusIsNot(String volumeId, Status status);

	Import findByVolumeIdAndArtifactNameAndRequeueId(String volumeId, String artifactNameAsInCatalog, int runId);
	
}