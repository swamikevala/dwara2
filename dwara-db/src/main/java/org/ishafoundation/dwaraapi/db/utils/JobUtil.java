package org.ishafoundation.dwaraapi.db.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.db.dao.master.jointables.FlowelementDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Flowelement;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobUtil {

	@Autowired
	private FlowelementDao flowelementDao;
	
	@Autowired
	private JobDao jobDao;
	
	// used during JobManagement
	public boolean isWriteJobReadyToBeExecuted(Job writeJob){
		boolean isWriteJobReadyToBeExecuted = true;
		Flowelement fe = writeJob.getFlowelement();
		
		List<Flowelement> flowelementList = flowelementDao.findAllByFlowIdAndActiveTrueOrderByDisplayOrderAsc(fe.getFlow().getId());
		Map<Flowelement, List<Flowelement>> flowelement_DepenedentFlowelements_Map = new HashMap<Flowelement, List<Flowelement>>();
		getWriteJobNestedDependentFlowelements(fe, flowelementList, flowelement_DepenedentFlowelements_Map);
		
		List<Job> jobsOnRequest = jobDao.findAllByRequestId(writeJob.getRequest().getId());
		Map<Flowelement, Job> flowelement_Job_Map = new HashMap<Flowelement, Job>();
		getWriteJobNestedDependentJobs(writeJob, jobsOnRequest, flowelement_Job_Map);
		
		for (Flowelement flowelement : flowelementList) {
			List<Flowelement> dependentFlowElementsList = flowelement_DepenedentFlowelements_Map.get(flowelement);
			
			for (Flowelement dependentFlowElement : dependentFlowElementsList) {
				Job dependentJob = flowelement_Job_Map.get(dependentFlowElement);
				if(dependentJob != null) { // Job already created - check status
					if(dependentJob.getStatus() != Status.completed) // if status is not completed
						isWriteJobReadyToBeExecuted = false;
				}
				else // Job not created at all
					isWriteJobReadyToBeExecuted = false;
			}
			
		}		
		return isWriteJobReadyToBeExecuted;
	}
	
	// Write Job's nested(depth >= 1) Dependent Jobs - For eg., Write --> Restore. Restore --> verify 
	private void getWriteJobNestedDependentJobs(Job job, List<Job> jobsOnRequest, Map<Flowelement, Job> flowelement_Job_Map) {
		for (Job nthJob : jobsOnRequest) {
			List<Integer> preReqJobIds = nthJob.getDependencies();
			if(preReqJobIds != null && preReqJobIds.contains(job.getId())) { // if the job has a direct dependency to the current job(running process job that generates the output) 
				flowelement_Job_Map.put(job.getFlowelement(), job);
				getWriteJobNestedDependentJobs(job, jobsOnRequest, flowelement_Job_Map);
			}
		}
	}
	
	private void getWriteJobNestedDependentFlowelements(Flowelement fe, List<Flowelement> flowelementList, Map<Flowelement, List<Flowelement>> flowelement_DependentFlowelements_Map) {
		for (Flowelement nthFlowelement : flowelementList) {
			if(nthFlowelement.getId() == fe.getId())
				continue;
			
			List<Integer> preReqs = nthFlowelement.getDependencies();
			if(preReqs != null && preReqs.contains(fe.getId())) {
				List<Flowelement> dependentFlowelementList = flowelement_DependentFlowelements_Map.get(fe);
				if(dependentFlowelementList == null) {
					dependentFlowelementList = new ArrayList<Flowelement>();
					flowelement_DependentFlowelements_Map.put(fe, dependentFlowelementList);
				}
				dependentFlowelementList.add(nthFlowelement);
			}
		}
	}
	
//	
//	public List<Job> getWriteDependentJobs(Job writeJob){
//		
//		Map<Flowelement, Job> flowelement_Job_Map = new HashMap<Flowelement, Job>();
//		List<Job> jobsOnRequest = jobDao.findAllByRequestId(writeJob.getRequest().getId());
//
//		for (Job nthJob : jobsOnRequest) {
//			List<Integer> preReqJobIds = nthJob.getDependencies();
//			if(preReqJobIds != null && preReqJobIds.contains(writeJob.getId())) { // if the job has a direct dependency to the current job(running process job that generates the output) 
//				depenedentJobsList.add(nthJob);
//				getNestedDependentJobs(job, jobsOnRequest, depenedentJobsList);
//			}
//		}
//
//		for (Flowelement flowelement : depenedentFlowelementsList) {
//			
//		}
//		List<Job> depenedentJobsList = new ArrayList<Job>(); 
//
//		// if jobs are already created for all the nested dependencies
//		
//		
//		getNestedDependentJobs(writeJob, jobList, depenedentJobsList);
//		
////		for (Job nthDependentJob : depenedentJobsList) {
////			if(nthDependentJob.getStatus() == Status.completed)
////				
////		}
//		
//		for (Job nthJob : jobList) {
//			List<Integer> preReqJobIds = nthJob.getDependencies();
//			if(preReqJobIds != null && preReqJobIds.contains(writeJob.getId())) { // if the job has a direct dependency to the current job(running process job that generates the output) 
//				depenedentJobsList.add(nthJob);
//				if(nthJob)
//			}
//		}
//
//
//	}
	
	
//	private void getNestedDependentJobs(Job job, List<Job> jobsOnRequest, List<Job> depenedentJobsList) {
//		for (Job nthJob : jobsOnRequest) {
//			List<Integer> preReqJobIds = nthJob.getDependencies();
//			if(preReqJobIds != null && preReqJobIds.contains(job.getId())) { // if the job has a direct dependency to the current job(running process job that generates the output) 
//				depenedentJobsList.add(nthJob);
//				getNestedDependentJobs(job, jobsOnRequest, depenedentJobsList);
//			}
//		}
//	}

	
	public List<Job> getDependentJobs(Job job){ 
		List<Job> depenedentJobsList = new ArrayList<Job>(); 
		
		List<Job> jobList = jobDao.findAllByRequestId(job.getRequest().getId());
		for (Job nthJob : jobList) {
			List<Integer> preReqJobIds = nthJob.getDependencies();
			if(preReqJobIds != null) {
				/*
				Need to set inputArtifact for jobs that has
					1) a direct dependency - for eg., write job has a direct dependency reference to the proxy-gen job
					2) a chained dependency - for eg., a verify job which dependency references a verify and write job, one/both of them which inturn then has a direct dependency reference to the proxy-gen job 
				*/
				if(preReqJobIds.contains(job.getId())) // if the job has a direct dependency to the current job(running process job that generates the output) 
					depenedentJobsList.add(nthJob);
				else { // if the job has a chained dependency to the current job(running process job that generates the output) 
					// Now use one of the processing jobs that too generating an output
					for (Integer preReqJobId : preReqJobIds) {
						Job dependentParentJob = jobDao.findById(preReqJobId).get();
						List<Integer> dependentParentList = dependentParentJob.getDependencies();
						if(dependentParentList != null && dependentParentList.contains(job.getId())) {
							depenedentJobsList.add(nthJob);
							break;
						}
					}
				}
			}
		}
		
		return depenedentJobsList;
	}
	
	// If a job is a dependent job the job's prerequisite job's status should be completed for it to be ready to be taken up for processing...
	public boolean isJobReadyToBeExecuted(Job job) {
		boolean isJobReadyToBeProcessed = true;

		List<Integer> dependencies = job.getDependencies();
		if(dependencies != null) {
			for (Integer nthPreReqJobId : dependencies) {
				Status preReqJobStatus = jobDao.findById(nthPreReqJobId).get().getStatus();
				if(preReqJobStatus != Status.completed && preReqJobStatus != Status.partially_completed && preReqJobStatus != Status.completed_failures && preReqJobStatus != Status.marked_completed)
					return false;			
			}
		}

		return isJobReadyToBeProcessed;
	}
}
