package org.ishafoundation.dwaraapi;

import org.ishafoundation.dwaraapi.controller.JobManagerTest;
import org.ishafoundation.dwaraapi.controller.LibraryControllerTest;
import org.ishafoundation.dwaraapi.controller.TapelibraryControllerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ LibraryControllerTest.class, TapelibraryControllerTest.class, JobManagerTest.class, TapelibraryControllerTest.class})
public class MapDriveTestSuite {

}
