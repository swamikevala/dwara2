package org.ishafoundation.dwaraapi.controller;

import org.ishafoundation.dwaraapi.entrypoint.resource.controller.TapelibraryController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TapelibraryControllerTest {

	@Autowired 
	TapelibraryController tapelibraryController;
	
	@Test
	@WithMockUser(username = "pgurumurthy", password = "pwd")
	public void triggerMapDrives() {
		tapelibraryController.mapDrives();
	}
}
