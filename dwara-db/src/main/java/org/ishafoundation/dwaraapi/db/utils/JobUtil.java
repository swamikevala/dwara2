package org.ishafoundation.dwaraapi.db.utils;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
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
}
