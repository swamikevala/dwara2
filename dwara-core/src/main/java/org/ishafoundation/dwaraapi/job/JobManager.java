package org.ishafoundation.dwaraapi.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.utils.JobUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
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
	private JobDao jobDao;	
	
	@Autowired
	private JobUtil jobUtil;	
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private StoragetypeJobDelegator storagetypeJobDelegator;
		
	@Autowired
	private ProcessingtaskSingleThreadExecutor processingtaskSingleThreadExecutor;
	
	@Autowired
	private ImportStoragetaskSingleThreadExecutor importStoragetaskSingleThreadExecutor;
	
	public void manageJobs() {
		logger.info("***** Managing jobs now *****");
		List<Job> storageJobList = new ArrayList<Job>();
		
		// Need to block all storage jobs from picked up for processing, when there is a queued/inprogress mapdrive/format request... 
		List<Action> actionList = new ArrayList<Action>();
		actionList.add(Action.map_tapedrives);
		actionList.add(Action.initialize);

		// If a subrequest action type is mapdrive and status is queued or inprogress skip storage jobs...
		long blockingJobsLinedUp = jobDao.countByStoragetaskActionIdInAndStatus(actionList, Status.queued);
		long blockingJobsInFlight = jobDao.countByStoragetaskActionIdInAndStatus(actionList, Status.in_progress);
		
		
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
				logger.trace("Job - " + job.getId() + ":" + jobName);
				boolean isJobReadyToBeProcessed = jobUtil.isJobReadyToBeExecuted(job);
				logger.trace("IsJobReadyToBeProcessed - " + isJobReadyToBeProcessed);
				if(isJobReadyToBeProcessed) {
					if(processingtaskId != null) { // a non-storage process job
						// This check is because of the same file getting queued up for processing again...
						// JobManager --> get all "Queued" processingjobs --> ProcessingJobManager ==== thread per file ====> ProcessingJobProcessor --> Only when the file's turn comes the status change to inprogress
						// Next iteration --> get all "Queued" processingjobs would still show the same job above sent already to ProcessingJobManager as it has to wait for its turn for CPU cycle... 
						ThreadPoolExecutor tpe = (ThreadPoolExecutor) processingtaskSingleThreadExecutor.getExecutor();
						BlockingQueue<Runnable> runnableQueueList = tpe.getQueue();
						boolean alreadyQueued = false;
						for (Runnable runnable : runnableQueueList) {
							ProcessingJobManager pjm = (ProcessingJobManager) runnable;
							if(job.getId() == pjm.getJob().getId()) {
								logger.debug(job.getId() + " already in ProcessingJobManager queue. Skipping it...");
								alreadyQueued = true;
								break;
							}
						}
						if(!alreadyQueued) { // only when the job is not already dispatched to the queue to be executed, send it now...
							ProcessingJobManager processingJobManager = applicationContext.getBean(ProcessingJobManager.class);
							processingJobManager.setJob(job);
							logger.debug(job.getId() + " added to the ProcessingJobManager queue.");
							tpe.execute(processingJobManager);
						}
					}else {
						if(storagetaskAction == Action.import_) {
							ImportStoragetaskAction importStoragetaskAction = applicationContext.getBean(ImportStoragetaskAction.class);
							importStoragetaskAction.setJob(job);
							importStoragetaskSingleThreadExecutor.getExecutor().execute(importStoragetaskAction);
						}
						if(blockingJobsInFlight > 0) { // format/tape map drive request in progress, so blocking all storage jobs until the job is complete...
							logger.trace("Skipping adding to storagejob collection as a blocking request " + Status.in_progress.name());
						}
						else if(blockingJobsLinedUp > 0) { // if any format/tape map drive request queued up
							// only adding one blocking job to the list
							if(job.getRequest().getActionId() == Action.initialize || job.getRequest().getActionId() == Action.map_tapedrives) {
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
}
	
