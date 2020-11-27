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
public class JobCreator_Ingest_VideoPub_Test2 extends DwaraService {
	
	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Ingest_VideoPub_Test2.class);
	
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
		URL fileUrl = this.getClass().getResource("/testcases/ingest/ingest_request_with2.json");
	
		String testIngestArtifactName1 =  "Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9";
		String artifact_name_1 = extractZip(testIngestArtifactName1);

		String testIngestArtifactName2 = "Shiva-Shambho_Everywhere_18-Nov-1980_Drone";
		String artifact_name_2 = extractZip(testIngestArtifactName2);

//		String testIngestArtifactName3 = "Cauvery-Calling_Day1-Sadhguru-Talking-With-People_Palace-Grounds-Bengaluru_02-Sep-2019_GoProApr6";
//		String artifact_name_3 = extractZip(testIngestArtifactName3);

		String postBodyJsonAsString = FileUtils.readFileToString(new File(fileUrl.getFile()));
		postBodyJsonAsString = postBodyJsonAsString.replace("<<artifact_name_1>>", artifact_name_1);
		postBodyJsonAsString = postBodyJsonAsString.replace("<<artifact_name_2>>", artifact_name_2);
//		postBodyJson = postBodyJson.replace("<<artifact_name_3>>", artifact_name_3);

		String artifactclassId =  "video-pub";
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
