package org.ishafoundation.dwaraapi.testinit;

import org.ishafoundation.dwaraapi.utils.IngestSetupUtil;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IngestSetup {

	protected static org.ishafoundation.dwaraapi.api.req.ingest.UserRequest userRequest = null;
	
	@BeforeClass
	public static void setup() throws Exception {
		userRequest = IngestSetupUtil.setupLibraryForIngest();
	}
	

}
