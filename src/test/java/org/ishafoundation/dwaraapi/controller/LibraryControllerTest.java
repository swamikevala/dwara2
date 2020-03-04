package org.ishafoundation.dwaraapi.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LibraryControllerTest {

	@Autowired 
	IngestController libraryController;
	
	@Test
	public void ingest() {
		//libraryController.ingest(14001, "C:\\data\\user\\pgurumurthy\\ingest\\pub-video", "14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A", "14715_Shivanga-Gents_Sharing_Tamil_Avinashi_10-Dec-2017_Panasonic-AG90A", "", -1, -1, -1);
		String libraryNameToBeIngested = "99999_Shivanga-Ladies_Sharing_English_Avinashi_10-Dec-2017_Panasonic-AG90A";
		org.ishafoundation.dwaraapi.api.req.ingest.UserRequest userRequest = new org.ishafoundation.dwaraapi.api.req.ingest.UserRequest();
		
		libraryController.ingest(userRequest);
	}

}
