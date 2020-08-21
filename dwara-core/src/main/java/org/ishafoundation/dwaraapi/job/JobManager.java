package org.ishafoundation.dwaraapi.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobMapDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.JobMap;
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
	private JobMapDao jobMapDao;	
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private StoragetypeJobDelegator storagetypeJobDelegator;
		
	@Autowired
	private ProcessingtaskSingleThreadExecutor processingtaskSingleThreadExecutor;
	
	@Autowired
	private ImportStoragetaskSingleThreadExecutor importStoragetaskSingleThreadExecutor;
	
	public void manageJobs() {
		logger.trace("***** Managing jobs now *****");
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
				logger.info("job - " + job.getId() + ":" + jobName);
				boolean isJobReadyToBeProcessed = isJobReadyToBeProcessed(job);
				logger.info("isJobReadyToBeProcessed - " + isJobReadyToBeProcessed);
				if(isJobReadyToBeProcessed) {
					if(processingtaskId != null) { // a non-storage process job
						ThreadPoolExecutor tpe = (ThreadPoolExecutor) processingtaskSingleThreadExecutor.getExecutor();
						BlockingQueue<Runnable> runnableQueueList = tpe.getQueue();
						boolean alreadyQueued = false;
						for (Runnable runnable : runnableQueueList) {
							ProcessingJobManager pjm = (ProcessingJobManager) runnable;
							if(job.getId() == pjm.getJob().getId()) {
								alreadyQueued = true;
								break;
							}
						}
						if(!alreadyQueued) { // only when the job is not already dispatched to the queue to be executed, send it now...
							ProcessingJobManager processingJobManager = applicationContext.getBean(ProcessingJobManager.class);
							processingJobManager.setJob(job);

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

	// If a job is a dependent job the job's prerequisite job's status should be completed for it to be ready to be taken up for processing...
	private boolean isJobReadyToBeProcessed(Job job) {
		boolean isJobReadyToBeProcessed = true;

		List<JobMap> preReqJobRefs = jobMapDao.findAllByIdJobId(job.getId());// getting all prerequisite jobss
		for (JobMap nthPreReqJobRef : preReqJobRefs) {
			// means a dependent job.
			Status preReqJobStatus = jobDao.findById(nthPreReqJobRef.getId().getJobRefId()).get().getStatus();
			if(preReqJobStatus != Status.completed && preReqJobStatus != Status.partially_completed && preReqJobStatus != Status.completed_failures && preReqJobStatus != Status.marked_completed)
				return false;			
			
		}
			
			/*
			// TODO : To maintain the requested order of the artifacts while write...
			 	Option 1 - checksum as part of write - Not a great idea for 1) processing framework multithreaded need to be mimicked during write 2) there are as many copy jobs and we need to generate chksum only once
			 				 	
			 	Option 2 - Request and previoussystemrequest
			 	
			 	Option 3 - Checksum and verify pair it.. but verify needs to be done only after both are completed. 
			 		instead of write dependent on checksum
				 		checksum 
							write
							verify
			 	
			 		let verify dependent on write/checksum but ensure both write and checksum are completed...
					 	checksum 
						write
							verify - but also code check that checksum job is also completed...

			 	
			 	Option 4 - Change schema so both checksum and write are configured as prereq for verify
				 	checksum 
					write
						verify

			// Option 2 - Request and 
			Request currentJobRequest = job.getRequest();
			int currentJobRequestId = currentJobRequest.getId();
			
			int previousSystemRequestId = currentJobRequestId - 1;
			Request currentJobUserRequest= currentJobRequest.getRequestRef();
			List<Request> allSystemRequestsForTheUserRequest = requestDao.findAllByRequestRefIdOrderByRequestIdDesc(currentJobUserRequest.getId());
			Request previousSystemRequest = null;
			for (Request request : allSystemRequestsForTheUserRequest) {
				if(request.getId() == previousSystemRequestId) {
					previousSystemRequest = request;
				}

			}
			// if there is no previousSystemRequestId to the current job and this is the first..			
			if(previousSystemRequest != null) {
				// get all the locations write jobs and see if its complete
				// get the job
			}
			*/
			
			// Option 3 - if verify job, then ensure the checksum-generation job is completed too....
//			if(job.getStoragetaskActionId() == Action.verify) {
//				if(job.getActionelement() != null) {// TODO : means it must be ingest...
//					Job checksumJob = jobDao.findByRequestIdAndProcessingtaskId(job.getRequest().getId(), "checksum-generation");
//					
//					Status checksumJobStatus = checksumJob.getStatus();
//					if(checksumJobStatus != Status.completed && checksumJobStatus != Status.completed_failures)// TODO completed_failures too???
//						return false;
//				}
//			}
//		}

		return isJobReadyToBeProcessed;
	}
	

}
	
