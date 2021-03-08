package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact1;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.Catalog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CatalogDao extends CrudRepository<Artifact1, Integer> {
    
    /* @Query(value = "select a.id, a.artifactclass_id, a.name, a.total_size, b.volume_id, c.group_ref_id from artifact1 a join artifact1_volume b join volume c where a.id=b.artifact_id and b.volume_id=c.id",
        nativeQuery = true)
    public List<Catalog> findCatalogs(); */
}
