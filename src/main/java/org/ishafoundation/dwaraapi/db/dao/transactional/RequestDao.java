package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.dao.transactional.custom.RequestCustom;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.springframework.data.repository.CrudRepository;

public interface RequestDao extends CrudRepository<Request,Integer>, RequestCustom {
	
	//List<Request> findAllByActionIdAndUserIdAndRequestedAtOrderByRequestedAtDesc(int actionId, int userId, String startDate, Pageable pageable);
	
}