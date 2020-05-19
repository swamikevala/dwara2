package org.ishafoundation.dwaraapi.job;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.jointables.IngestconfigDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Ingestconfig;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Tasktype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobUtils {

	@Autowired
	private IngestconfigDao ingestconfigDao;	
	
	@Autowired
	private JobDao jobDao;	


	public Job getPrerequisiteJob(Job job) {
		Job parentJob = null;
		int taskId = job.getTaskId(); // jobs' task id
		Tasktype tasktype = job.getTasktype();
		int libraryclassId = job.getSubrequest().getLibrary().getLibraryclassId();
		int tapesetId = job.getTape().getTapeset().getId();
		
		Ingestconfig ingestconfig = ingestconfigDao.findByTaskIdAndTasktypeAndInputLibraryclassIdAndTapesetId(taskId, tasktype, libraryclassId, tapesetId);
		
		Integer prerequisiteProcessingTaskId = ingestconfig.getPreProcessingTaskId();
		if(prerequisiteProcessingTaskId != null) { // means a dependent job. Get the parent job
			parentJob = jobDao.findByTaskIdAndSubrequestId(prerequisiteProcessingTaskId, job.getSubrequest().getId());
		}
		return parentJob;
	}
	
	public List<Job> getDependentJobs(Job job){
		List<Job> dependentJobsList = new ArrayList<Job>();
		int taskId = job.getTaskId(); // jobs' task id
		int libraryclassId = job.getSubrequest().getLibrary().getLibraryclassId();
		
		List<Ingestconfig> ingestconfigList = ingestconfigDao.findAllByPreProcessingTaskIdAndInputLibraryclassIdOrderByDisplayOrderAsc(taskId, libraryclassId);
		for (Ingestconfig ingestconfig : ingestconfigList) {

			int nthDependentTaskId = ingestconfig.getTaskId();
			Job nthDependentJob = jobDao.findByTaskIdAndSubrequestId(nthDependentTaskId, job.getSubrequest().getId());
			dependentJobsList.add(nthDependentJob);
		}
		return dependentJobsList;
	}
}
