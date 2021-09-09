package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;
import java.util.Optional;

import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ArtifactRepository<T extends Artifact> extends CrudRepository<T,Integer> {

	List<Artifact> findAllByWriteRequestId(int ingestRequestId);
	
	Artifact findTopByWriteRequestIdOrderByIdAsc(int ingestRequestId); // TODO use Artifactclass().isSource() instead of orderBy

	Artifact findByName(String artifactName);
	
	List<Artifact> findAllByTotalSizeAndDeletedIsFalse(long totalSize);
	
	List<Artifact> findAllByPrevSequenceCode(String prevSequenceCode);
	
	Artifact findById(int  id);
}
