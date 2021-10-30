package org.ishafoundation.dwaraapi.db.dao.transactional.domain;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ArtifactRepository<T extends Artifact> extends CrudRepository<T,Integer> {

	List<Artifact> findAllByWriteRequestId(int ingestRequestId);
	
	Artifact findTopByWriteRequestIdOrderByIdAsc(int ingestRequestId); // TODO use Artifactclass().isSource() instead of orderBy

	Artifact findByName(String artifactName);
	
	List<Artifact> findAllByTotalSizeAndDeletedIsFalse(long totalSize);
	
	List<Artifact> findAllByPrevSequenceCode(String prevSequenceCode); // Used in digi to accomodate PART
	
	Artifact findByPrevSequenceCodeAndDeletedIsFalse(String prevSequenceCode);
	
	Artifact findBySequenceCodeAndDeletedIsFalse(String sequenceCode); // sequenceCode is running but extracted code used as sequence code could have been deleted too - So the Deleted check
}
