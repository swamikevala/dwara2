package org.ishafoundation.dwaraapi.db.utils;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobUtil {

	@Autowired
	private JobDao jobDao;
	
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
