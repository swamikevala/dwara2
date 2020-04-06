package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;
import org.springframework.data.repository.CrudRepository;

public interface TFileJobDao extends CrudRepository<TFileJob,Integer> {

}