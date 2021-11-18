package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.File;
import org.springframework.data.repository.CrudRepository;

public interface FileDao extends CrudRepository<File,Integer> {
	
	File findByPathnameChecksum(byte[] filePathnameChecksum);
	
	File findByPathname(String pathname);
	
	List<File> findAllByArtifactIdAndDeletedFalse(int artifactId);
	
	List<File> findAllByArtifactId(int artifactId);
	
	List<File> findAllByFileRefId(int fileId);
}
