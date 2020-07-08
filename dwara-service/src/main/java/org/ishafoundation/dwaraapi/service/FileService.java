package org.ishafoundation.dwaraapi.service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ishafoundation.dwaraapi.api.req.ingest.mapper.RequestToEntityObjectMapper;
import org.ishafoundation.dwaraapi.api.req.restore.FileParams;
import org.ishafoundation.dwaraapi.api.req.restore.UserRequest;
import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificArtifactFactory;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificFileFactory;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
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
public class FileService {

	private static final Logger logger = LoggerFactory.getLogger(FileService.class);

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

    public ResponseEntity<String> restore(UserRequest userRequest){	
    	try {
	    	
		    	Request request = new Request();
				request.setActionId(Action.restore);
		    	// request.setUser(userDao.findByName(requestedBy));
				// request.setUser(user);
				request.setRequestedAt(LocalDateTime.now());
				request.setDomain(userRequest.getDomain());
				RequestDetails details = new RequestDetails();
				JsonNode postBodyJson = getRequestDetails(userRequest); 
				details.setBody(postBodyJson);
				request.setDetails(details);
				
		    	request = requestDao.save(request);
		    	int requestId = request.getId();
		    	logger.info("Request - " + requestId);
	
			
				
		    	List<FileParams> fileParamsList = userRequest.getFileParams();
		    	
		    	for (FileParams fileParams : fileParamsList) {
					Request systemrequest = new Request();
					systemrequest.setRequestRef(request);
					systemrequest.setActionId(request.getActionId());
					systemrequest.setUser(request.getUser());
					systemrequest.setRequestedAt(LocalDateTime.now());
					systemrequest.setDomain(request.getDomain());

		    		RequestDetails systemrequestDetails = requestToEntityObjectMapper.getRequestDetails(fileParams);
		    		String requestedLocation = userRequest.getLocation();
		    		Location location = getLocation(requestedLocation);
					systemrequestDetails.setLocation_id(location.getId());
					systemrequestDetails.setOutput_folder(userRequest.getOutput_folder());
					systemrequestDetails.setDestinationpath(userRequest.getDestinationpath());
					systemrequestDetails.setVerify(userRequest.isVerify()); // overwriting default archiveformat.verify during restore

					systemrequest.setDetails(systemrequestDetails);
					systemrequest = requestDao.save(systemrequest);
					logger.info("System request - " + systemrequest.getId());
					
					
					jobCreator.createJobs(systemrequest, null);
				}

			
		}catch (Exception e) {
			e.printStackTrace();
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
	
	private Location getLocation(String requestedLocation) {
		// to get domaindefault we might need a util... or a query...
		Location location = null; // userRequest.getlocation(); null;// from user request
		if (requestedLocation != null) {
			location = (Location) dBMasterTablesCacheManager.getRecord(CacheableTablesList.location.name(), requestedLocation);
		}
		return location;
	}
}

