package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

import java.util.Set;

import org.ishafoundation.dwaraapi.model.WrappedSubrequestList;

public interface SubrequestCustom {

	WrappedSubrequestList findAllByActionIdAndStatusIds(Integer actionId, Set<Integer> statusIds, int pageNumber, int pageSize);
	
	WrappedSubrequestList findAllLatestByActionAndStatusIds(int actionId, Set<Integer> statusIds, int pageNumber, int pageSize);
}
