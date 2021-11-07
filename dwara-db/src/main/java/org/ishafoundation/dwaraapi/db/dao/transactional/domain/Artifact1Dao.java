package org.ishafoundation.dwaraapi.db.dao.transactional.domain;

import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact1;

import java.util.List;

public interface Artifact1Dao extends ArtifactRepository<Artifact1> {
 boolean existsByName(String pathName);
 List<Artifact1> findByartifact1Ref(Artifact artifact);
}
