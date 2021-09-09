package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;


public interface ArtifactVolumeRepository<T extends ArtifactVolume> extends CrudRepository<T,Integer> {
	
	ArtifactVolume findByIdArtifactIdAndIdVolumeId(int artifactId, String volumeId);
	
	List<ArtifactVolume> findAllByIdVolumeId(String volumeId);
	
	List<ArtifactVolume> findAllByIdVolumeIdAndStatus(String volumeId, ArtifactVolumeStatus current);
	
	List<ArtifactVolume> findAllByIdArtifactId(int artifactId);
	
	List<ArtifactVolume> findAllByIdArtifactIdAndStatus(int artifactId, ArtifactVolumeStatus current);
	
	int countByIdVolumeId(String volumeId);
}