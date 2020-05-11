package org.ishafoundation.dwaraapi.controller;

import org.ishafoundation.dwaraapi.api.req.Format;
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
	public void format() {
		
		Format requestBody = new Format();
		requestBody.setBarcode("V5A999L7");
		requestBody.setType("LTO7");
		requestBody.setForce(false);
		
		tapeController.format(requestBody);
	}
}
