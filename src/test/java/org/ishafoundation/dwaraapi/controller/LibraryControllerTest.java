package org.ishafoundation.dwaraapi.controller;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.exception.DwaraException;
import org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LibraryControllerTest {

	@Autowired 
	LibraryController libraryController;
	
	@Test
	@WithMockUser(username = "pgurumurthy", password = "pwd")
	public void ingest() {
		String sourcePath =  "C:\\data\\user\\pgurumurthy\\ingest\\pub-video";
		String libraryNameToBeIngested = "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9";
		
		List<LibraryParams> libraryParamsList = new ArrayList<LibraryParams>();
		
		LibraryParams libraryParams = new LibraryParams();
		libraryParams.setSourcePath(sourcePath);
		libraryParams.setName(libraryNameToBeIngested);
		
		libraryParamsList.add(libraryParams);
		
		org.ishafoundation.dwaraapi.api.req.ingest.UserRequest userRequest = new org.ishafoundation.dwaraapi.api.req.ingest.UserRequest();
		userRequest.setLibraryclass("pub-video");
		userRequest.setLibrary(libraryParamsList);
		
		ResponseEntity<RequestWithSubrequestDetails> response = null;
		try {
			response = libraryController.ingest(userRequest);
		}catch (DwaraException e) {
			// since we are invoking the method straight... we need to catch the exception.
			
			System.err.println(e.getDetails());
		}
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
