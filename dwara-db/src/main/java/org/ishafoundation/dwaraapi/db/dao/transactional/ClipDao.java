package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.dao.transactional.custom.JobCustom;
import org.ishafoundation.dwaraapi.db.model.transactional.Clip;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface ClipDao extends CrudRepository<Clip,Integer> {
    List<Clip> findAllByIdIn(Set<Integer> clipId );

}
