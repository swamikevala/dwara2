package org.ishafoundation.dwaraapi.controller;

import org.ishafoundation.dwaraapi.job.JobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("jobsSchedulerMimick")
public class JobsController {
	
	@Autowired
	private JobManager jobManager;
	
	@Scheduled(fixedDelay = 60000)
    @PostMapping("/triggerJobs")
    public ResponseEntity<String> triggerJobs(){
    	
    	jobManager.processJobs();
    	
    	return ResponseEntity.status(HttpStatus.OK).body("Done"); 
    }
}
