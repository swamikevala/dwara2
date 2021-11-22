package org.ishafoundation.dwaraapi.job.creation;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.service.StagedService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Ingest_UseJobId_Test{

	@Autowired
	StagedService artifactService;

	@Autowired
	JobCreator jobCreator;

	@Autowired
	RequestDao requestDao;
	
	@Autowired
	JobDao jobDao;

	@Autowired
	ArtifactDao artifactDao;
	
	//String readyToIngestPath =  "C:\\data\\user\\pgurumurthy\\ingest\\pub-video";
	String readyToIngestPath =  "C:\\data\\ingested";
	
	//@Test
	public void test_a_create() {
		Request systemrequest = requestDao.findById(26).get();
				
		Artifact dependentJobInputArtifact = artifactDao.findById(16).get();
		List<Job> jobList = jobCreator.createJobs(systemrequest, dependentJobInputArtifact);
		for (Job job : jobList) {
			System.out.println(job.getId() + job.getProcessingtaskId() + job.getStoragetaskActionId());
		}
	}
	
	@Test
	public void test_ab_updateStatus() {
//		Job job = jobDao.findById(5).get();
//		job.setStatus(Status.completed);
//		jobDao.save(job);
//		jobCreator.createDependentJobs(job);
//		
//		job = jobDao.findById(6).get();
//		job.setStatus(Status.completed);
//		jobDao.save(job);
//		jobCreator.createDependentJobs(job);
//
//		job = jobDao.findById(7).get();
//		job.setStatus(Status.completed);
//		jobDao.save(job);
//		jobCreator.createDependentJobs(job);
//		
//		job = jobDao.findById(8).get();
//		job.setStatus(Status.completed);
//		jobDao.save(job);
//		jobCreator.createDependentJobs(job);
		
		Job job = jobDao.findById(10).get();
		job.setStatus(Status.completed);
		jobDao.save(job);
		jobCreator.createDependentJobs(job);

//		job = jobDao.findById(10).get();
//		job.setStatus(Status.completed);
//		jobDao.save(job);
//		jobCreator.createDependentJobs(job);
//		
//		job = jobDao.findById(11).get();
//		job.setStatus(Status.completed);
//		jobDao.save(job);
//		jobCreator.createDependentJobs(job);

//		
//		job = jobDao.findById(29).get();
//		job.setStatus(Status.completed);
//		jobDao.save(job);
//		jobCreator.createDependentJobs(job);
		
//		Job checksumJob = jobDao.findById(77).get();
//		checksumJob.setStatus(Status.completed);
//		jobDao.save(checksumJob);
		
//		Job write1Job = jobDao.findById(78).get();
//		write1Job.setStatus(Status.completed);
//		jobDao.save(write1Job);
		
//		Job write2Job = jobDao.findById(79).get();
//		write2Job.setStatus(Status.completed);
//		jobDao.save(write2Job);

//		Job restore1Job = jobDao.findById(80).get();
//		restore1Job.setStatus(Status.completed);
//		jobDao.save(restore1Job);
		
//		Job restore2Job = jobDao.findById(81).get();
//		restore2Job.setStatus(Status.completed);
//		jobDao.save(restore2Job);
		
//		Job proxyJob = jobDao.findById(65).get();
//		proxyJob.setStatus(Status.completed);
//		proxyJob.setOutputArtifactId(4);
//		jobDao.save(proxyJob);


	}	

}
