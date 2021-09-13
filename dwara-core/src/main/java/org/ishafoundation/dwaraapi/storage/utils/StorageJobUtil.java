package org.ishafoundation.dwaraapi.storage.utils;

import java.util.Map;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageJobUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(StorageJobUtil.class);
	
	@Autowired
	private JobDao jobDao;	

	@Autowired
	private Map<String, AbstractStoragetaskAction> storagetaskActionMap;

	public StorageJob wrapJobWithStorageInfo(Job job) {
		AbstractStoragetaskAction storagetaskActionImpl = storagetaskActionMap.get(job.getStoragetaskActionId().name());
		logger.trace("building storage job - " + job.getId() + ":" + storagetaskActionImpl.getClass().getSimpleName());
		StorageJob storageJob = null;
		try {
			storageJob = storagetaskActionImpl.buildStorageJob(job);
		} catch (Exception e) {
			logger.error("Unable to gather necessary details for executing the job " + job.getId() + " - " + Status.failed, e);
			job.setMessage(e.getMessage());
			job.setStatus(Status.failed); // fail the job so it doesnt keep looping...
			job = jobDao.save(job);
		}
		return storageJob;
	}

}
