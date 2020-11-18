package org.ishafoundation.dwaraapi.job.creation;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req.ingest.UserRequest;
import org.ishafoundation.dwaraapi.api.req.staged.ingest.IngestUserRequest;
import org.ishafoundation.dwaraapi.api.req.staged.ingest.StagedFile;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.resource.mapper.RequestToEntityObjectMapper;
import org.ishafoundation.dwaraapi.service.DwaraService;
import org.ishafoundation.dwaraapi.service.StagedService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.lingala.zip4j.ZipFile;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Ingest_VideoPub_Test extends DwaraService {
	
	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Ingest_VideoPub_Test.class);
	
	@Autowired
	StagedService stagedService;
	
	@Autowired
	private RequestToEntityObjectMapper requestToEntityObjectMapper; 
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;

	@Autowired
	JobCreator jobCreator;

	@Autowired
	RequestDao requestDao;
	
	@Autowired
	JobDao jobDao;

	@Autowired
	DomainUtil domainUtil;
	
	//String readyToIngestPath =  "C:\\data\\user\\pgurumurthy\\ingest\\pub-video";
	String readyToIngestPath =  "C:\\data\\staged";
	int artifactId = 0;
	
	@Test
	public void test_a_ingest() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		URL fileUrl = this.getClass().getResource("/testcases/ingest/ingest_request.json");

		String testIngestArtifactName1 =  "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9";
		String artifactNameToBeIngested = extractZip(testIngestArtifactName1);
		
		String artifactclassId =  "video-pub";
		
		String postBodyJsonAsString = FileUtils.readFileToString(new File(fileUrl.getFile()));
		postBodyJsonAsString = postBodyJsonAsString.replace("<<artifact_name_1>>", artifactNameToBeIngested);
		postBodyJsonAsString = postBodyJsonAsString.replace("<<artifact_class>>", artifactclassId);
		
		IngestUserRequest ingestUserRequest = mapper.readValue(postBodyJsonAsString, new TypeReference<IngestUserRequest>() {});
		
		Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
		//String readyToIngestPath =  artifactclass.getPathPrefix();
		Domain domain = artifactclass.getDomain();

    	Request userRequest = new Request();
    	userRequest.setType(RequestType.user);
		userRequest.setActionId(Action.ingest);
		userRequest.setStatus(Status.queued);
    	//userRequest.setRequestedBy(getUserObjFromContext());
		userRequest.setRequestedAt(LocalDateTime.now());
		
		RequestDetails details = new RequestDetails();
		JsonNode postBodyJson = getRequestDetails(ingestUserRequest); 
		details.setBody(postBodyJson);
		userRequest.setDetails(details);
		
    	userRequest = requestDao.save(userRequest);
    	int userRequestId = userRequest.getId();
    	logger.info(DwaraConstants.USER_REQUEST + userRequestId);

    	
    	List<StagedFile> stagedFileList = ingestUserRequest.getStagedFiles();
    	for (StagedFile stagedFile : stagedFileList) {
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
	    	Collection<java.io.File> libraryFileAndDirsList = FileUtils.listFilesAndDirs(libraryFileInStagingDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
	
			Artifact artifact = domainUtil.getDomainSpecificArtifactInstance(domain);
			artifact.setWriteRequest(systemrequest);
			artifact.setqLatestRequest(systemrequest);
			artifact.setName(stagedFile.getName());
			artifact.setArtifactclass(artifactclass);
			artifact.setFileCount(5);
			artifact.setTotalSize(12345);
			artifact.setSequenceCode("V123");
			artifact.setPrevSequenceCode(null);
			artifact = (Artifact) domainUtil.getDomainSpecificArtifactRepository(domain).save(artifact);
			
			artifactId = artifact.getId();
			logger.info(artifact.getClass().getSimpleName() + " - " + artifact.getId());
			
	        stagedService.createFilesAndExtensions(readyToIngestPath, domain, artifact, 12345, libraryFileAndDirsList);
			
	        List<Job> jobList = jobCreator.createJobs(systemrequest, artifact);
	        
			for (Job job : jobList) {
				logger.info(job.getId() + ":" + job.getStoragetaskActionId()  + ":" + job.getProcessingtaskId() + ":" + (job.getGroupVolume() != null ? job.getGroupVolume().getId() : null));
			}
			test_b_checksum_complete_writes_still_in_progress_checksum_calls(jobList);
			test_c_checksum_complete_write1_complete_write2_still_in_progress_checksum_calls(jobList);
			test_d_checksum_complete_writes_complete_checksum_calls(jobList);
			test_e_checksum_in_progress_write1_complete_write1_calls(jobList);
			test_f_checksum_complete_write1_complete_write1_calls(jobList);

			test_z_proxy_complete_proxy_calls(jobList);

    	}
//		
//		Request systemrequest = requestDao.findById(4).get();
//				
//		Artifact dependentJobInputArtifact = null;
//		Domain[] domains = Domain.values();
//		for (Domain nthDomain : domains) {
//			dependentJobInputArtifact = domainUtil.getDomainSpecificArtifact(nthDomain, 3);
//			if(dependentJobInputArtifact != null) {
//				break;
//			}
//		}
//		List<Job> jobList = jobCreator.createJobs(systemrequest, dependentJobInputArtifact);
//		for (Job job : jobList) {
//			System.out.println(job.getId() + job.getProcessingtaskId() + job.getStoragetaskActionId());
//		}
	}

	//@Test
	public void test_b_checksum_complete_writes_still_in_progress_checksum_calls(List<Job> jobList) {
//		for (Job job : jobList) {
//			job.setStatus(Status.completed);
//			// TODO find how...
//			if("video-proxy-low-gen".equals(job.getProcessingtaskId()))
//				job.setOutputArtifactId(artifactId);
//			jobDao.save(job);
//		}
		
		Job checksumJob = jobDao.findById(jobList.get(0).getId()).get();
		checksumJob.setStatus(Status.completed);
		jobDao.save(checksumJob);
		
		jobCreator.createDependentJobs(checksumJob);
		
		// expected is no job created...
	}
	
	//@Test
	public void test_c_checksum_complete_write1_complete_write2_still_in_progress_checksum_calls(List<Job> jobList) {
		Job checksumJob = jobDao.findById(jobList.get(0).getId()).get();
		checksumJob.setStatus(Status.completed);
		jobDao.save(checksumJob);
		
		Job write1Job = jobDao.findById(jobList.get(1).getId()).get();
		write1Job.setStatus(Status.completed);
		jobDao.save(write1Job);
		
		jobCreator.createDependentJobs(checksumJob);
		
		// expected is restore1 job created...
	}
	
	//@Test
	public void test_d_checksum_complete_writes_complete_checksum_calls(List<Job> jobList) {
		Job checksumJob = jobDao.findById(jobList.get(0).getId()).get();
		checksumJob.setStatus(Status.completed);
		jobDao.save(checksumJob);
		
		Job write1Job = jobDao.findById(jobList.get(1).getId()).get();
		write1Job.setStatus(Status.completed);
		jobDao.save(write1Job);
		
		Job write2Job = jobDao.findById(jobList.get(2).getId()).get();
		write2Job.setStatus(Status.completed);
		jobDao.save(write2Job);

		jobCreator.createDependentJobs(checksumJob);
		
		// expected is restore1 & 2 job created...
	}	

	//@Test
	public void test_e_checksum_in_progress_write1_complete_write1_calls(List<Job> jobList) {
		Job checksumJob = jobDao.findById(jobList.get(0).getId()).get();
		checksumJob.setStatus(Status.in_progress);
		jobDao.save(checksumJob);
		
		Job write1Job = jobDao.findById(jobList.get(1).getId()).get();
		write1Job.setStatus(Status.completed);
		jobDao.save(write1Job);

		List<Job> dependentJobList = jobCreator.createDependentJobs(write1Job);
		Job restore1Job = dependentJobList.get(0);
		logger.info(restore1Job.getId() + "");
		jobDao.delete(restore1Job);
		// expected is restore1 job created... // NOTE restore is not dependent on checksum
	}
	
	//@Test
	public void test_f_checksum_complete_write1_complete_write1_calls(List<Job> jobList) {
		Job checksumJob = jobDao.findById(jobList.get(0).getId()).get();
		checksumJob.setStatus(Status.completed);
		jobDao.save(checksumJob);
		
		Job write1Job = jobDao.findById(jobList.get(1).getId()).get();
		write1Job.setStatus(Status.completed);
		jobDao.save(write1Job);

		List<Job> dependentJobList = jobCreator.createDependentJobs(write1Job);
		Job restore1Job = dependentJobList.get(0);
		// expected is restore1 job created...
		
		Job restore2Job = test_g_checksum_complete_write2_complete_write2_calls(jobList);
		
		
		test_h_checksum_complete_restores_complete_checksum_calls(jobList, restore1Job, restore2Job);
	}
	
	//@Test
	public Job test_g_checksum_complete_write2_complete_write2_calls(List<Job> jobList) {
		Job checksumJob = jobDao.findById(jobList.get(0).getId()).get();
		checksumJob.setStatus(Status.completed);
		jobDao.save(checksumJob);
		
		Job write2Job = jobDao.findById(jobList.get(2).getId()).get();
		write2Job.setStatus(Status.completed);
		jobDao.save(write2Job);

		List<Job> dependentJobList = jobCreator.createDependentJobs(write2Job);
		for (Job job : dependentJobList) {
			logger.info(job.getId() + ":" + job.getStoragetaskActionId()  + ":" + job.getProcessingtaskId() + ":" + (job.getGroupVolume() != null ? job.getGroupVolume().getId() : null));
		}
		
		Job restore2Job = dependentJobList.get(0);
		// expected is restore2 job created...
		checksum_in_progress_restore2_complete_restore2_calls(jobList, restore2Job);
		checksum_complete_restore2_complete_restore2_calls(jobList, restore2Job);
		
		return restore2Job;
	}
	
	public void checksum_in_progress_restore2_complete_restore2_calls(List<Job> jobList, Job restore2Job) {
		Job checksumJob = jobDao.findById(jobList.get(0).getId()).get();
		checksumJob.setStatus(Status.in_progress);
		jobDao.save(checksumJob);
		
		restore2Job.setStatus(Status.completed);
		jobDao.save(restore2Job);		
		
		List<Job> dependentJobList = jobCreator.createDependentJobs(restore2Job);
		for (Job job : dependentJobList) {
			logger.info(job.getId() + ":" + job.getStoragetaskActionId()  + ":" + job.getProcessingtaskId() + ":" + (job.getGroupVolume() != null ? job.getGroupVolume().getId() : null));
		}
		// expected is no job created...
	}
	
	public void checksum_complete_restore2_complete_restore2_calls(List<Job> jobList, Job restore2Job) {
		Job checksumJob = jobDao.findById(jobList.get(0).getId()).get();
		checksumJob.setStatus(Status.completed);
		jobDao.save(checksumJob);
		
		restore2Job.setStatus(Status.completed);
		jobDao.save(restore2Job);		
		
		List<Job> dependentJobList = jobCreator.createDependentJobs(restore2Job);
		for (Job job : dependentJobList) {
			logger.info(job.getId() + ":" + job.getStoragetaskActionId()  + ":" + job.getProcessingtaskId() + ":" + (job.getGroupVolume() != null ? job.getGroupVolume().getId() : null));
		}
		// expected is verify2 job created...
	}
	
	//@Test
	public void test_h_checksum_complete_restores_complete_checksum_calls(List<Job> jobList, Job restore1Job, Job restore2Job) {
		Job checksumJob = jobDao.findById(jobList.get(0).getId()).get();
		checksumJob.setStatus(Status.completed);
		jobDao.save(checksumJob);

		restore1Job.setStatus(Status.completed);
		jobDao.save(restore1Job);	
		
		restore2Job.setStatus(Status.completed);
		jobDao.save(restore2Job);	
		
		List<Job> dependentJobList = jobCreator.createDependentJobs(checksumJob);
		for (Job job : dependentJobList) {
			logger.info(job.getId() + ":" + job.getStoragetaskActionId()  + ":" + job.getProcessingtaskId() + ":" + (job.getGroupVolume() != null ? job.getGroupVolume().getId() : null));
		}
		// expected is 2 verify jobs created..
	}

	//@Test
	public void test_z_proxy_complete_proxy_calls(List<Job> jobList) {
		Job proxyJob = jobDao.findById(jobList.get(3).getId()).get();
		proxyJob.setStatus(Status.completed);
		proxyJob.setOutputArtifactId(artifactId);
		jobDao.save(proxyJob);

		List<Job> dependentJobList = jobCreator.createDependentJobs(proxyJob);
		for (Job job : dependentJobList) {
			logger.info(job.getId() + ":" + job.getStoragetaskActionId()  + ":" + job.getProcessingtaskId() + ":" + (job.getGroupVolume() != null ? job.getGroupVolume().getId() : null));
		}
		
		System.out.println("breakpoint here");
		
		// expected is mam job, proxy checksum job and write jobs created..
	}

	//@Test
	public void test_b_Ingest() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			URL fileUrl = this.getClass().getResource("/testcases/ingest/ingest_request.json");

			String testIngestArtifactName1 =  "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9";
			String artifact_name_1 = extractZip(testIngestArtifactName1);
			
			String testIngestArtifactName2 = "Shiva-Shambho_Everywhere_18-Nov-1980_Drone";
			String artifact_name_2 = extractZip(testIngestArtifactName2);

//			String testIngestArtifactName3 = "Cauvery-Calling_Day1-Sadhguru-Talking-With-People_Palace-Grounds-Bengaluru_02-Sep-2019_GoProApr6";
//			String artifact_name_3 = extractZip(testIngestArtifactName3);

			String postBodyJson = FileUtils.readFileToString(new File(fileUrl.getFile()));
			postBodyJson = postBodyJson.replace("<<artifact_name_1>>", artifact_name_1);
			postBodyJson = postBodyJson.replace("<<artifact_name_2>>", artifact_name_2);
//			postBodyJson = postBodyJson.replace("<<artifact_name_3>>", artifact_name_3);
			
			UserRequest ur = mapper.readValue(postBodyJson, new TypeReference<UserRequest>() {});
			//artifactService.ingest(ur);
			
			// Delete the files after the creation is done...
//			FileUtils.deleteDirectory(new File(readyToIngestPath + File.separator + artifact_name_1));
//			FileUtils.deleteDirectory(new File(readyToIngestPath + File.separator + artifact_name_2));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private String extractZip(String testIngestArtifactName) throws Exception {	
		
		URL fileUrl = JobCreator_Ingest_VideoPub_Test.class.getResource("/" + testIngestArtifactName + ".zip");
		ZipFile zipFile = new ZipFile(fileUrl.getFile());

		zipFile.extractAll(readyToIngestPath);
		
		String ingestFileSourcePath = readyToIngestPath + File.separator + testIngestArtifactName;
		String artifactNameToBeIngested = testIngestArtifactName + "_" + System.currentTimeMillis(); // TO have the artifact name uniqued...
		
		String artifactPath = readyToIngestPath + File.separator +  artifactNameToBeIngested;
		FileUtils.moveDirectory(new File(ingestFileSourcePath), new File(artifactPath));
		return artifactNameToBeIngested;
		
	}
}
