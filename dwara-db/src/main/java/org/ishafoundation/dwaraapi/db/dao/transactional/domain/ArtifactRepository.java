package org.ishafoundation.dwaraapi.db.dao.transactional.domain;

import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ArtifactRepository<T extends Artifact> extends CrudRepository<T,Integer> {

	// TODO _ Temp only - can have multiple libraries resultset for a single source artifact. For e.g., prev/mezz from the same source...
	//Artifact findByArtifactIdRef(int artifactIdRef);

	Artifact findByName(String artifactName);
}
