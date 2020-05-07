package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.Collection;
import java.util.List;

import org.ishafoundation.dwaraapi.constants.Action;
import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.dao.transactional.custom.SubrequestCustom;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.springframework.data.repository.CrudRepository;

public interface SubrequestDao extends CrudRepository<Subrequest,Integer>, SubrequestCustom {
	
	List<Subrequest> findAllByRequestId(int requestId);
	
	//select count(*) from subrequest join request on subrequest.request_id = request.id and request.action_id=8001; 
	long countByRequestActionAndStatusIn(Action action, Collection<Status> queuedORin_progress);

}