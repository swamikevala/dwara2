package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.custom.SubrequestCustom;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.springframework.data.repository.CrudRepository;

public interface SubrequestDao extends CrudRepository<Subrequest,Integer>, SubrequestCustom {
	
	List<Subrequest> findAllByRequestId(int requestId);

}