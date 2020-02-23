package org.ishafoundation.dwaraapi.workflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.common.RequesttypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.ingest.RequesttypeLibraryclassDao;
import org.ishafoundation.dwaraapi.db.dao.master.workflow.TaskTasksetDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.common.Requesttype;
import org.ishafoundation.dwaraapi.db.model.master.ingest.RequesttypeLibraryclass;
import org.ishafoundation.dwaraapi.db.model.master.workflow.TaskTaskset;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.model.TaskOrTasksetDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobUtils {
	
	@Autowired
	private RequesttypeLibraryclassDao requesttypeLibraryclassDao;

	@Autowired
	private RequesttypeDao requesttypeDao;
	
	@Autowired
	private TaskTasksetDao taskTasksetDao;	
	
	@Autowired
	private JobDao jobDao;	


	public Job getPrerequisiteJob(Job job, Request request){
		Job parentJob = null;
		int taskId = job.getTaskId();
		int requestId = request.getRequestId();
		
		TaskOrTasksetDetails taskOrTasksetDetails = getTaskOrTasksetDetails(request);
		int tasksetId = taskOrTasksetDetails.getTasksetId();	
	
		if(tasksetId > 0) {// means a taskset involving multiple tasks
			TaskTaskset taskTaskset = taskTasksetDao.findByTasksetIdAndTaskId(tasksetId, taskId);
			int preRequisiteTaskId = taskTaskset.getPreTaskId();
			if(preRequisiteTaskId > 0) {
				parentJob = jobDao.findByTaskIdAndRequestId(preRequisiteTaskId, requestId);
			}
		}
		
		return parentJob;
	}
	
	public List<Job> getDependentJobs(Job job, Request request){
		List<Job> dependentJobsList = new ArrayList<Job>();
		TaskOrTasksetDetails taskOrTasksetDetails = getTaskOrTasksetDetails(request);
		int tasksetId = taskOrTasksetDetails.getTasksetId();	
	
		if(tasksetId > 0) {// means a taskset involving multiple tasks
			int taskId = job.getTaskId();
		    // getting all the dependent tasks on this task... querying on pretask with current task will give all its dependent tasks..
		    List<TaskTaskset> taskTasksetList = taskTasksetDao.findAllByTasksetIdAndPreTaskId(tasksetId, taskId);

			for (Iterator<TaskTaskset> iterator = taskTasksetList.iterator(); iterator.hasNext();) {
				TaskTaskset taskTaskset = (TaskTaskset) iterator.next();
				
				int nthDependentTaskId = taskTaskset.getTaskId();
				
				Job nthDependentJob = jobDao.findByTaskIdAndRequestId(nthDependentTaskId, request.getRequestId());
				dependentJobsList.add(nthDependentJob);
			}
		}
		return dependentJobsList;
	}

	// TODO Move this to taskUtils...
	public TaskOrTasksetDetails getTaskOrTasksetDetails(Request request) {
		int requesttypeId = request.getRequesttypeId();
		int libraryclassId = request.getLibraryclassId();
		
		int tasksetId = 0;
		int taskId = 0;
		RequesttypeLibraryclass requesttypeLibraryclass = requesttypeLibraryclassDao.findByRequesttypeIdAndLibraryclassId(requesttypeId, libraryclassId);
		if(requesttypeLibraryclass != null) { // means library class specific workflow
			tasksetId = requesttypeLibraryclass.getTasksetId();
			taskId = requesttypeLibraryclass.getTaskId();
		}else { // means default ingest workflow or restore
			Requesttype requesttype = requesttypeDao.findById(requesttypeId).get();
			tasksetId = requesttype.getTasksetId();
			taskId = requesttype.getTaskId();
		}
		
		TaskOrTasksetDetails taskOrTasksetDetails = new TaskOrTasksetDetails();
		taskOrTasksetDetails.setTasksetId(tasksetId);
		taskOrTasksetDetails.setTaskId(taskId);
		return taskOrTasksetDetails; // if both tasksetId and taskId are 0 it means no job need to be created... 
	}
}
