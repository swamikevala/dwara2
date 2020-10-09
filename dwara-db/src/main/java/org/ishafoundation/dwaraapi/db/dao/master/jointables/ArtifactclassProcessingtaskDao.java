package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import org.ishafoundation.dwaraapi.db.keys.ArtifactclassProcessingtaskKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassProcessingtask;
import org.springframework.data.repository.CrudRepository;

public interface ArtifactclassProcessingtaskDao extends CrudRepository<ArtifactclassProcessingtask, ArtifactclassProcessingtaskKey> {
	
}
