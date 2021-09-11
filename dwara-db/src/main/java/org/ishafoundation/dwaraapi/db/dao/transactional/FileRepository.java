package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.File;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;


public interface FileRepository<T extends File> extends CrudRepository<T,Integer> {
	
	public static final String FIND_ALL_BY_ARTIFACT_ID_AND_DELETED_FALSE = "findAllBy<<DOMAIN_SPECIFIC_ARTIFACT>>IdAndDeletedFalse";
	
	public static final String FIND_ALL_BY_ARTIFACT_ID = "findAllBy<<DOMAIN_SPECIFIC_ARTIFACT>>Id";
	
	File findByPathname(String pathname);

	File findById(int id);
	
	List<File> findAllByArtifactId(int ArtifactId);
	List<File> findAllByFileRefId(int fileId);
	
	//@Query("SELECT * FROM dwara_dev.file where deleted = true;")
	List<File> findAllByArtifactIdAndDeletedFalse(int ArtifactId);
}
