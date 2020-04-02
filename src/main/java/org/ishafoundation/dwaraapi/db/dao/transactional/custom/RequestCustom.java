package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

import java.time.LocalDateTime;

import org.ishafoundation.dwaraapi.model.WrappedRequestList;

public interface RequestCustom {
	
	WrappedRequestList findAllByRequesttypeAndUserIdAndRequestedAtOrderByLatest(Integer requesttypeId, Integer userId, LocalDateTime fromDate, LocalDateTime toDate, int pageNumber, int pageSize);

}
