package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.Badfile;
import org.springframework.data.repository.CrudRepository;

public interface BadfileDao extends CrudRepository<Badfile,Integer> {
	

}