package org.ishafoundation.dwaraapi.job.creation;

import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Map_Tapedrives_Test extends JobCreator_Test{

	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Map_Tapedrives_Test.class);

	public JobCreator_Map_Tapedrives_Test() {
		action = Action.map_tapedrives.name();
	}
	
	@Test
	public void test_Map_TapeDrives() {
		try {
			jobCreator.createJobs(request, null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
