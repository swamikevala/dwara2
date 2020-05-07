package org.ishafoundation.dwaraapi.controller;

import org.ishafoundation.dwaraapi.entrypoint.resource.controller.scheduled.ScheduledStatusUpdaterController;
import org.ishafoundation.dwaraapi.job.JobManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FlowTest{

	@Autowired
	private JobManager jobManager;
	
	@Autowired 
	private ScheduledStatusUpdaterController scheduledStatusUpdaterController;
	
	@Test
	public void processJobs() {
		int storageJobsCnt = 4;
		for (int i = 0; i < storageJobsCnt; i++) {
			jobManager.processJobs();
			
			//TODO db validate here
			
			scheduledStatusUpdaterController.updateStatus();
			
			//TODO again db validate here
		}
	}

}
