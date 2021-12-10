package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.custom.RequestCustom;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface RequestDao extends CrudRepository<Request,Integer>, RequestCustom {
	
	//---long countByActionIdInAndStatusIn(Collection<Action> tapedrivemappingOrFormat, Collection<Status> queuedOrInprogress);
	
	//List<Request> findAllByActionIdAndUserIdAndRequestedAtOrderByRequestedAtDesc(int actionId, int userId, String startDate, Pageable pageable);
	
	List<Request> findAllByRequestRefId(int requestRefId);
	
	List<Request> findAllByRequestRefIdOrderByIdDesc(int requestRefId);
	
	//List<Request> findAllByTypeAndActionIdAndStatusInOrderByIdDesc(RequestType requestType, Action actionId, List<Status> statusList);
	
	List<Request> findAllByTypeAndStatusIn(RequestType type, Collection<Status> statusList);
	
	List<Request> findAllByActionIdAndStatusInAndType(Action action, Collection<Status> statusList, RequestType type);
	
	List<Request> findAllByActionIdInAndStatusInAndTypeAndRequestedByIdNotNullOrderByRequestedAtDesc(Collection<Action> actionList, Collection<Status> statusList, RequestType type);

	List<Request> findAllByCompletedAtBetweenAndActionIdAndStatusInAndType(LocalDateTime startDateTime, LocalDateTime endDateTime, Action action, Collection<Status> completedVariants, RequestType type);
	
	@Query(value = "select * from request where action_id='import' and status='completed' and json_extract(details, '$.body.xmlPathname') like %?1%", nativeQuery = true)
	Request findAlreadyCompletelyImportedVolumeNative(String volumeId);
}