package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;
import java.util.Set;

import org.ishafoundation.dwaraapi.db.model.transactional.Clip;
import org.springframework.data.repository.CrudRepository;

public interface ClipDao extends CrudRepository<Clip,Integer> {
    List<Clip> findAllByIdIn(Set<Integer> clipId );

}
