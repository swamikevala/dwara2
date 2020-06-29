package org.ishafoundation.dwaraapi.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.ingest.RequestParams;
import org.ishafoundation.dwaraapi.api.req.ingest.UserRequest;
import org.ishafoundation.dwaraapi.api.req.ingest.mapper.RequestToEntityObjectMapper;
import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificArtifactFactory;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ArtifactService {

	private static final Logger logger = LoggerFactory.getLogger(ArtifactService.class);

	@Autowired
	protected RequestDao requestDao;
	
	@Autowired
	protected JobCreator jobCreator;

	@Autowired
	private RequestToEntityObjectMapper requestToEntityObjectMapper; 
	
	@Autowired
	private DomainUtil domainUtil;

	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;

    public ResponseEntity<String> ingest(UserRequest userRequest){	
    	try {
	    	
		    	Request request = new Request();
				request.setActionId(Action.ingest);
		    	// request.setUser(userDao.findByName(requestedBy));
				// request.setUser(user);
				request.setRequestedAt(LocalDateTime.now());
				
				RequestDetails details = new RequestDetails();
				JsonNode postBodyJson = getRequestDetails(userRequest); 
				details.setBody(postBodyJson);
				request.setDetails(details);
				
		    	request = requestDao.save(request);
		    	int requestId = request.getId();
		    	logger.info("Request - " + requestId);
	
				// TODO - ??? - Artifactclass mismatch - api request passes string, while request.details has id...
		    	String artifactclassName = userRequest.getArtifactclass();
				Artifactclass artifactclass = (Artifactclass) dBMasterTablesCacheManager.getRecord(CacheableTablesList.artifactclass.name(), artifactclassName);
				Domain domain = artifactclass.getDomain();
		    	List<RequestParams> requestParamsList = userRequest.getArtifact();
		    	
		    	for (RequestParams requestParams : requestParamsList) {
					Request systemrequest = new Request();
					systemrequest.setRequestRef(request);
					systemrequest.setActionId(request.getActionId());
					systemrequest.setUser(request.getUser());
					systemrequest.setRequestedAt(LocalDateTime.now());
					

		    		RequestDetails systemrequestDetails = requestToEntityObjectMapper.getRequestDetails(requestParams);
		    		systemrequestDetails.setArtifactclass_id(artifactclass.getId()); // transitioning from global level on the request to artifact level...
					systemrequest.setDetails(systemrequestDetails);
					
					systemrequest = requestDao.save(systemrequest);
					logger.info("System request - " + systemrequest.getId());
					
					Artifact artifact = DomainSpecificArtifactFactory.getInstance(domain);
					artifact.setName(requestParams.getArtifact_name());
					artifact.setArtifactclass(artifactclass);
					artifact = (Artifact) domainUtil.getDomainSpecificArtifactRepository(domain).save(artifact);
					logger.debug(artifact.getClass().getSimpleName() + " - " + artifact.getId());
					
					// TODO - Pending Impl - File related changes go here...

					
					jobCreator.createJobs(systemrequest, artifact);
				}

			
		}catch (Exception e) {

		}

    	return ResponseEntity.status(HttpStatus.ACCEPTED).body("Done");
    }
	
	// TODO - Unnecessary conversion happening...
	private JsonNode getRequestDetails(UserRequest userRequest) {
		JsonNode postBodyJson = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			String postBody = mapper.writeValueAsString(userRequest);
			postBodyJson = mapper.readValue(postBody, JsonNode.class);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return postBodyJson;
	}
}

