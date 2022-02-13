package org.ishafoundation.dwaraapi.job.creation;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req.staged.ingest.IngestUserRequest;
import org.ishafoundation.dwaraapi.api.req.staged.ingest.StagedFile;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.JobUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.job.JobManager;
import org.ishafoundation.dwaraapi.resource.mapper.RequestToEntityObjectMapper;
import org.ishafoundation.dwaraapi.scheduler.ScheduledStatusUpdater;
import org.ishafoundation.dwaraapi.service.DwaraService;
import org.ishafoundation.dwaraapi.service.StagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.lingala.zip4j.ZipFile;

@Component
public class JobCreator_Ingest extends DwaraService {

	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Ingest.class);
	
	@Autowired
	private StagedService stagedService;
	
	@Autowired
	private RequestToEntityObjectMapper requestToEntityObjectMapper; 
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;

	@Autowired
	private JobCreator jobCreator;

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private JobDao jobDao;

	@Autowired
	private JobManager jobManager;
	
	@Autowired
	private ArtifactDao artifactDao;
	
	@Autowired
	private JobUtil jobUtil;
	
	@Autowired 
	private ScheduledStatusUpdater scheduledStatusUpdater;
	
	//String readyToIngestPath =  "C:\\data\\user\\pgurumurthy\\ingest\\pub-video";
	String readyToIngestPath =  "C:\\data\\staged";
	int artifactId = 0;
	
	
	protected String extractZip(String testIngestArtifactName, String seqCode) throws Exception {	
		
		URL fileUrl = JobCreator_Ingest_VideoPub_Test.class.getResource("/" + testIngestArtifactName + ".zip");
		ZipFile zipFile = new ZipFile(fileUrl.getFile());

		zipFile.extractAll(readyToIngestPath);
		
		String ingestFileSourcePath = readyToIngestPath + File.separator + testIngestArtifactName;
		String artifactNameToBeIngested = seqCode + "_" + testIngestArtifactName + "_" + System.currentTimeMillis(); // TO have the artifact name uniqued...
		
		String artifactPath = readyToIngestPath + File.separator +  artifactNameToBeIngested;
		FileUtils.moveDirectory(new File(ingestFileSourcePath), new File(artifactPath));
		return artifactNameToBeIngested;
		
	}
	
	protected List<Job> synchronousActionForRequest(String testIngestArtifactName1, String artifactclassId) throws Exception {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("pgurumurthy", "ShivaShambho"));
		
		ObjectMapper mapper = new ObjectMapper();
		URL fileUrl = this.getClass().getResource("/testcases/ingest/ingest_request.json");

		String artifactNameToBeIngested = testIngestArtifactName1;//extractZip(testIngestArtifactName1);
		
		String postBodyJsonAsString = FileUtils.readFileToString(new File(fileUrl.getFile()));
		postBodyJsonAsString = postBodyJsonAsString.replace("<<artifact_name_1>>", artifactNameToBeIngested);
		postBodyJsonAsString = postBodyJsonAsString.replace("<<artifact_class>>", artifactclassId);
		
		IngestUserRequest ingestUserRequest = mapper.readValue(postBodyJsonAsString, new TypeReference<IngestUserRequest>() {});
		
		Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
		//String readyToIngestPath =  artifactclass.getPathPrefix();
		Request userRequest = createUserRequest(Action.ingest, ingestUserRequest);
    	//int userRequestId = userRequest.getId();
    	
//    	List<StagedFile> stagedFileList = ingestUserRequest.getStagedFiles();
//    	for (StagedFile stagedFile : stagedFileList) {
		StagedFile stagedFile = ingestUserRequest.getStagedFiles().get(0);
			Request systemrequest = new Request();
			systemrequest.setType(RequestType.system);
			systemrequest.setRequestRef(userRequest);
			systemrequest.setStatus(Status.queued);
			systemrequest.setActionId(userRequest.getActionId());
			systemrequest.setRequestedBy(userRequest.getRequestedBy());
			systemrequest.setRequestedAt(LocalDateTime.now());
	
			RequestDetails systemrequestDetails = requestToEntityObjectMapper.getRequestDetailsForIngest(stagedFile);
			
			// transitioning from global level on the request to artifact level...
			systemrequestDetails.setArtifactclassId(artifactclassId); 
			
			systemrequest.setDetails(systemrequestDetails);
			
			systemrequest = requestDao.save(systemrequest);
			logger.info(DwaraConstants.SYSTEM_REQUEST + systemrequest.getId());
	
			File libraryFileInStagingDir = new File(readyToIngestPath + File.separator + stagedFile.getName());
	    	//Collection<java.io.File> libraryFileAndDirsList = FileUtils.listFilesAndDirs(libraryFileInStagingDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
	
			Artifact artifact = new Artifact();
			artifact.setWriteRequest(systemrequest);
			artifact.setQueryLatestRequest(systemrequest);
			artifact.setName(stagedFile.getName());
			artifact.setArtifactclass(artifactclass);
			artifact.setFileCount(5);
			artifact.setTotalSize(12345);
			String seqCode = StringUtils.substringBefore(stagedFile.getName(), "_");
			artifact.setSequenceCode(seqCode);
			artifact.setPrevSequenceCode(null);
			artifact = artifactDao.save(artifact);
			
			artifactId = artifact.getId();
			logger.info(artifact.getClass().getSimpleName() + " - " + artifact.getId());
			
	        stagedService.createFilesAndExtensions(readyToIngestPath, artifact, 12345, libraryFileInStagingDir, ".junk");
			
	        return jobCreator.createJobs(systemrequest, artifact);
//    	}
	}

	protected Job updateJobStatus(int jobId, Status status){
		Job job = jobDao.findById(jobId).get();
		return updateJobStatus(job, status);
	}

	protected Job updateJobStatus(Job job, Status status){
		job.setStatus(status);
		return jobDao.save(job);
	}

	protected Job completeJob(int jobId){
		Job job = jobDao.findById(jobId).get();
		return completeJob(job);
	}
	
	protected Job completeJob(Job job){
		return updateJobStatus(job, Status.completed);
	}
	
	protected List<Job> completeJobAndCreateDependentJobs(int jobId){
		Job job = jobDao.findById(jobId).get();
		return completeJobAndCreateDependentJobs(job);
	}
	
	protected List<Job> completeJobAndCreateDependentJobs(Job job){
		job = completeJob(job);
		return jobCreator.createDependentJobs(job);
	}
	
	protected void validateArchiveFlow(int checksumJobId, String groupVolumePrefix) {
		/*
		 * checksum not complete
				write1 complete
					sourcejob = write 1 - Dependant Group volume R1
					expected -	Restore 1
		 */		

		int nthCopy = 1;
		Job job = jobDao.findById(checksumJobId + nthCopy).get();
		job.setStatus(Status.completed);
		jobDao.save(job);
		List<Job> write1DependentJobList = jobCreator.createDependentJobs(job);
		Job restore1Job = write1DependentJobList.get(0);
		assertEquals(write1DependentJobList.size(), 1);
		assertEquals(restore1Job.getStoragetaskActionId() + ":" + restore1Job.getGroupVolume().getId(), "restore:" + groupVolumePrefix + nthCopy);

		/**
		 * checksum not complete
				but restore 1 complete
					No verify Job
		 */
		restore1Job.setStatus(Status.completed);
		jobDao.save(restore1Job);
		List<Job> restore1DependentJobList = jobCreator.createDependentJobs(restore1Job);
		assertEquals(restore1DependentJobList.size(), 0);
	
		/**
			wirte2 & write3 completes
			restore2 and restore 3 created
		 */			
		nthCopy = 2;
		job = jobDao.findById(checksumJobId + nthCopy).get();
		job.setStatus(Status.completed);
		jobDao.save(job);
		List<Job> write2DependentJobList = jobCreator.createDependentJobs(job);

		Job restore2Job = write2DependentJobList.get(0);
		assertEquals(write2DependentJobList.size(), 1);
		assertEquals(restore2Job.getStoragetaskActionId() + ":" + restore2Job.getGroupVolume().getId(), "restore:" + groupVolumePrefix + nthCopy);

		nthCopy = 3;
		job = jobDao.findById(checksumJobId + nthCopy).get();
		job.setStatus(Status.completed);
		jobDao.save(job);
		List<Job> write3DependentJobList = jobCreator.createDependentJobs(job);

		Job restore3Job = write3DependentJobList.get(0);
		assertEquals(write3DependentJobList.size(), 1);
		assertEquals(restore3Job.getStoragetaskActionId() + ":" + restore3Job.getGroupVolume().getId(), "restore:" + groupVolumePrefix + nthCopy);

		/**
			checksum completes after restore1 job completion but before restore2 and restore 3
				only verify 1 job created
		 */			
		job = jobDao.findById(checksumJobId).get();
		job.setStatus(Status.completed);
		jobDao.save(job);
		List<Job> checksumDependentJobList = jobCreator.createDependentJobs(job);
		assertEquals(checksumDependentJobList.size(), 1);
		Job checksumDependentJob= checksumDependentJobList.get(0);
		assertEquals(checksumDependentJob.getProcessingtaskId() + ":" + checksumDependentJob.getGroupVolume().getId(), "checksum-verify:" + groupVolumePrefix + "1");

		/**
			checksum already completed
				restore2 completes
					verify2 created
		 */
		
		restore2Job.setStatus(Status.completed);
		jobDao.save(restore2Job);
		List<Job> restore2DependentJobList = jobCreator.createDependentJobs(restore2Job);
		assertEquals(restore2DependentJobList.size(), 1);
		Job restore2DependentJob= restore2DependentJobList.get(0);
		assertEquals(restore2DependentJob.getProcessingtaskId() + ":" + restore2DependentJob.getGroupVolume().getId(), "checksum-verify:" + groupVolumePrefix + "2");

		restore3Job.setStatus(Status.completed);
		jobDao.save(restore3Job);
		List<Job> restore3DependentJobList = jobCreator.createDependentJobs(restore3Job);
		assertEquals(restore3DependentJobList.size(), 1);
		Job restore3DependentJob= restore3DependentJobList.get(0);
		assertEquals(restore3DependentJob.getProcessingtaskId() + ":" + restore3DependentJob.getGroupVolume().getId(), "checksum-verify:" + groupVolumePrefix + "3");
		
		// TODO - checksum completes after restore1 and restore2 job completion but before restore 3

		// marking the other processing tasks complete so we can call proxy job runs
		checksumDependentJob.setStatus(Status.completed);
		jobDao.save(checksumDependentJob);

		restore2DependentJob.setStatus(Status.completed);
		jobDao.save(restore2DependentJob);

		restore3DependentJob.setStatus(Status.completed);
		jobDao.save(restore3DependentJob);

	}
	
	//protected List<Job> callJobManagerAndStatusUpdater(Request systemrequest, int artifactId){
	protected List<Job> callJobManagerAndStatusUpdater(Job job){
		/**
		 * Now run the preservation-gen job so it generates output files
		 */
		jobManager.manageJobs();
		
		// The manager creates threads for jobs and let them get processed on their own threads and returns here. 
		// The server shutsdown when the call returns back here and the test completes - even when the spawned threads are still processing.
		// So sleeping some time so the job threads would have completed...
		try {
			Thread.sleep(20000); // sleeping for 20 secs before we do the expected DB verification
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Run the status update scheduler so the status of the proxy job gets updated
		scheduledStatusUpdater.updateTransactionalTablesStatus();
		
		return jobUtil.getDependentJobs(job);//jobDao.findAllByRequestIdAndInputArtifactId(systemrequest.getId(), artifactId);
	}
	
	
	protected String getExpected(Job job) {
		return job.getId() + ":" + job.getStoragetaskActionId()  + ":" + job.getProcessingtaskId() + ":" + (job.getGroupVolume() != null ? job.getGroupVolume().getId() : null);
	}
}
