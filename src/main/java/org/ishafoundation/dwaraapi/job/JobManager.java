package org.ishafoundation.dwaraapi.job;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.constants.Action;
import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.dao.master.TaskDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.TaskTasksetDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.Task;
import org.ishafoundation.dwaraapi.db.model.master.jointables.TaskTaskset;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.process.thread.executor.StorageSingleThreadExecutor;
import org.ishafoundation.dwaraapi.process.thread.executor.TaskSingleThreadExecutor;
import org.ishafoundation.dwaraapi.process.thread.task.TaskJobManager_ThreadTask;
import org.ishafoundation.dwaraapi.storage.thread.task.StorageJobsManager_ThreadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class JobManager {
	
	private static final Logger logger = LoggerFactory.getLogger(JobManager.class);
	
	@Autowired
	private TaskTasksetDao taskTasksetDao;	
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private TaskDao taskDao;
		
	@Autowired
	private JobUtils jobUtils;	
	
	@Autowired
	private TaskUtils taskUtils;
	
	@Autowired
	private TaskSingleThreadExecutor taskSingleThreadExecutor;
	
	@Autowired
	private StorageSingleThreadExecutor storageSingleThreadExecutor;

	@Autowired
	private ApplicationContext applicationContext;

	
	public List<Job> createJobs(Request request, Subrequest subrequest, Library library){
		List<Job> jobList = new ArrayList<Job>();

		int tasksetId = request.getLibraryclass().getTasksetId();
		logger.trace("tasksetId - " + tasksetId);
		List<TaskTaskset> taskTasksetList = taskTasksetDao.findAllByTasksetId(tasksetId);
		for (Iterator<TaskTaskset> iterator = taskTasksetList.iterator(); iterator.hasNext();) {
			TaskTaskset taskTaskset = (TaskTaskset) iterator.next();
			
			Job job = new Job();
			Task nthTask = taskTaskset.getTask();
			logger.trace("nthTaskId in set - " + nthTask.getId());
			job.setTask(nthTask);
			// If a task contains prerequisite task that means its a derived one, for which the input library id needs to set by the prerequisite/parent task's job at the time of its processing and not at the time of job creation...
			// for eg., Mezz copy wont have a input library id upfront, which will be generated by its prerequisite/parent Mezz Transcoding job...
			if(taskTaskset.getPreTask() == null) {
				if(library != null) // For eg., restore requests if they decide to use taskset instead of tasks as right now, library will be null and this section is not needed anyway...
					job.setInputLibrary(library);
			}
			job.setSubrequest(subrequest);				
			job.setCreatedAt(LocalDateTime.now());
			job.setStatus(Status.queued);
			jobList.add(job);
		}
		
		return jobList;
	}
	
	public Job createJobForRestore(Request request, Subrequest subrequest){
			Job job = new Job();
			Task nthTask = taskDao.findByName(request.getAction().name());
			logger.trace("nthTaskId in set - " + nthTask.getId());
			job.setTask(nthTask);
			job.setSubrequest(subrequest);				
			job.setCreatedAt(LocalDateTime.now());
			job.setStatus(Status.queued);

		return job;
	}	

	public void processJobs() {
		List<Job> storageJobList = new ArrayList<Job>();
		List<Job> jobList = jobDao.findAllByStatusOrderById(Status.queued);
		for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
			Job job = (Job) iterator.next();
			logger.info("job - " + job.getId());
			Task task = job.getTask();
			// check prerequisite jobs completion status
			boolean isJobReadyToBeProcessed = isJobReadyToBeProcessed(job);
			logger.info("isJobReadyToBeProcessed - " + isJobReadyToBeProcessed);
			if(task.getName().equals(Action.restore.toString()) || isJobReadyToBeProcessed) {
				// TODO : we were doing this on tasktype, but now that there is no tasktype how to differentiate? Check with Swami
				if(!taskUtils.isTaskStorage(task)) { // a non-storage process job
					logger.trace("process job");
					TaskJobManager_ThreadTask taskJobManager_ThreadTask = applicationContext.getBean(TaskJobManager_ThreadTask.class);
					taskJobManager_ThreadTask.setJob(job);
					taskSingleThreadExecutor.getExecutor().execute(taskJobManager_ThreadTask);
				}else {
					logger.trace("added to storagejob collection");
					// all storage jobs need to be grouped for some optimisation...
					storageJobList.add(job);
				}
			}
		}
		
		StorageJobsManager_ThreadTask storageThreadTask = applicationContext.getBean(StorageJobsManager_ThreadTask.class);
		storageThreadTask.setJobList(storageJobList);
		storageSingleThreadExecutor.getExecutor().execute(storageThreadTask);
	}

	// If a job is a dependent job the parent job's status should be completed for it to be ready to be taken up for processing...
	private boolean isJobReadyToBeProcessed(Job job) {
		boolean isJobReadyToBeProcessed = true;
		
		Library inputLibrary = job.getInputLibrary();//The input library of a dependent job is set by the parent job after it completes the processing
		if(inputLibrary == null) { // means its a job dependent on its parent job(to set the library to be used), which is not completed.  
			isJobReadyToBeProcessed = false;
		}
		else {
			Job parentJob = jobUtils.getPrerequisiteJob(job); // Has the prerequisite job's status is complete?
			if(parentJob != null) { 
				// means a dependent job.
				Status parentJobStatus = parentJob.getStatus();
				if(parentJobStatus != Status.completed && parentJobStatus != Status.completed_with_failures)
					isJobReadyToBeProcessed = false;
			}
		}
		return isJobReadyToBeProcessed;
	}
	

}
	
