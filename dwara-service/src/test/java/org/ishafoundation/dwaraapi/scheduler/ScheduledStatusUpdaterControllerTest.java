package org.ishafoundation.dwaraapi.scheduler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ScheduledStatusUpdaterControllerTest {
	
	@Autowired 
	private ScheduledStatusUpdater scheduledStatusUpdaterController;
	
	@Test
	public void updateStatus() {
		scheduledStatusUpdaterController.updateTransactionalTablesStatus();
	}

}
