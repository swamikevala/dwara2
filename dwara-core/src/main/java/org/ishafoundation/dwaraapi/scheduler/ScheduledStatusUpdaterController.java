package org.ishafoundation.dwaraapi.scheduler;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileJobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("scheduledStatusUpdaterController")
public class ScheduledStatusUpdaterController {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduledStatusUpdaterController.class);
	
	@Autowired
	private JobDao jobDao;

	@Autowired
	private TFileJobDao tFileJobDao;

	@Value("${scheduler.enabled:true}")
	private boolean isEnabled;
	
	@Scheduled(fixedDelay = 60000)
    @PostMapping("/updateStatus")
    public ResponseEntity<String> updateStatus(){
    	if(isEnabled) {
    		updateTransactionalTablesStatus();
	    	return ResponseEntity.status(HttpStatus.OK).body("Done");
    	}
    	else
    		return null; 
    }
	
	public void updateTransactionalTablesStatus() {
		List<Job> jobList = jobDao.findAllByStatusAndProcessingtaskIdIsNotNullOrderById(Status.in_progress);
		for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
			Job job = (Job) iterator.next();
			if(job.getProcessingtaskId() != null){ // consolidated status update needed only for process jobs...
				boolean inProgress = false;
				boolean hasFailures = false;
				boolean hasAnyCompleted = false;
				boolean isAllComplete = true;
				List<TFileJob> jobFileList = tFileJobDao.findAllByJobId(job.getId()); 
				for (Iterator<TFileJob> iterator2 = jobFileList.iterator(); iterator2.hasNext();) {
					TFileJob jobFile = (TFileJob) iterator2.next();
					Status status = jobFile.getStatus();
					if(status == Status.in_progress) {
						inProgress = true;
						isAllComplete = false;
						break;
					}
					if(status == Status.failed) {
						isAllComplete = false;
						hasFailures = true;
					}
					else if(status == Status.completed) {
						hasAnyCompleted = true;
					}
				}
				
				if(!inProgress) {
					Status status = null;
					if(isAllComplete) {
						job.setCompletedAt(LocalDateTime.now()); // Just can only give some rough completed times... 
						status = Status.completed;
						
					}
					if(hasFailures && hasAnyCompleted)
						status = Status.completed_failures;
					
					job.setStatus(status);
					jobDao.save(job);
					logger.info("Job " + job.getId() + " - " + status);
					
					tFileJobDao.deleteAll(jobFileList);
					logger.info("tFileJob cleaned up files of Job " + job.getId());
				}
			}
		}
	}
}