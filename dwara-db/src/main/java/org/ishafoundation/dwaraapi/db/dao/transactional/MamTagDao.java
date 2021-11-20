package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.dao.transactional.custom.JobCustom;
import org.ishafoundation.dwaraapi.db.model.transactional.MamTag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MamTagDao extends CrudRepository<MamTag,Integer> {
 List<MamTag> findByNameIn(List<String> name);

 List<MamTag> findByIdIn(List<Integer> clipTagIds);
}
