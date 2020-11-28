package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.ActionArtifactclassFlowKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionArtifactclassFlow;
import org.springframework.data.repository.CrudRepository;

public interface ActionArtifactclassFlowDao extends CrudRepository<ActionArtifactclassFlow,ActionArtifactclassFlowKey> {
	
	List<ActionArtifactclassFlow> findAllByArtifactclassIdOrArtifactclassIdAndActionIdAndActiveTrue(String allArtifactclassId, String artifactclassId, String actionId);
}
