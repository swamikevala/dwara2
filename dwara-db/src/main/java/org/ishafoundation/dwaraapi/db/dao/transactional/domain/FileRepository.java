package org.ishafoundation.dwaraapi.db.dao.transactional.domain;

import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FileRepository<T extends File> extends CrudRepository<T,Integer> {
	
	public static final String FIND_ALL_BY_ARTIFACT_ID = "findAllBy<<DOMAIN_SPECIFIC_ARTIFACT>>Id";

//	List<File> findAllByArtifactId(int artifactId);
	
	// Get all files for the library that are not derived files...
	// Commenting out as libraryid and file_ids go hand in hand. if a derived file has a fileidref then it also has its own derived library id linked - so not needed List<File1> findAllByLibraryIdAndFileIdRef(int libraryId, int fileIdRef);
	
	
	// Almost all files except directory will have filetypeId - so whats the point....
	//List<File> findAllByLibraryIdAndFileIdRefAndFiletypeIdGreaterThan(int libraryId, int fileIdRef, int filetypeId);

}
