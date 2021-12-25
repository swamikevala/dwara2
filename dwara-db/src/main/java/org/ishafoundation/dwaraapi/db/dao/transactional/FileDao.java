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
	
	List<File> findAllByFileRefIdAndPathnameEndsWith(int file1Id , String extension);

	List<File> findAllByArtifactIdAndPathnameEndsWith(int id , String extensions);
	
	List<File> findByPathnameIn(List<String> pathnameList);
	
	List<File> findByPathnameContains(String keyword);
	
	List<File> findAllByArtifactIdAndDeletedFalseAndDiffIsNull(int artifactId);
}
