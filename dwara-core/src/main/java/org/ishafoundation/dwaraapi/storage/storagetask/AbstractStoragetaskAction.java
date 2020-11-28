package org.ishafoundation.dwaraapi.storage.storagetask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractStoragetaskAction{

	private static final Logger logger = LoggerFactory.getLogger(AbstractStoragetaskAction.class);
	
	@Autowired
	private JobDao jobDao;		
	
	protected List<org.ishafoundation.dwaraapi.enumreferences.Action> storagetaskActionList = new ArrayList<org.ishafoundation.dwaraapi.enumreferences.Action>();
	
	public List<Job> createJobsForStoragetaskAction(Request request, Action action){
		List<Job> simpleActionJobs = new ArrayList<Job>();
		
		if(storagetaskActionList.size() == 0) { // one action to one storage task - like format/finalize etc..
			simpleActionJobs.add(createStoragetaskActionJob(null, request, action));
		}
		else { // one action to nested tree of dependent storagetasks like - rewrite to restore/write/verify
			Job job = null;
			for (Action nthAction : storagetaskActionList) {
				simpleActionJobs.add(job = createStoragetaskActionJob(job, request, nthAction));
			}
		}
		return simpleActionJobs;
	}
	
	protected Job createStoragetaskActionJob(Job preReqJob, Request request, Action storagetaskAction){
		Job job = new Job();

		if(preReqJob != null) {
			List<Integer> dependencies = job.getDependencies();
			if(dependencies == null) {
				dependencies = new ArrayList<Integer>();
			}
			dependencies.add(preReqJob.getId());
			job.setDependencies(dependencies);
		}
		job.setStoragetaskActionId(storagetaskAction);
		job.setRequest(request);
		// TODO needed only for sub actions - job.setInputArtifactId(subrequest.getDetails().getArtifact_id());
		job.setCreatedAt(LocalDateTime.now());
		job.setStatus(Status.queued);
		return saveJob(job);
	}

	// Only the information absolutely needed for tape job selection is set here - rest is all lazy loaded after the job is selected, so we dont load up all info upfront, only to be not used later...
	public StorageJob buildStorageJob(Job job) throws Exception{
		// If needed to be overwritten by subclass implementations
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		return storageJob;
	}

	protected Job saveJob(Job job) {
		job = jobDao.save(job);
		logger.info(DwaraConstants.JOB + job.getId());
		return job;
	}



}
