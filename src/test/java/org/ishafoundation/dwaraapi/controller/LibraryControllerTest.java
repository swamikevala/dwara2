package org.ishafoundation.dwaraapi.controller;

import org.ishafoundation.dwaraapi.api.exception.DwaraException;
import org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails;
import org.ishafoundation.dwaraapi.entrypoint.resource.controller.LibraryController;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LibraryControllerTest extends IngestSetup{

	@Autowired 
	LibraryController libraryController;

	@Test
	@WithMockUser(username = "pgurumurthy", password = "pwd")
	public void ingest() throws Exception{
		ResponseEntity<RequestWithSubrequestDetails> response = libraryController.ingest(userRequest);
		
		// TODO validate response here...
		HttpStatus statusCode = response.getStatusCode();
		Assert.assertEquals(202, response.getStatusCodeValue());
		// assert statuscode = 202
		RequestWithSubrequestDetails respBody = response.getBody();
		Assert.assertEquals(true, response.getBody().getLibraryId() > 0);
		System.out.println("Ingested successfully");
		// TODO Also assert important parts of response...
		
		// verify DB data here...
	}
}
