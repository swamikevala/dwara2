package org.ishafoundation.dwaraapi.job;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
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
public class JobManipulatorTest {

	private static final Logger logger = LoggerFactory.getLogger(JobManipulatorTest.class);
	
	@Autowired
	RequestDao requestDao;
	
	@Autowired
	JobDao jobDao;
	
	@Autowired
	private JobManipulator jobManipulator;
	
	@Test
	public void test_a() {
		Request request = requestDao.findById(2).get();
		List<Job> jobListForResponse = jobManipulator.getJobs(request);
		for (Job job : jobListForResponse) {
			System.out.println(job.getuId() + ":" + job.getuIdDependencies());	
		}
		
	}
}
