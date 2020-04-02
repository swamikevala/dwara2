package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

import java.util.Set;

import org.ishafoundation.dwaraapi.model.WrappedSubrequestList;

public interface SubrequestCustom {

	WrappedSubrequestList findAllByRequesttypeIdAndStatusIds(Integer requesttypeId, Set<Integer> statusIds, int pageNumber, int pageSize);
	
	WrappedSubrequestList findAllLatestByRequesttypeAndStatusIds(int requesttypeId, Set<Integer> statusIds, int pageNumber, int pageSize);
}
