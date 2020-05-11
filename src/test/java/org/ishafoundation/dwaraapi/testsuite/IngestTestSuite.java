package org.ishafoundation.dwaraapi.testsuite;

import org.ishafoundation.dwaraapi.controller.JobManagerTest;
import org.ishafoundation.dwaraapi.controller.LibraryControllerTest;
import org.ishafoundation.dwaraapi.controller.ScheduledStatusUpdaterControllerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ LibraryControllerTest.class, JobManagerTest.class, ScheduledStatusUpdaterControllerTest.class})
public class IngestTestSuite {

}
