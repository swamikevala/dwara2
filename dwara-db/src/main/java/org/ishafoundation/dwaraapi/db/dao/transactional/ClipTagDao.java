package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.dao.transactional.custom.JobCustom;
import org.ishafoundation.dwaraapi.db.model.transactional.ClipTag;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ClipTagDao extends CrudRepository<ClipTag,Integer>{
    List<ClipTag> findByTagIdIn(List<Integer> tagIds);
    List<ClipTag> findAllByClipId(int clipId);
}

