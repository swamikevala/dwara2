package org.ishafoundation.dwaraapi.job.creation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Ingest_VideoDigitizationPub_Test extends JobCreator_Ingest {
	
	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Ingest_VideoDigitizationPub_Test.class);
	
	/*
		1
		2
 
		1 - complete
		jobmanager
		scheduler
			3 checksum-gen
				4 mxf - complete
					5 W 1
					6 W 2
					7 W 3
					8 proxy
						9 R 1
						10 CSV 1
						jobmanager
						scheduler
						11 : Mam
						12 : checksum-gen
						13 : w1
						14 : w2
						15 : w3
	 */

	@Test
	@Sql(scripts = {"/data/sql/truncate_transaction_tables.sql","/data/sql/flowelement_video-digitization-pub.sql"})
	public void test_a_ingest() throws Exception {
		int hdrNftrExtractionJobId = 1;
		int rawCheksumGenJobId = 3;
		int rawW1JobId = 5;
		int proxyJobId = 8;
		int proxyArchiveFlowJobId = 12;
		
		String testIngestArtifactName1 =  "prasad-artifact-1";
		String artifact_name_1 = extractZip(testIngestArtifactName1,"D1");
		String artifactclassId =  "video-digi-2020-pub";

		List<Job> jobList = synchronousActionForRequest(artifact_name_1, artifactclassId);
        
        assertEquals(jobList.size(), 2);
		for (Job job : jobList) {
			String expected = job.getId() + ":" + job.getStoragetaskActionId()  + ":" + job.getProcessingtaskId() + ":" + (job.getGroupVolume() != null ? job.getGroupVolume().getId() : null);
			logger.info("---" + expected);
			String actual = "";
			//assertEquals(expected, actual);
		}
		
		// 1 complete - Header and footer extraction - so we can run preservation call using JobManager
		// TODO: What if this job is not complete but just ffv1 generation is complete we should not move to the next step? 
		Job job = completeJob(hdrNftrExtractionJobId);
		
		// 2 - run preservation call using JobManager
		List<Job> dependentJobList = callJobManagerAndStatusUpdater(job.getRequest(), artifactId);
		for (Job nthDependentJob : dependentJobList) {
			String expected = getExpected(nthDependentJob);
			logger.info("***" + expected);
			String actual = "";
			//assertEquals(expected, actual);
		}
		
		// 3 checksum-gen - generates mxfexclusion job
		List<Job> checksumGenDependentJobList = completeJobAndCreateDependentJobs(rawCheksumGenJobId);
		assertEquals(checksumGenDependentJobList.size(), 1);

		// 4
		Job mxfExclusionJob = checksumGenDependentJobList.get(0);
		List<Job> mxfExclusionDependentJobList = completeJobAndCreateDependentJobs(mxfExclusionJob);
		assertEquals(mxfExclusionDependentJobList.size(), 4);
		for (Job nthDependentJob : mxfExclusionDependentJobList) {
			String expected = getExpected(nthDependentJob);
			logger.info("***" + expected);
			String actual = "";
			//assertEquals(expected, actual);
		}
		
		// 5 through 8 
		int nthCopy = 1;
		String groupVolumePrefix = "R";
		List<Job> write1DependentJobList = completeJobAndCreateDependentJobs(rawW1JobId);
		assertEquals(write1DependentJobList.size(), 1);
		Job restore1Job = write1DependentJobList.get(0);
		assertEquals(restore1Job.getStoragetaskActionId() + ":" + restore1Job.getGroupVolume().getId(), "restore:" + groupVolumePrefix + nthCopy);


		List<Job> restore1DependentJobList = completeJobAndCreateDependentJobs(restore1Job);
		assertEquals(restore1DependentJobList.size(), 1);
		Job restore1DependentJob= restore1DependentJobList.get(0);
		assertEquals(restore1DependentJob.getProcessingtaskId() + ":" + restore1DependentJob.getGroupVolume().getId(), "checksum-verify:" + groupVolumePrefix + "1");

		// complete the verify job created so we can run proxy
		completeJob(restore1DependentJob);

		/**
		 * Now run proxy job so it generates output aritifact
		 */
		// proxy job is on_hold now release it first 
		updateJobStatus(proxyJobId, Status.queued);		
		callJobManagerAndStatusUpdater(job.getRequest(), artifactId + 1);
		
		// TODO - call archive flow...
		validateArchiveFlow(proxyArchiveFlowJobId, "G");
		//assertEquals(mxfExclusionJob.getStoragetaskActionId() + ":" + restore1Job.getGroupVolume().getId(), "restore:" + groupVolumePrefix + nthCopy);
	}
}
