package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.springframework.data.repository.CrudRepository;

public interface SubrequestDao extends CrudRepository<Subrequest,Integer> {
	

}