package org.ishafoundation.dwaraapi.job;

import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
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
public class JobCreator_Restore_Test extends JobCreator_Test {

	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Restore_Test.class);

	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;

	public JobCreator_Restore_Test() {
		action = Action.restore;
		requestInputFilePath = "/testcases/restore_request.json";
	}
	
	@Override
	protected RequestDetails getSystemrequestDetails() {
		int file_id = 1;
		Location location = getLocation("userrequestgoeshere");
		
		RequestDetails details = new RequestDetails();
		details.setFile_id(file_id);
		// details.setPriority(priority);
		details.setLocation_id(location.getId());
		details.setOutput_folder("some output_folder");
		details.setDestinationpath("some dest path");
		details.setVerify(false); // overwriting default archiveformat.verify during restore
		return details;
	}
	
	@Test
	public void test_c_Restore() {
		try {
			createSingleSystemrequestAndJobs();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}


	private Location getLocation(String userRequest) {
		// to get domaindefault we might need a util... or a query...
		Location location = null; // userRequest.getlocation(); null;// from user request
		if (location == null) {
			location = (Location) dBMasterTablesCacheManager.getRecord(CacheableTablesList.location.name(), "LR"); // defaulting
			// to
			// the
			// domain
			// configured
			// as
			// default...
		}
		return location;
	}

}
