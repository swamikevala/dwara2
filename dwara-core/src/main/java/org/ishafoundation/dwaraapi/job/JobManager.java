package org.ishafoundation.dwaraapi.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.storage.storagetype.StoragetypeJobDelegator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class JobManager {
	
	private static final Logger logger = LoggerFactory.getLogger(JobManager.class);
	
	@Autowired
	private SubrequestDao subrequestDao;
	
	@Autowired
	private JobDao jobDao;	
		
	@Autowired
	private VolumeDao volumeDao;

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private StoragetypeJobDelegator storagetypeManager;
	
	public void processJobs() {
		List<Job> storageJobList = new ArrayList<Job>();
		
//		// Need to block all storage jobs when there is a queued/inprogress mapdrive request... 
//		List<Status> statusList = new ArrayList<Status>();
//		statusList.add(Status.queued);
//		statusList.add(Status.in_progress);
//
//		// If a subrequest action type is mapdrive and status is queued or inprogress skip storage jobs...
//		long tapedrivemappingRequestInFlight = subrequestDao.countByRequestActionAndStatusIn(Action.map_tapedrives, statusList); 
//		boolean isTapedrivemappingReqInFlight = false;
//		if(tapedrivemappingRequestInFlight > 0)
//			isTapedrivemappingReqInFlight = true;
		
		
		List<Job> jobList = jobDao.findAllByStatusOrderById(Status.queued); // Irrespective of the tapedrivemapping or format request non storage jobs can still be dequeued, hence we are querying it all... 
//		List<Job> tapelabelingJobListQueued = jobDao.findAllBySubrequestRequestActionAndStatus(Action.format, Status.queued);
//		List<Job> tapelabelingJobListInProgress = jobDao.findAllBySubrequestRequestActionAndStatus(Action.format, Status.in_progress);
		
		if(jobList.size() > 0) {
			for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
				Job job = (Job) iterator.next();
				logger.info("job - " + job.getId());
				
				Integer storagetaskId = job.getStoragetaskId();
				Integer processingtaskId = job.getProcessingtaskId();

				boolean isJobReadyToBeProcessed = isJobReadyToBeProcessed(job);
				
				logger.info("isJobReadyToBeProcessed - " + isJobReadyToBeProcessed);
				if(isJobReadyToBeProcessed) {
					// TODO : we were doing this on tasktype, but now that there is no tasktype how to differentiate? Check with Swami
					if(processingtaskId != null) { // a non-storage process job
						logger.trace("process job");
//						ProcessingtaskJobManager_ThreadTask taskJobManager_ThreadTask = applicationContext.getBean(ProcessingtaskJobManager_ThreadTask.class);
//						taskJobManager_ThreadTask.setJob(job);
//						taskSingleThreadExecutor.getExecutor().execute(taskJobManager_ThreadTask);
					}else {
//						if(isTapedrivemappingReqInFlight) { // there is a queued map drive request, so blocking all storage jobs until the mapdrive request is complete...
//							logger.trace("Skipping adding to storagejob collection as Tapedrivemapping InFlight");
//						}
//						else if(tapelabelingJobListInProgress.size() > 0) { // if any tape labeling request already in flight
//							logger.trace("Skipping adding to storagejob collection as Tapelabeling InFlight");
//						}
//						else if(tapelabelingJobListQueued.size() > 0) { // if any tape labeling request queued up
//							// only adding the tape labeling job to the list
//							if(job.getSubrequest().getRequest().getAction() == Action.format) {
//								if(storageJobList.size() == 0) { // add only one job at a time. If already added skip it
//									storageJobList.add(job);
//									logger.trace("Added the format job to storagejob collection");
//								}
//								else
//									logger.trace("Already another format job added to storagejob collection. So skipping this");
//							}
//						}
//						else { // only add when no tapedrivemapping or format activity
							// all storage jobs need to be grouped for some optimisation...
							storageJobList.add(job);
							logger.trace("Added to storagejob collection");
//						}
					}
				}
			}
			
			if(storageJobList.size() > 0) {
				System.out.println(this.getClass().getName() + " job manager --> storagetypedelegation");
				storagetypeManager.process(storageJobList);
				
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
		
		Integer storagetaskId = job.getStoragetaskId();
		if(storagetaskId == 2 || storagetaskId == 7)
			return isJobReadyToBeProcessed;
					
		Integer inputLibraryId = job.getInputArtifactId();//The input library of a dependent job is set by the parent job after it completes the processing
		if(inputLibraryId == null) { // means its a job dependent on its parent job(to set the library to be used), which is not completed.  
			isJobReadyToBeProcessed = false;
		}
		else {
			
			Job parentJob = job.getJobRef();
			if(parentJob != null) { 
				// means a dependent job.
				Status parentJobStatus = parentJob.getStatus();
				if(parentJobStatus != Status.completed && parentJobStatus != Status.completed_failures)
					isJobReadyToBeProcessed = false;
			}
		}
		return isJobReadyToBeProcessed;
	}
	

}
	
