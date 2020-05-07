package org.ishafoundation.dwaraapi.controller;

import org.ishafoundation.dwaraapi.entrypoint.resource.controller.TapeController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TapeControllerTest {

	@Autowired 
	TapeController tapeController;
	
	@Test
	@WithMockUser(username = "pgurumurthy", password = "pwd")
	public void triggerMapDrives() {
		tapeController.writeLabel("V5A001");
	}
}
