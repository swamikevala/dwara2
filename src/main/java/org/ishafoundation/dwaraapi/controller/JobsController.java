package org.ishafoundation.dwaraapi.controller;

import org.ishafoundation.dwaraapi.job.JobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// mimicks the scheduler

@RestController
@RequestMapping("jobsSchedulerMimick")
public class JobsController {
	
	@Autowired
	private JobManager workflowManager;
	
    @PostMapping("/triggerJobs")
    public ResponseEntity<String> triggerJobs(){
    	
    	workflowManager.processJobs();
    	
		return null;
    }
}
