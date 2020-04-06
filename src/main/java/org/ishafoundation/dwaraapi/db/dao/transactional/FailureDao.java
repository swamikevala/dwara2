package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.Failure;
import org.springframework.data.repository.CrudRepository;

public interface FailureDao extends CrudRepository<Failure,Integer> {

}