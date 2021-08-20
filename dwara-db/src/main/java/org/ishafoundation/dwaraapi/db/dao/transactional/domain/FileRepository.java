package org.ishafoundation.dwaraapi.db.dao.transactional.domain;

import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FileRepository<T extends File> extends CrudRepository<T,Integer> {
	
	public static final String FIND_ALL_BY_ARTIFACT_ID_AND_DELETED_FALSE = "findAllBy<<DOMAIN_SPECIFIC_ARTIFACT>>IdAndDeletedFalse";
	
	public static final String FIND_ALL_BY_ARTIFACT_ID = "findAllBy<<DOMAIN_SPECIFIC_ARTIFACT>>Id";
	
	File findByPathname(String pathname);
	
	File findByFileRefId(int fileId);
}
