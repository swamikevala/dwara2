package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Tag;
import org.springframework.data.repository.CrudRepository;

public interface TagDao extends CrudRepository<Tag, String> {
    public List<Tag> findByArtifacts_Id(int artifactId);
}
