package org.ishafoundation.dwaraapi.controller;

import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobFileDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.JobFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// mimicks the scheduler

@RestController
@RequestMapping("scheduledStatusCtrller")
public class ScheduledStatusUpdaterController {
	
	@Autowired
	private JobDao jobDao;

	@Autowired
	private JobFileDao jobFileDao;
	
    @PostMapping("/updateStatus")
    public ResponseEntity<String> updateStatus(){
    	
		List<Job> jobList = jobDao.findAllByStatusIdOrderByJobId(Status.IN_PROGRESS.getStatusId());
		for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
			Job job = (Job) iterator.next();
			
			boolean inProgress = false;
			boolean hasFailures = false;
			boolean hasAnyCompleted = false;
			boolean isAllComplete = true;
			List<JobFile> jobFileList = jobFileDao.findAllByJobId(job.getJobId());
			for (Iterator<JobFile> iterator2 = jobFileList.iterator(); iterator2.hasNext();) {
				JobFile jobFile = (JobFile) iterator2.next();
				int statusId = jobFile.getStatusId();
				if(statusId == Status.IN_PROGRESS.getStatusId()) {
					inProgress = true;
					isAllComplete = false;
					break;
				}
				if(statusId == Status.FAILED.getStatusId()) {
					isAllComplete = false;
					hasFailures = true;
				}
				else if(statusId == Status.COMPLETED.getStatusId()) {
					hasAnyCompleted = true;
				}
			}
			
			if(!inProgress) {
				if(isAllComplete)
					job.setStatusId(Status.COMPLETED.getStatusId());
				if(hasFailures && hasAnyCompleted)
					job.setStatusId(Status.COMPLETED_WITH_FAILURE.getStatusId());
				
				jobDao.save(job);
				
				jobFileDao.deleteAll(jobFileList);
			}
		}
   	
		return null;
    }
}
