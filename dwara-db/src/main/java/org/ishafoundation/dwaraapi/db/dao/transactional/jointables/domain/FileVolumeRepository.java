package org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FileVolumeRepository<T extends FileVolume> extends CrudRepository<T,Integer> {
	
	//	SELECT * FROM dwara_v4_test.file1_volume join dwara_v4_test.volume on dwara_v4_test.volume.id = dwara_v4_test.file1_volume.volume_id where dwara_v4_test.volume.location_id = 3 and dwara_v4_test.file1_volume.file_id = 60;
	//	SELECT * FROM dwara_v4_test.volume join dwara_v4_test.file1_volume on id = dwara_v4_test.file1_volume.volume_id where  dwara_v4_test.volume.location_id = 3 and  dwara_v4_test.file1_volume. file_id = 60;
	FileVolume findByIdFileIdAndVolumeLocationId(int fileId, int locationId);
	// TODO - Which one is more performant above or below
	//File1Volume findByIdVolumeLocationIdAndFileId()

	// FileVolume findByFileIdAndCopyNumber(int fileIdToBeRestored, int copyNumber);
	
	// TODO _ this might give multiple values - need copynumber to narrow it down
	//FileVolume findByFileId(int fileIdToBeRestored);
	
//	FileVolume findByFileIdAndVolumeId(int fileIdToBeRestored, int volumeId);
//	
//	List<FileVolume> findAllByFileId(int fileIdToBeRestored);
//	
//	List<FileVolume> findAllByVolumeId(int volumeId);
}
