package org.ishafoundation.dwaraapi.storage.storagetask;

import java.time.LocalDateTime;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ImportStoragetaskAction implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(ImportStoragetaskAction.class);

	@Autowired
	private JobDao jobDao;	
	
	private Job job;

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	@Override
    public void run() {
		logger.trace("Import job - " + job.getId());
		try {
			updateJobInProgress(job);
			RequestDetails rd = job.getRequest().getDetails();
			String volUid = rd.getVolume_uid();
			String volGrpUid = rd.getVolume_group_uid();
			logger.trace("Importing catalog for - " + volUid);
			
			logger.trace("Attaching " + volUid + " to " + volGrpUid);
			
			updateJobCompleted(job);
		}catch (Exception e) {
			updateJobFailed(job);
		}
	}

	protected Job updateJobInProgress(Job job) {
		if(job.getStatus() != Status.in_progress) { // If not updated already
			job.setStartedAt(LocalDateTime.now());
			job = updateJobStatus(job, Status.in_progress);
		}
		
		return job;
	}
	
	protected Job updateJobCompleted(Job job) {
		job.setCompletedAt(LocalDateTime.now());
		return updateJobStatus(job, Status.completed);
	}
	
	protected Job updateJobFailed(Job job) {
		return updateJobStatus(job, Status.failed);
	}
	
	private Job updateJobStatus(Job job, Status status) {
		job.setStatus(status);
		job = jobDao.save(job);
		logger.info("Job " + job.getId() + " - " + status);
		return job;
	}
}
