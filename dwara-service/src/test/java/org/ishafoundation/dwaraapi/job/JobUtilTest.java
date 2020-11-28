package org.ishafoundation.dwaraapi.job;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.utils.JobUtil;
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
public class JobUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(JobUtilTest.class);
	
	@Autowired
	JobDao jobDao;
	
	@Autowired
	private JobUtil jobUtil;
	
	@Test
	public void test_a() {
		Job artifact1write1Job = jobDao.findById(3).get();
//		artifact1write1Job.setStatus(Status.queued);
//		jobDao.save(artifact1write1Job);

		Job artifact2write1Job = jobDao.findById(6).get();
//		artifact2write1Job.setStatus(Status.completed);
//		jobDao.save(artifact2write1Job);

		boolean ready = jobUtil.isWriteJobAndItsDependentJobsComplete(artifact1write1Job);
		
		System.out.println(ready);
	}
}
