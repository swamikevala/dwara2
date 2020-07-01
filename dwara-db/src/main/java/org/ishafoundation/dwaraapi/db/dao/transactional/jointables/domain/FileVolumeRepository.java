package org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FileVolumeRepository<T extends FileVolume> extends CrudRepository<T,Integer> {

	// FileVolume findByFileIdAndCopyNumber(int fileIdToBeRestored, int copyNumber);
	
	// TODO _ this might give multiple values - need copynumber to narrow it down
	//FileVolume findByFileId(int fileIdToBeRestored);
	
//	FileVolume findByFileIdAndVolumeId(int fileIdToBeRestored, int volumeId);
//	
//	List<FileVolume> findAllByFileId(int fileIdToBeRestored);
//	
//	List<FileVolume> findAllByVolumeId(int volumeId);
}
