package org.ishafoundation.dwaraapi.job;

import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Finalize_Test extends JobCreator_Test{

	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Finalize_Test.class);
	
	public JobCreator_Finalize_Test() {
		action = Action.finalize.name();
		//requestInputFilePath = "/testcases/finalize/finalize_request.json";
	}
	
	@Override
	protected RequestDetails getSystemrequestDetails() {
		RequestDetails details = new RequestDetails();
		details.setVolumeId("V4A002");
		return details;
	}

	@Test
	public void test_Finalise() {
		try {
			createSingleSystemrequestAndJobs();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
