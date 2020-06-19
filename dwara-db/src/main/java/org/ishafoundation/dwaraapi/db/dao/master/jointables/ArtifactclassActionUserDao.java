package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.ArtifactclassActionUserKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassActionUser;
import org.springframework.data.repository.CrudRepository;

public interface ArtifactclassActionUserDao extends CrudRepository<ArtifactclassActionUser,ArtifactclassActionUserKey> {
	
	List<ArtifactclassActionUser> findAllByArtifactclassIdAndActionId(int artifactclassId, int actionId);
	
	List<ArtifactclassActionUser> findAllByActionIdAndUserId(int actionId, int userId);
	
	List<ArtifactclassActionUser> findAllByUserId(int userId);
}
