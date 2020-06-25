package org.ishafoundation.dwaraapi.job;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.DomainAttributeConverter;
import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificArtifactFactory;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobCreator_Ingest_Test extends JobCreator_Test{

	private static final Logger logger = LoggerFactory.getLogger(JobCreator_Ingest_Test.class);


	@SuppressWarnings("rawtypes")
	@Autowired
	private Map<String, ArtifactRepository> artifactDaoMap;

	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;

	@Autowired
	private DomainAttributeConverter domainAttributeConverter;

	public JobCreator_Ingest_Test() {
		action = Action.ingest;
	}
	
	@Override
	protected RequestDetails getRequestDetails() {
		String postBodyJson = "";
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode postBody = null;
		try {
			postBody = mapper.readValue(postBodyJson, JsonNode.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		RequestDetails details = new RequestDetails();
		details.setBody(postBody);
		//return details;
		return null;
	}

	@Test
	public void test_b_Ingest() {
		try {
			Request systemrequest = new Request();
			systemrequest.setRequestRef(request);
			
			systemrequest.setAction(request.getAction());
			systemrequest.setDomain(request.getDomain());
			systemrequest.setRequestedAt(LocalDateTime.now());
			

			String artifact_name = "10058_Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9"
					+ System.currentTimeMillis();

			RequestDetails details = new RequestDetails();
			details.setArtifactclass_id(1);
			details.setSourcepath("some sourcepath");
			details.setArtifact_name(artifact_name);
			details.setPrev_sequence_code("some prev_sequence_code");

			systemrequest.setDetails(details);
			
			systemrequest = requestDao.save(systemrequest);
			logger.debug("successfully tested json insert");


			
			Artifactclass artifactclass = (Artifactclass) dBMasterTablesCacheManager
					.getRecord(CacheableTablesList.artifactclass.name(), "pub-video");

			logger.debug("successfully tested config table caching");

			String domainAsString = domainAttributeConverter.convertToDatabaseColumn(request.getDomain());
			String domainSpecificArtifactName = "artifact" + domainAsString;
			Artifact artifact = DomainSpecificArtifactFactory.getInstance(domainSpecificArtifactName);
			artifact.setName(
					"10058_Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9" + System.currentTimeMillis());
			artifact.setArtifactclass(artifactclass);
			artifactDaoMap.get(domainSpecificArtifactName + "Dao").save(artifact);

			// TODO File related changes go here...

			logger.debug("successfully tested domain specific table testing");
			jobCreator.createJobs(request, artifact);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
