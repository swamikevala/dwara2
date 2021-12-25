package org.ishafoundation.dwaraapi.db.dao.transactional._import.jointables;

import org.ishafoundation.dwaraapi.db.keys.FileVolumeKey;
import org.ishafoundation.dwaraapi.db.model.transactional._import.jointables.FileVolumeDiff;
import org.springframework.data.repository.CrudRepository;

public interface FileVolumeDiffDao extends CrudRepository<FileVolumeDiff,FileVolumeKey> {

}
