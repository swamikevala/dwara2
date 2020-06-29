package org.ishafoundation.dwaraapi.job;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.json.JobDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DaoTests{

	@Autowired
	private JobDao jobDao;
	
	@Test
	public void testDao() {		
		Job job = new Job();
		

		JobDetails jd = new JobDetails();
		jd.setDevice_id(1);
		job.setDetails(jd);
		jobDao.save(job);
		System.out.println("Done");
	}
	
}
