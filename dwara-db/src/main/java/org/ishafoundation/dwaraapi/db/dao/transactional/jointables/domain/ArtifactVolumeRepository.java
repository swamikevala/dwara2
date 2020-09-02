package org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ArtifactVolumeRepository<T extends ArtifactVolume> extends CrudRepository<T,Integer> {
	
	ArtifactVolume findByIdArtifactIdAndIdVolumeId(int artifactId, String volumeId);
	
	List<ArtifactVolume> findAllByIdVolumeId(String volumeId);
	
	int countByIdVolumeId(String volumeId);
}