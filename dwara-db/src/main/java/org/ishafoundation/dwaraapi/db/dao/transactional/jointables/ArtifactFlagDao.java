package org.ishafoundation.dwaraapi.db.dao.transactional.jointables;

import org.ishafoundation.dwaraapi.db.keys.ArtifactFlagKey;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactFlag;
import org.springframework.data.repository.CrudRepository;

public interface ArtifactFlagDao extends CrudRepository<ArtifactFlag, ArtifactFlagKey> {
}