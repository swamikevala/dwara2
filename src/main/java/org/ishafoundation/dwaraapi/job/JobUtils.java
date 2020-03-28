package org.ishafoundation.dwaraapi.job;

import org.springframework.stereotype.Component;

@Component
public class JobUtils {
//
//	@Autowired
//	private TaskUtils taskUtils;
//	
//	@Autowired
//	private TaskTasksetDao taskTasksetDao;	
//	
//	@Autowired
//	private JobDao jobDao;	
//
//
//	public Job getPrerequisiteJob(Job job, int requesttypeId, int libraryclassId){
//		Job parentJob = null;
//		int taskId = job.getTaskId();
//		
//		TaskOrTasksetDetails taskOrTasksetDetails = taskUtils.getTaskOrTasksetDetails(requesttypeId, libraryclassId);
//		int tasksetId = taskOrTasksetDetails.getTasksetId();	
//	
//		if(tasksetId > 0) {// means a taskset involving multiple tasks
//			TaskTaskset taskTaskset = taskTasksetDao.findByTasksetIdAndTaskId(tasksetId, taskId);
//			int preRequisiteTaskId = taskTaskset.getPreTaskId();
//			if(preRequisiteTaskId > 0) {
//				parentJob = jobDao.findByTaskIdAndSubrequestId(preRequisiteTaskId, job.getSubrequestId());
//			}
//		}
//		
//		return parentJob;
//	}
//	
//	public List<Job> getDependentJobs(Job job, int requesttypeId, int libraryclassId){
//		List<Job> dependentJobsList = new ArrayList<Job>();
//		TaskOrTasksetDetails taskOrTasksetDetails = taskUtils.getTaskOrTasksetDetails(requesttypeId, libraryclassId);
//		int tasksetId = taskOrTasksetDetails.getTasksetId();	
//	
//		if(tasksetId > 0) {// means a taskset involving multiple tasks
//			int taskId = job.getTaskId();
//		    // getting all the dependent tasks on this task... querying on pretask with current task will give all its dependent tasks..
//		    List<TaskTaskset> taskTasksetList = taskTasksetDao.findAllByTasksetIdAndPreTaskId(tasksetId, taskId);
//
//			for (Iterator<TaskTaskset> iterator = taskTasksetList.iterator(); iterator.hasNext();) {
//				TaskTaskset taskTaskset = (TaskTaskset) iterator.next();
//				
//				int nthDependentTaskId = taskTaskset.getTaskId();
//				
//				Job nthDependentJob = jobDao.findByTaskIdAndSubrequestId(nthDependentTaskId, job.getSubrequestId());
//				dependentJobsList.add(nthDependentJob);
//			}
//		}
//		return dependentJobsList;
//	}
}
