package org.ishafoundation.dwaraapi.scheduler;

import org.ishafoundation.dwaraapi.job.JobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

@Component
public class ScheduledJobManagerInvoker {
	
	@Autowired
	private JobManager jobManager;
	
	@Value("${scheduler.enabled:true}")
	private boolean isEnabled;
	
	@Scheduled(fixedDelay = 60000)
    @PostMapping("/triggerJobs")
    public ResponseEntity<String> triggerJobs(){
    	if(isEnabled) {
	    	jobManager.manageJobs();
	    	return ResponseEntity.status(HttpStatus.OK).body("Done");
    	}
    	else
    		return null; 
    }
}
