package org.ishafoundation.dwaraapi.testsuite;

import org.ishafoundation.dwaraapi.controller.JobManagerTest;
import org.ishafoundation.dwaraapi.controller.TapeControllerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TapeControllerTest.class, JobManagerTest.class})
public class FormatTestSuite {

}
