package org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.File1Volume;

import java.util.List;

public interface File1VolumeDao extends FileVolumeRepository<File1Volume>{
	List<File1Volume> findByIdFileId(int fileId);


}