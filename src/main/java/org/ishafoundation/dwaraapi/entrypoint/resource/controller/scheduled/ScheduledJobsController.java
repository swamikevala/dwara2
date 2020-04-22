package org.ishafoundation.dwaraapi.entrypoint.resource.controller.scheduled;

import org.ishafoundation.dwaraapi.job.JobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO - Do we really want this to be exposed as a controller? 
@RestController
@RequestMapping("jobsSchedulerMimick")
public class ScheduledJobsController {
	
	@Autowired
	private JobManager jobManager;
	
	@Value("${scheduler.enabled:true}")
	private boolean isEnabled;
	
	@Scheduled(fixedDelay = 60000)
    @PostMapping("/triggerJobs")
    public ResponseEntity<String> triggerJobs(){
    	if(isEnabled) {
	    	jobManager.processJobs();
	    	return ResponseEntity.status(HttpStatus.OK).body("Done");
    	}
    	else
    		return null; 
    }
}
