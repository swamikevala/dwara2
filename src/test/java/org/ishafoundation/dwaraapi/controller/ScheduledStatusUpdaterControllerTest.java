package org.ishafoundation.dwaraapi.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ScheduledStatusUpdaterControllerTest {
	
	@Autowired 
	private ScheduledStatusUpdaterController scheduledStatusUpdaterController;
	
	@Test
	public void updateStatus() {
		scheduledStatusUpdaterController.updateStatus();
	}

}
