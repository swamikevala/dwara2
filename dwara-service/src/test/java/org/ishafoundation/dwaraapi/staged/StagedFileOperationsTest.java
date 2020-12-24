package org.ishafoundation.dwaraapi.staged;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StagedFileOperationsTest {
	
	@Autowired 
	private StagedFileOperations stagedFileOperations;
	
	@Test
	public void updateStatus() {
		stagedFileOperations.setPermissions("/data/user/pgurumurthy/ingest/video-pub", "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_91589095983567");
	}

}
