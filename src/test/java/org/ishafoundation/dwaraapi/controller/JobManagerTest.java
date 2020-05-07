package org.ishafoundation.dwaraapi.controller;

import org.ishafoundation.dwaraapi.job.JobManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JobManagerTest{

	@Autowired
	private JobManager jobManager;
	
	@Test
	@WithMockUser(username = "pgurumurthy", password = "pwd")
	public void processJobs() {
		jobManager.processJobs();
	}

}
