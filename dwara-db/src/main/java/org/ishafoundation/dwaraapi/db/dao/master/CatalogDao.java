package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.springframework.data.repository.CrudRepository;

public interface CatalogDao extends CrudRepository<Artifact, Integer> {
    
    /* @Query(value = "select a.id, a.artifactclass_id, a.name, a.total_size, b.volume_id, c.group_ref_id from artifact1 a join artifact1_volume b join volume c where a.id=b.artifact_id and b.volume_id=c.id",
        nativeQuery = true)
    public List<Catalog> findCatalogs(); */
}
