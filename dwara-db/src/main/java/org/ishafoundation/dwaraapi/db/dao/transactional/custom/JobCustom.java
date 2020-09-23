package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Status;

public interface JobCustom {
	
	List<Job> findAllByStatusOrderByLatest(List<Status> statusList);

}
