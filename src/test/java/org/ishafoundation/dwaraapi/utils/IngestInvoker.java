package org.ishafoundation.dwaraapi.utils;

import org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails;
import org.ishafoundation.dwaraapi.entrypoint.resource.controller.LibraryController;
import org.ishafoundation.dwaraapi.testinit.IngestSetup;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IngestInvoker extends IngestSetup{

	@Autowired 
	LibraryController libraryController;

	public ResponseEntity<RequestWithSubrequestDetails> invokeIngest() throws Exception{
		return libraryController.ingest(userRequest);
	}
}
