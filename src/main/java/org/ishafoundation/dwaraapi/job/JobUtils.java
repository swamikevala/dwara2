package org.ishafoundation.dwaraapi.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.jointables.TaskTasksetDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.keys.TaskTasksetKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.TaskTaskset;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobUtils {
	
	@Autowired
	private TaskTasksetDao taskTasksetDao;	
	
	@Autowired
	private JobDao jobDao;	


	public Job getPrerequisiteJob(Job job) {
		Job parentJob = null;
		int taskId = job.getTask().getId(); // jobs' task id
		int tasksetId = job.getSubrequest().getLibrary().getLibraryclass().getTasksetId(); 
		/* Don't be tempted to take the inputlibrary's route. The inputLibrary and its libraryclass for a dependent job is different from the origin request's library
		int tasksetId = job.getInputLibrary().getLibraryclass().getTasksetId(); 
		*/ 
		TaskTaskset taskTaskset = taskTasksetDao.findById(new TaskTasksetKey(taskId, tasksetId)).get();
		if(taskTaskset.getPreTask() != null) { // means a dependent job. Get the parent job
			parentJob = jobDao.findByTaskIdAndSubrequestId(taskTaskset.getPreTask().getId(), job.getSubrequest().getId());
		}
		return parentJob;
	}
	
	public List<Job> getDependentJobs(Job job){
		List<Job> dependentJobsList = new ArrayList<Job>();
		int taskId = job.getTask().getId(); // jobs' task id
		int tasksetId = job.getSubrequest().getLibrary().getLibraryclass().getTasksetId(); 
		/* Don't be tempted to take the inputlibrary's route. The inputLibrary and its libraryclass for a dependent job is different from the origin request's library
		int tasksetId = job.getInputLibrary().getLibraryclass().getTasksetId(); 
		*/ 

	    // getting all the dependent tasks on this task... querying on pretask with current task will give all its dependent tasks..
	    List<TaskTaskset> taskTasksetList = taskTasksetDao.findAllByTasksetIdAndPreTaskId(tasksetId, taskId);

		for (Iterator<TaskTaskset> iterator = taskTasksetList.iterator(); iterator.hasNext();) {
			TaskTaskset taskTaskset = (TaskTaskset) iterator.next();
			
			int nthDependentTaskId = taskTaskset.getTask().getId();
			
			Job nthDependentJob = jobDao.findByTaskIdAndSubrequestId(nthDependentTaskId, job.getSubrequest().getId());
			dependentJobsList.add(nthDependentJob);
		}
		return dependentJobsList;
	}
}
