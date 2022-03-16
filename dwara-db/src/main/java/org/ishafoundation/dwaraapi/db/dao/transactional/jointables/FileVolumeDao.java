package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileVolume;
import org.springframework.data.repository.CrudRepository;

// public interface FileVolumeDao extends CrudRepository<FileVolume, FileVolumeKey> {
public interface FileVolumeDao extends CrudRepository<FileVolume, Integer> {
	
	List<FileVolume> findAllByIdFileIdAndVolumeGroupRefCopyId(int fileId, int copyNumber);
	
	FileVolume findByIdFileIdAndIdVolumeId(int fileId, String volumeId);

    List<FileVolume> findAllByIdVolumeIdOrderByVolumeStartBlockAsc(String volumeId);
    
    List<FileVolume> findAllByIdVolumeId(String volumeId);
    
    List<FileVolume> findAllByIdFileId(int fileId);
}
