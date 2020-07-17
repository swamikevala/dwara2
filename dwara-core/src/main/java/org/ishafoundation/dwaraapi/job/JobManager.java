package org.ishafoundation.dwaraapi.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.process.thread.ProcessingJobManager;
import org.ishafoundation.dwaraapi.storage.storagetask.ImportStoragetaskAction;
import org.ishafoundation.dwaraapi.storage.storagetype.StoragetypeJobDelegator;
import org.ishafoundation.dwaraapi.thread.executor.ImportStoragetaskSingleThreadExecutor;
import org.ishafoundation.dwaraapi.thread.executor.ProcessingtaskSingleThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class JobManager {
	
	private static final Logger logger = LoggerFactory.getLogger(JobManager.class);

	@Autowired
	private RequestDao requestDao;	
	
	@Autowired
	private JobDao jobDao;	

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private StoragetypeJobDelegator storagetypeJobDelegator;
		
	@Autowired
	private ProcessingtaskSingleThreadExecutor processingtaskSingleThreadExecutor;
	
	@Autowired
	private ImportStoragetaskSingleThreadExecutor importStoragetaskSingleThreadExecutor;
	
	@Autowired
	private Map<String, IProcessingTask> processingtaskActionMap;
	
	public void manageJobs() {
		logger.trace("***** Managing jobs now *****");
		List<Job> storageJobList = new ArrayList<Job>();
		
		// Need to block all storage jobs from picked up for processing, when there is a queued/inprogress mapdrive/format request... 
		List<Action> actionList = new ArrayList<Action>();
		actionList.add(Action.map_tapedrives);
		actionList.add(Action.format);

		// If a subrequest action type is mapdrive and status is queued or inprogress skip storage jobs...
		long blockingRequestsLinedUp = requestDao.countByActionIdInAndStatus(actionList, Status.queued);
		long blockingRequestsInFlight = requestDao.countByActionIdInAndStatus(actionList, Status.in_progress);
		
		
		List<Job> jobList = jobDao.findAllByStatusOrderById(Status.queued); // Irrespective of the tapedrivemapping or format request non storage jobs can still be dequeued, hence we are querying it all... 
		
		if(jobList.size() > 0) {
			for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
				Job job = (Job) iterator.next();
				
				String jobName = null;
				Action storagetaskAction = job.getStoragetaskActionId();
				String processingtaskId = job.getProcessingtaskId();
				if(storagetaskAction != null) {
					jobName = storagetaskAction.name();
				}
				else {
					jobName = processingtaskId;
				}
				logger.info("job - " + job.getId() + ":" + jobName);
				boolean isJobReadyToBeProcessed = isJobReadyToBeProcessed(job);
				logger.info("isJobReadyToBeProcessed - " + isJobReadyToBeProcessed);
				if(isJobReadyToBeProcessed) {
					if(processingtaskId != null) { // a non-storage process job
//						logger.trace("process job");
//						ProcessingJobManager 
//						processingtaskActionMap.get(processingtaskId).execute();
//						logger.trace("done");
						ProcessingJobManager processingJobManager = applicationContext.getBean(ProcessingJobManager.class);
						processingJobManager.setJob(job);
						processingtaskSingleThreadExecutor.getExecutor().execute(processingJobManager);
					}else {
						if(storagetaskAction == Action.import_) {
							ImportStoragetaskAction importStoragetaskAction = applicationContext.getBean(ImportStoragetaskAction.class);
							importStoragetaskAction.setJob(job);
							importStoragetaskSingleThreadExecutor.getExecutor().execute(importStoragetaskAction);
						}
						if(blockingRequestsInFlight > 0) { // format/tape map drive request in progress, so blocking all storage jobs until the job is complete...
							logger.trace("Skipping adding to storagejob collection as a blocking request " + Status.in_progress.name());
						}
						else if(blockingRequestsLinedUp > 0) { // if any format/tape map drive request queued up
							// only adding one blocking job to the list
							if(job.getRequest().getActionId() == Action.format || job.getRequest().getActionId() == Action.map_tapedrives) {
								if(storageJobList.size() == 0) { // add only one job at a time. If already added skip adding to the list and continue loop(we still need to continue so non-storage jobs are managed)...
									storageJobList.add(job);
									logger.trace("Added to storagejob collection");
								}
								else
									logger.trace("Already another blocking job added to storagejob collection. So skipping this");
							}
						}
						else { // only add when no tapedrivemapping or format activity
							// all storage jobs need to be grouped for some optimisation...
							storageJobList.add(job);
							logger.trace("Added to storagejob collection");
						}
					}
				}
			}
			
			if(storageJobList.size() > 0) {
				logger.debug(storageJobList.size() + " storage jobs are process ready");
				storagetypeJobDelegator.delegate(storageJobList);
			}else {
				logger.trace("No storage job to be processed");
			}
		}
		else {
			logger.trace("No jobs queued up");
		}
	}

	// If a job is a dependent job the parent job's status should be completed for it to be ready to be taken up for processing...
	private boolean isJobReadyToBeProcessed(Job job) {
		boolean isJobReadyToBeProcessed = true;

		Job parentJob = job.getJobRef();
		if(parentJob != null) { 
			// means a dependent job.
			Status parentJobStatus = parentJob.getStatus();
			if(parentJobStatus != Status.completed && parentJobStatus != Status.completed_failures)// TODO completed_failures too???
				isJobReadyToBeProcessed = false;
		}

		return isJobReadyToBeProcessed;
	}
	

}
	
