package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileVolume;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;


public interface FileVolumeRepository<T extends FileVolume> extends CrudRepository<T,Integer> {
	
	List<FileVolume> findAllByIdFileIdAndVolumeGroupRefCopyId(int fileId, int copyNumber);
	
	FileVolume findByIdFileIdAndIdVolumeId(int fileId, String volumeId);
}
