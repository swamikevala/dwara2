package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

import java.util.List;
import java.util.Set;

import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;

public interface SubrequestCustom {
	List<Subrequest> findSubrequestByTypeAndStatus(int requesttypeId, Set<String> status);
	
	List<Subrequest> findSubrequestByTypeAndStatusId(int requesttypeId, Set<Integer> statusId);
}
