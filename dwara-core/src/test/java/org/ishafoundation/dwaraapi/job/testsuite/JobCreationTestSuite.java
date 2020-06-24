package org.ishafoundation.dwaraapi.job.testsuite;

import org.ishafoundation.dwaraapi.job.JobCreator_Finalize_Test;
import org.ishafoundation.dwaraapi.job.JobCreator_Format_Test;
import org.ishafoundation.dwaraapi.job.JobCreator_Ingest_Test;
import org.ishafoundation.dwaraapi.job.JobCreator_Map_Tapedrives_Test;
import org.ishafoundation.dwaraapi.job.JobCreator_Restore_Test;
import org.ishafoundation.dwaraapi.job.JobCreator_Rewrite_Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ JobCreator_Map_Tapedrives_Test.class, JobCreator_Format_Test.class, JobCreator_Ingest_Test.class, JobCreator_Restore_Test.class, JobCreator_Finalize_Test.class, JobCreator_Rewrite_Test.class})
public class JobCreationTestSuite {

}
