package org.ishafoundation.dwaraapi.storage.storagetype.thread;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.JobRunDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.enumreferences.VolumeHealthStatus;
import org.ishafoundation.dwaraapi.exception.StorageException;
import org.ishafoundation.dwaraapi.helpers.ThreadNameHelper;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.service.JobServiceRequeueHelper;
import org.ishafoundation.dwaraapi.service.UserRequestHelper;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetype.AbstractStoragetypeJobProcessor;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeException;
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
	private RequestDao requestDao;
	
	@Autowired
	private VolumeDao volumeDao;	
	
	@Autowired
	private JobRunDao jobRunDao;
	
	@Autowired
	private JobCreator jobCreator;
	
	@Autowired
	private JobServiceRequeueHelper jobServiceRequeueHelper;
	
	@Autowired
	private UserRequestHelper userRequestHelper;
	
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
	
	protected StorageResponse manage(SelectedStorageJob selectedStorageJob){
		selectedStorageJob.setJunkFilesStagedDirName(configuration.getJunkFilesStagedDirName()); // to skip junk files from writing
		
		Job job = null;
		StorageResponse storageResponse = null;
		ThreadNameHelper threadNameHelper = new ThreadNameHelper();
		try {
			job = selectedStorageJob.getStorageJob().getJob();
			threadNameHelper.setThreadName(job.getRequest().getId(), job.getId());
			checkAndUpdateStatusesToInProgress(job);
			
			Action storagetaskAction = selectedStorageJob.getStorageJob().getJob().getStoragetaskActionId();
			
			Storagetype storagetype = selectedStorageJob.getStorageJob().getVolume().getStoragetype();
			AbstractStoragetypeJobProcessor storagetypeJobProcessorImpl = storagetypeJobProcessorMap.get(storagetype.name() + DwaraConstants.STORAGETYPE_JOBPROCESSOR_SUFFIX);
			Method storageTaskMethod = storagetypeJobProcessorImpl.getClass().getMethod(storagetaskAction.name(), SelectedStorageJob.class);
			storageResponse = (StorageResponse) storageTaskMethod.invoke(storagetypeJobProcessorImpl, selectedStorageJob);
			
			updateJobCompleted(job);
		}catch (Throwable e) {
			String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			logger.error(errorMsg, e);//printing the stacktrace
			job.setMessage("[error] " + errorMsg);
			updateJobFailed(job);
			
//			if(e instanceof StorageException || e instanceof TapeException) {
				if(job.getStoragetaskActionId() == Action.write || job.getStoragetaskActionId() == Action.restore) { // Any write or restore failure should have the tape marked as suspect...
					long jobAlreadyRequeuedCount = jobRunDao.countByJobId(job.getId());
					if(jobAlreadyRequeuedCount < configuration.getAllowedAutoRequeueAttemptsOnFailedStorageJobs()) {
						try {
							logger.info("Requeuing job " + job.getId() + ". Attempt " + (jobAlreadyRequeuedCount + 1));
							jobServiceRequeueHelper.requeueJob(job.getId(),DwaraConstants.SYSTEM_USER_NAME);
						} catch (Exception e1) {
							logger.error("Unable to auto requeue failed job..." + job.getId(), e1);
						}
					}
					else {
						if(job.getStoragetaskActionId() == Action.write || job.getStoragetaskActionId() == Action.restore) { // TODO: Should we Mark a tape suspect on restore failures too or only for write?
							Volume volume = selectedStorageJob.getStorageJob().getVolume();
							
							volume.setHealthstatus(VolumeHealthStatus.suspect);
							volumeDao.save(volume);
							logger.info("Marked the volume " + volume.getId() + " as suspect");
					
							// create user request for tracking
							HashMap<String, Object> data = new HashMap<String, Object>();
							data.put("volumeId", volume.getId());
							data.put("status", VolumeHealthStatus.suspect);
							String reason = "Repeated failure on job " + job.getId();
							data.put("reason", reason);
							userRequestHelper.createUserRequest(Action.mark_volume, DwaraConstants.SYSTEM_USER_NAME, Status.completed, data, reason);
						}
					}	
				}
//			}
		}finally {
			threadNameHelper.resetThreadName();
		}
		return storageResponse;
	}

	protected Job checkAndUpdateStatusesToInProgress(Job job) {
		if(job.getStatus() != Status.in_progress) { // If not updated already
			job.setStartedAt(LocalDateTime.now());
			job.setMessage(null);
			job = updateJobStatus(job, Status.in_progress);
		}
		
		Request systemGeneratedRequest = job.getRequest();
		if(systemGeneratedRequest.getStatus() != Status.in_progress) {
			String logMsg = "DB Request - " + systemGeneratedRequest.getId() + " - Update - status to " + Status.in_progress;
			logger.debug(logMsg);
	        systemGeneratedRequest.setStatus(Status.in_progress);
	        systemGeneratedRequest = requestDao.save(systemGeneratedRequest);
	        logger.debug(logMsg + " - Success");
		}			

		return job;
	}
	
	protected Job updateJobCompleted(Job job) {
		job.setCompletedAt(LocalDateTime.now());
		job.setMessage(null);
		job = updateJobStatus(job, Status.completed);
		
		try {
			jobCreator.createDependentJobs(job);
		}catch (Exception e) {
			logger.error("Unable to create dependent job for - " + job.getId());
		}
		
		return job;
	}
	
	protected Job updateJobFailed(Job job) {
		job.setCompletedAt(LocalDateTime.now());
		return updateJobStatus(job, Status.failed);
	}
	
	private Job updateJobStatus(Job job, Status status) {
		job.setStatus(status);
		job = jobDao.save(job);
		logger.info("Job " + job.getId() + " - " + status);
		return job;
	}
}
