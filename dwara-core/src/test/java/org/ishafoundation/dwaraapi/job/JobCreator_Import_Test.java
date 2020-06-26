package org.ishafoundation.dwaraapi.job;

import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Import_Test extends JobCreator_Test{
	
	public JobCreator_Import_Test() {
		action = Action.import_;
	}
}
