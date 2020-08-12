package org.ishafoundation.dwaraapi.job;

import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Storagesubtype;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Import_Test extends JobCreator_Test{
	
	public JobCreator_Import_Test() {
		action = "import";
		requestInputFilePath = "/testcases/import_request.json";
	}
	
	@Override
	protected RequestDetails getSystemrequestDetails() {
		
		RequestDetails details = new RequestDetails();
		details.setVolume_id("V4A999"); // TODO how do we validate that the volume passed is only physical and not
		// group the volume belongs to
		details.setVolume_group_id("V4A");
		details.setStoragesubtype("LTO-7");
		return details;
	}

	@Test
	public void test_Import() {
		try {
			createSingleSystemrequestAndJobs();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
