package org.ishafoundation.dwaraapi.storage.storagetype.thread;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.storagetype.AbstractStoragetypeJobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractStoragetypeJobManager implements Runnable{
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractStoragetypeJobManager.class);
	
	@Autowired
	private Map<String, AbstractStoragetypeJobProcessor> storagetypeJobProcessorMap;
		
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private Configuration configuration;
	
	// Not thread safe - so ensure the subclass is prototype scoped
	private List<StorageJob> storageJobList;

	public List<StorageJob> getStorageJobList() {
		return storageJobList;
	}

	public void setStorageJobList(List<StorageJob> storageJobList) {
		this.storageJobList = storageJobList;
	}

//	TODO : How to enforce that invokeAction is called without the below as run() for Tape will spawn threads which eventually needs to call invokeAction()
//	public void run() {
//		StorageTypeJob storagetypeJob = selectStorageTypeJob();
//		invokeAction(storagetypeJob);
//    }
//	
//	protected abstract StorageTypeJob selectStorageTypeJob();
	
	protected StorageResponse manage(StoragetypeJob storagetypeJob){
		storagetypeJob.setJunkFilesStagedDirName(configuration.getJunkFilesStagedDirName()); // to skip junk files from writing
		
		Job job = null;
		StorageResponse storageResponse = null;
		try {
			job = storagetypeJob.getStorageJob().getJob();
			updateJobInProgress(job);
			
			Action storagetaskAction = storagetypeJob.getStorageJob().getJob().getStoragetaskActionId();
			
			Storagetype storagetype = storagetypeJob.getStorageJob().getVolume().getStoragetype();
			AbstractStoragetypeJobProcessor storagetypeJobProcessorImpl = storagetypeJobProcessorMap.get(storagetype.name() + DwaraConstants.STORAGETYPE_JOBPROCESSOR_SUFFIX);
			Method storageTaskMethod = storagetypeJobProcessorImpl.getClass().getMethod(storagetaskAction.name(), StoragetypeJob.class);
			storageResponse = (StorageResponse) storageTaskMethod.invoke(storagetypeJobProcessorImpl, storagetypeJob);
			
			updateJobCompleted(job);
		}catch (Throwable e) {
			updateJobFailed(job);
			// updateError Table;
			logger.error(e.getMessage(), e);
		}
		return storageResponse;
	}

	protected Job updateJobInProgress(Job job) {
		if(job.getStatus() != Status.in_progress) { // If not updated already
			job.setStartedAt(LocalDateTime.now());
			job = updateJobStatus(job, Status.in_progress);
		}
		
		return job;
	}
	
	protected Job updateJobCompleted(Job job) {
		job.setCompletedAt(LocalDateTime.now());
		return updateJobStatus(job, Status.completed);
	}
	
	protected Job updateJobFailed(Job job) {
		return updateJobStatus(job, Status.failed);
	}
	
	private Job updateJobStatus(Job job, Status status) {
		job.setStatus(status);
		job = jobDao.save(job);
		logger.info("Job " + job.getId() + " - " + status);
		return job;
	}
}
