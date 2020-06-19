package org.ishafoundation.dwaraapi.job;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobManagerTest {

	private static final Logger logger = LoggerFactory.getLogger(JobManagerTest.class);

	@Autowired
	private JobManager jobManager;
	
	@Test
	public void test_Process() {
		try {
			jobManager.processJobs();
			
			try {
				Thread.sleep(20000); // sleeping for 20 secs before we do the expected DB verification
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
