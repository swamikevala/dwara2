package org.ishafoundation.dwaraapi.controller;

import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TmpJobFileDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.TmpJobFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("scheduledStatusUpdaterController")
public class ScheduledStatusUpdaterController {
	
	@Autowired
	private JobDao jobDao;

	@Autowired
	private TmpJobFileDao jobFileDao;

	@Scheduled(fixedDelay = 60000)
    @PostMapping("/updateStatus")
    public ResponseEntity<String> updateStatus(){
    	
		List<Job> jobList = jobDao.findAllByStatusIdOrderByJobId(Status.IN_PROGRESS.getStatusId());
		for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
			Job job = (Job) iterator.next();
			
			boolean inProgress = false;
			boolean hasFailures = false;
			boolean hasAnyCompleted = false;
			boolean isAllComplete = true;
			List<TmpJobFile> jobFileList = jobFileDao.findAllByJobId(job.getJobId()); // TODO : should this be only process jobs...
			for (Iterator<TmpJobFile> iterator2 = jobFileList.iterator(); iterator2.hasNext();) {
				TmpJobFile jobFile = (TmpJobFile) iterator2.next();
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
				if(isAllComplete) {
					job.setCompletedAt(System.currentTimeMillis()); // Just can only give some rough completed times... 
					job.setStatusId(Status.COMPLETED.getStatusId());
				}
				if(hasFailures && hasAnyCompleted)
					job.setStatusId(Status.COMPLETED_WITH_FAILURE.getStatusId());
				
				jobDao.save(job);
				
				jobFileDao.deleteAll(jobFileList);
			}
		}
   	
		return null;
    }
}
