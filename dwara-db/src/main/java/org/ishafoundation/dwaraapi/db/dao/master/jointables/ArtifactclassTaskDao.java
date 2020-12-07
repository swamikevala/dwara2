package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassTask;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.springframework.data.repository.CrudRepository;

public interface ArtifactclassTaskDao extends CrudRepository<ArtifactclassTask, Integer> {

	ArtifactclassTask findByArtifactclassIdAndProcessingtaskId(String artifactclassId, String processingtaskId);
	
	ArtifactclassTask findByArtifactclassIdAndStoragetaskActionId(String artifactclassId, Action action);
	
}
