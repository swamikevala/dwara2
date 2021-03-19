package org.ishafoundation.dwaraapi.db.dao.transactional.domain;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ArtifactRepository<T extends Artifact> extends CrudRepository<T,Integer> {

	List<Artifact> findAllByWriteRequestId(int ingestRequestId);
	
	Artifact findTopByWriteRequestIdOrderByIdAsc(int ingestRequestId);

	Artifact findByName(String artifactName);
	
	List<Artifact> findAllByTotalSize(long totalSize);
	
	List<Artifact> findAllByPrevSequenceCode(String prevSequenceCode);
}
