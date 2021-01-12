package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileVolume;
import org.springframework.data.repository.CrudRepository;

public interface TFileVolumeDao extends CrudRepository<TFileVolume, Integer>{

	TFileVolume findByIdFileIdAndIdVolumeId(int fileId,	String volumeId);
	
	List<TFileVolume> findAllByIdVolumeId(String volumeId);

	List<TFileVolume> findAllByIdFileId(int fileId);
}