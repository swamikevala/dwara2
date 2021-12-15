package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.ArtifactVolumeKey;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.springframework.data.repository.CrudRepository;

public interface ArtifactVolumeDao extends CrudRepository<ArtifactVolume, Integer> {
// public interface ArtifactVolumeDao extends CrudRepository<ArtifactVolume, ArtifactVolumeKey> {
	
	ArtifactVolume findByIdArtifactIdAndIdVolumeId(int artifactId, String volumeId);
	
	ArtifactVolume findByIdArtifactIdAndVolumeGroupRefCopyIdAndStatus(int artifactId, int copyNumber, ArtifactVolumeStatus artifactVolumeStatus);
	
	List<ArtifactVolume> findAllByIdVolumeId(String volumeId);
	
	List<ArtifactVolume> findAllByIdVolumeIdAndStatus(String volumeId, ArtifactVolumeStatus current);
	
	List<ArtifactVolume> findAllByIdArtifactId(int artifactId);
	
	List<ArtifactVolume> findAllByIdArtifactIdAndStatus(int artifactId, ArtifactVolumeStatus current);
	
	int countByIdVolumeId(String volumeId);
}