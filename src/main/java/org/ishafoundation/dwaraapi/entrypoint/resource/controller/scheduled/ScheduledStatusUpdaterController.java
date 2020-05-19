package org.ishafoundation.dwaraapi.entrypoint.resource.controller.scheduled;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileJobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Tasktype;
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
	
	private void updateTransactionalTablesStatus() {
		List<Job> jobList = jobDao.findAllByStatusAndJobRefIsNullOrderById(Status.in_progress);
		for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
			Job job = (Job) iterator.next();
			if(job.getTasktype() == Tasktype.processing){ // consolidated update needed only for process jobs...
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
					if(isAllComplete) {
						job.setCompletedAt(LocalDateTime.now()); // Just can only give some rough completed times... 
						job.setStatus(Status.completed);
					}
					if(hasFailures && hasAnyCompleted)
						job.setStatus(Status.completed_failures);
					
					jobDao.save(job);
					
					tFileJobDao.deleteAll(jobFileList);
				}
			}
		}
	}
}
