package org.ishafoundation.dwaraapi.job;

import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Format_Test extends JobCreator_Test{

	public JobCreator_Format_Test() {
		action = Action.format.name();
		requestInputFilePath = "/testcases/format_request.json";
	}
	
	@Override
	protected RequestDetails getSystemrequestDetails() {
		RequestDetails details = new RequestDetails();
		details.setVolume_uid("V4A002"); // TODO how do we validate that the volume passed is only physical and not
		// group the volume belongs to
		details.setVolume_group_uid("V4A");
		details.setGeneration(7);
		details.setForce(false);
		return details;
	}

	@Test
	public void test_Format() {
		try {
			createSingleSystemrequestAndJobs();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
