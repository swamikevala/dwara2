package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

import java.util.List;
import java.util.Set;

import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;

public interface SubrequestCustom {

	List<Subrequest> findAllByRequesttypeAndStatusIds(int requesttypeId, Set<Integer> statusId);
	
	List<Subrequest> findAllLatestByRequesttypeAndStatusIds(int requesttypeId, Set<Integer> statusId);
}
