package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.File;
import org.springframework.data.repository.CrudRepository;

public interface FileDao extends CrudRepository<File,Integer> {
	
//	List<File> findAllByLibraryId(int libraryId);
	
	// Get all files for the library that are not derived files...
	// Commenting out as libraryid and file_ids go hand in hand. if a derived file has a fileidref then it also has its own derived library id linked - so not needed List<File> findAllByLibraryIdAndFileIdRef(int libraryId, int fileIdRef);
	
	
	// Almost all files except directory will have filetypeId - so whats the point....
	//List<File> findAllByLibraryIdAndFileIdRefAndFiletypeIdGreaterThan(int libraryId, int fileIdRef, int filetypeId);
	

}