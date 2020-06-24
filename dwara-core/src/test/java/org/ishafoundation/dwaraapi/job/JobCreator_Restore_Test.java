package org.ishafoundation.dwaraapi.job;

import java.io.IOException;
import java.time.LocalDateTime;

import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
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
import com.fasterxml.jackson.databind.ObjectMapper;

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
	}
	
	@Override
	protected RequestDetails getRequestDetails() {
		String postBodyJson = "";
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode postBody = null;
		try {
			postBody = mapper.readValue(postBodyJson, JsonNode.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		RequestDetails details = new RequestDetails();
		details.setBody(postBody);
		return details;
	}
	
	@Test
	public void test_c_Restore() {
		try {
			Action action = Action.restore;

			Location location = getLocation("userrequestgoeshere");

			Request systemrequest = new Request();
			systemrequest.setRequestRef(request);
			
			systemrequest.setAction(request.getAction());
			systemrequest.setDomain(request.getDomain());
			systemrequest.setRequestedAt(LocalDateTime.now());
			

			
			RequestDetails details = new RequestDetails();
			int file_id = 1;

			details.setFile_id(file_id);
			// details.setPriority(priority);
			details.setLocation_id(location.getId());
			details.setOutput_folder("some output_folder");
			details.setDestinationpath("some dest path");
			details.setVerify(false); // overwriting default archiveformat.verify during restore


			systemrequest.setDetails(details);
			requestDao.save(systemrequest);
			logger.debug("successfully tested json insert");

			jobCreator.createJobs(request, null);
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
