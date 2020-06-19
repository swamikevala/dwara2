package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.cache.CacheableRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;

public interface ArtifactclassDao extends CacheableRepository<Artifactclass> {
	
	Artifactclass findByName(String name);
	
	//List<Artifactclass> findAllByArtifactclassTargetvolumeTargetvolumeId(int targetvolumeId);
}