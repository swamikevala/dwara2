package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

import java.time.LocalDateTime;
import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;

public interface RequestCustom {
	
	List<Request> findAllDynamicallyBasedOnParamsOrderByLatest(RequestType requestType, List<Action> action, List<Status> statusList, String user, LocalDateTime requestedAtStart, LocalDateTime requestedAtEnd, LocalDateTime completedAtStart, LocalDateTime completedAtEnd, String artifactName, int pageNumber, int pageSize);

}
