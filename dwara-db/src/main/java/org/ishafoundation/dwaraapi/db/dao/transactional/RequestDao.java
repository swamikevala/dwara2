package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.custom.RequestCustom;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.data.repository.CrudRepository;

public interface RequestDao extends CrudRepository<Request,Integer>, RequestCustom {
	
	//---long countByActionIdInAndStatusIn(Collection<Action> tapedrivemappingOrFormat, Collection<Status> queuedOrInprogress);
	
	//List<Request> findAllByActionIdAndUserIdAndRequestedAtOrderByRequestedAtDesc(int actionId, int userId, String startDate, Pageable pageable);
	
	List<Request> findAllByRequestRefId(int requestRefId);
	
	List<Request> findAllByRequestRefIdOrderByIdDesc(int requestRefId);
	
	//List<Request> findAllByTypeAndActionIdAndStatusInOrderByIdDesc(RequestType requestType, Action actionId, List<Status> statusList);
	
	List<Request> findAllByTypeAndStatus(RequestType type, Status status);
}