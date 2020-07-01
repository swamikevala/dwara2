package org.ishafoundation.dwaraapi.job;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.json.JobDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DaoTests{
	
	@Autowired
	private RequestDao requestDao;	
	
	@Autowired
	private JobDao jobDao;
	
	@Test
	public void testRequestDao() {
		List<Status> statusList = new ArrayList<Status>();
		statusList.add(Status.queued);
		statusList.add(Status.in_progress);
		
		List<Action> actionList = new ArrayList<Action>();
		actionList.add(Action.map_tapedrives);
		actionList.add(Action.format);
		
		long tdOrFormatJobInFlight = requestDao.countByActionIdInAndStatus(actionList, Status.in_progress);
		System.out.println(tdOrFormatJobInFlight);
	}
	
	public void testJobDao() {		
		Job job = new Job();
		

		JobDetails jd = new JobDetails();
		jd.setDevice_id(1);
		job.setDetails(jd);
		jobDao.save(job);
		System.out.println("Done");
	}
	
}
