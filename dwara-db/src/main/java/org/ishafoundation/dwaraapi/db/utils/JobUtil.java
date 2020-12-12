package org.ishafoundation.dwaraapi.db.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ishafoundation.dwaraapi.db.dao.master.jointables.FlowelementDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Flowelement;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobUtil {

	private static final Logger logger = LoggerFactory.getLogger(JobUtil.class);
	
	@Autowired
	private FlowelementDao flowelementDao;
	
	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private FlowelementUtil flowelementUtil;
	
	// used during JobManagement
	public boolean isWriteJobAndItsDependentJobsComplete(Job writeJob){
		boolean isWriteJobReadyToBeExecuted = true;
		
		Flowelement fe = flowelementDao.findById(writeJob.getFlowelementId()).get(); 
		
		List<Flowelement> flowelementList = flowelementUtil.getAllFlowElements(fe.getFlowId());
		Map<Integer, List<Flowelement>> flowelementId_DependentFlowelements_Map = new HashMap<Integer, List<Flowelement>>();
		getWriteJobNestedDependentFlowelements(fe, flowelementList, flowelementId_DependentFlowelements_Map);
		
		logger.trace("Dependants of Flowelement " + fe.getId());
		Set<Integer> flowelementIdSet = flowelementId_DependentFlowelements_Map.keySet();
		for (Integer nthFlowelementId : flowelementIdSet) {
		
			List<Flowelement> dependentFlowelementList = flowelementId_DependentFlowelements_Map.get(nthFlowelementId);
			for (Flowelement flowelement : dependentFlowelementList) {
				logger.trace(nthFlowelementId + " --> " + flowelement.getId());
			}
		}
		
		List<Job> jobsOnRequest = jobDao.findAllByRequestId(writeJob.getRequest().getId());
		Map<Integer, Job> flowelementId_dependentJob_Map = new HashMap<Integer, Job>();
		getWriteJobNestedDependentJobs(writeJob, jobsOnRequest, flowelementId_dependentJob_Map);

		logger.trace("Dependent Jobs");
		Set<Integer> flowelementIdSet2 = flowelementId_dependentJob_Map.keySet();
		for (Integer nthFlowelementId : flowelementIdSet2) {
			Job job = flowelementId_dependentJob_Map.get(nthFlowelementId);
			logger.trace(nthFlowelementId + " --> " + (job != null ? job.getId() : null));
		}
		
		logger.trace("Now iterating all flowelements of flow " + fe.getFlowId());
		for (Flowelement flowelement : flowelementList) {
			logger.trace("flowelement " + flowelement.getId());
			List<Flowelement> dependentFlowElementsList = flowelementId_DependentFlowelements_Map.get(flowelement.getId());
			if(dependentFlowElementsList != null) {
				for (Flowelement dependentFlowElement : dependentFlowElementsList) {
					logger.trace("dependentFlowElement " + dependentFlowElement.getId());
					
					Job dependentJob = flowelementId_dependentJob_Map.get(dependentFlowElement.getId());
					if(dependentJob != null) { // Job already created - check status
						logger.trace(dependentJob.getId() + " Job already created");
						if(dependentJob.getStatus() != Status.completed) { // if status is not completed
							isWriteJobReadyToBeExecuted = false;
							logger.trace(dependentJob.getId() + " Job still not completed");
						}else {
							logger.trace(dependentJob.getId() + " Job already completed");
						}
					}
					else { // Job not created at all
						isWriteJobReadyToBeExecuted = false;
						logger.trace("Job still not created");
					}
				}
			}else {
				logger.trace("No dependants");
			}
		}		
		return isWriteJobReadyToBeExecuted;
	}
	
	// made as public for testing
	// Write Job's nested(depth >= 1) Dependent Jobs - For eg., Write --> Restore. Restore --> verify 
	public void getWriteJobNestedDependentJobs(Job job, List<Job> jobsOnRequest, Map<Integer, Job> flowelementId_dependantJob_Map) {
		logger.trace("job " + job.getId());
		for (Job nthJob : jobsOnRequest) {
			List<Integer> preReqJobIds = nthJob.getDependencies();
			logger.trace("nthJob " + nthJob.getId() + " preReqJobIds " + preReqJobIds);
			if(preReqJobIds != null && preReqJobIds.contains(job.getId())) { // if the job has a direct dependency to the current job(running process job that generates the output)
				logger.trace(nthJob.getFlowelementId() + ":" + nthJob.getId());
				flowelementId_dependantJob_Map.put(nthJob.getFlowelementId(), nthJob);
				getWriteJobNestedDependentJobs(nthJob, jobsOnRequest, flowelementId_dependantJob_Map);
			}
		}
	}
	
	// made as public for testing
	private void getWriteJobNestedDependentFlowelements(Flowelement fe, List<Flowelement> flowelementList, Map<Integer, List<Flowelement>> flowelementId_DepenedentFlowelements_Map) {
		for (Flowelement nthFlowelement : flowelementList) {
			if(nthFlowelement.getId() == fe.getId())
				continue;
			
			List<Integer> preReqs = nthFlowelement.getDependencies();
			if(preReqs != null && preReqs.contains(fe.getId())) {
				List<Flowelement> dependentFlowelementList = flowelementId_DepenedentFlowelements_Map.get(fe.getId());
				if(dependentFlowelementList == null) {
					dependentFlowelementList = new ArrayList<Flowelement>();
					flowelementId_DepenedentFlowelements_Map.put(fe.getId(), dependentFlowelementList);
					getWriteJobNestedDependentFlowelements(nthFlowelement, flowelementList, flowelementId_DepenedentFlowelements_Map);
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
				if(preReqJobStatus != Status.completed && preReqJobStatus != Status.completed_failures && preReqJobStatus != Status.marked_completed)
					return false;			
			}
		}

		return isJobReadyToBeProcessed;
	}
}
