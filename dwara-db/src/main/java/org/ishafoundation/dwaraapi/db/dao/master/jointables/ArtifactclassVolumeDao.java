package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.ArtifactclassVolumeKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassVolume;
import org.springframework.data.repository.CrudRepository;

public interface ArtifactclassVolumeDao extends CrudRepository<ArtifactclassVolume, ArtifactclassVolumeKey> {
	
	List<ArtifactclassVolume> findAllByArtifactclass(Artifactclass artifactclass);
	
}
