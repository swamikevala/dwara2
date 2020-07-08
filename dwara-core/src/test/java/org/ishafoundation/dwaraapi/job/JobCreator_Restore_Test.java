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

import com.fasterxml.jackson.databind.JsonNode;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Restore_Test extends JobCreator_Test {

	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Restore_Test.class);

	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;

	public JobCreator_Restore_Test() {
		action = Action.restore.name();
		requestInputFilePath = "/testcases/restore_request.json";
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
	
	@Override
	protected RequestDetails getSystemrequestDetails() {

		JsonNode body = request.getDetails().getBody();
		String requestedLocation = body.get("location").textValue();
		String outputFolder = body.get("output_folder").textValue();
		String destinationpath = body.get("destinationpath").textValue();
		boolean verify = body.get("verify").booleanValue();
		Location location = getLocation(requestedLocation);
		
//		Iterator<JsonNode> fileParams = body.get("fileParams").elements();
//		while (fileParams.hasNext()) {
//			JsonNode fileParamsJsonNode = (JsonNode) fileParams.next();
//			int fileId = fileParamsJsonNode.get("file_id").intValue();
//
//		}
		
		JsonNode fileParamsJsonNode = body.get("fileParams").get(0);
		int fileId = fileParamsJsonNode.get("file_id").intValue();		
		
		RequestDetails details = new RequestDetails();
		details.setFile_id(fileId);
		// details.setPriority(priority);
		details.setLocation_id(location.getId());
		details.setOutput_folder(outputFolder);
		details.setDestinationpath(destinationpath);
		details.setVerify(verify); // overwriting default archiveformat.verify during restore
		return details;
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
