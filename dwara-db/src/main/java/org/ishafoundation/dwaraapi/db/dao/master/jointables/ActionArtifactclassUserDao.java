package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.ActionArtifactclassUserKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionArtifactclassUser;
import org.springframework.data.repository.CrudRepository;

public interface ActionArtifactclassUserDao extends CrudRepository<ActionArtifactclassUser,ActionArtifactclassUserKey> {
	
	List<ActionArtifactclassUser> findAllByArtifactclassIdAndActionId(String artifactclassId, String action);
}
