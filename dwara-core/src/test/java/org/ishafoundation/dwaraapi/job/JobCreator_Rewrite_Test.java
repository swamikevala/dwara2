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
public class JobCreator_Rewrite_Test extends JobCreator_Test{

	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Rewrite_Test.class);

	public JobCreator_Rewrite_Test() {
		action = Action.rewrite.name();
		requestInputFilePath = "/testcases/rewrite_request.json";
	}

	@Override
	protected RequestDetails getSystemrequestDetails() {
		RequestDetails details = new RequestDetails();
		String artifact_name = "";
		details.setArtifact_name(artifact_name);
		details.setFrom_volume_uid("V5B001"); // TODO how do we validate that the volume passed is only physical and not
		details.setTo_volume_uid("V5B002");

		return details;
	}
	
	@Test
	public void test_Rewrite() {
		try {
			createSingleSystemrequestAndJobs();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
