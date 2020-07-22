package org.ishafoundation.dwaraapi.service;

import java.io.IOException;
import java.time.LocalDateTime;

import org.ishafoundation.dwaraapi.api.req.format.FormatRequest;
import org.ishafoundation.dwaraapi.api.req.ingest.mapper.RequestToEntityObjectMapper;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
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
public class VolumeService {

	private static final Logger logger = LoggerFactory.getLogger(VolumeService.class);

	@Autowired
	protected RequestDao requestDao;

	@Autowired
	protected JobCreator jobCreator;

	@Autowired
	private RequestToEntityObjectMapper requestToEntityObjectMapper; 

	public ResponseEntity<String> format(FormatRequest formatRequest){	
		try {

			Request request = new Request();
			request.setActionId(Action.format);
			// request.setUser(userDao.findByName(requestedBy));
			// request.setUser(user);
			request.setRequestedAt(LocalDateTime.now());
			RequestDetails details = new RequestDetails();
			JsonNode postBodyJson = getRequestDetails(formatRequest); 
			details.setBody(postBodyJson);
			request.setDetails(details);

			request = requestDao.save(request);
			int requestId = request.getId();
			logger.info("Request - " + requestId);


			Request systemrequest = new Request();
			systemrequest.setRequestRef(request);
			systemrequest.setActionId(request.getActionId());
			systemrequest.setUser(request.getUser());
			systemrequest.setRequestedAt(LocalDateTime.now());
			systemrequest.setDomain(request.getDomain());

			RequestDetails systemrequestDetails = requestToEntityObjectMapper.getRequestDetails(formatRequest);

			systemrequest.setDetails(systemrequestDetails);
			systemrequest = requestDao.save(systemrequest);
			logger.info("System request - " + systemrequest.getId());


			jobCreator.createJobs(systemrequest, null);
		}catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Done");
	}

	// TODO : Why domain needed? Filevolume and Artifactvolume needed for generating the index are domain-ed
	public ResponseEntity<String> finalize(String volumeUid, Domain domain){

		try {

			Request request = new Request();
			request.setActionId(Action.finalize);
			// request.setUser(userDao.findByName(requestedBy));
			// request.setUser(user);
			request.setRequestedAt(LocalDateTime.now());
			request.setDomain(domain);
			RequestDetails details = new RequestDetails();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode postBodyJson = mapper.readValue("{\"volume_uid\":\""+volumeUid+"\"}", JsonNode.class);
			details.setBody(postBodyJson);
			request.setDetails(details);

			request = requestDao.save(request);
			int requestId = request.getId();
			logger.info("Request - " + requestId);


			Request systemrequest = new Request();
			systemrequest.setRequestRef(request);
			systemrequest.setActionId(request.getActionId());
			systemrequest.setUser(request.getUser());
			systemrequest.setRequestedAt(LocalDateTime.now());
			systemrequest.setDomain(request.getDomain());

			RequestDetails systemrequestDetails = new RequestDetails();
			systemrequestDetails.setVolume_uid(volumeUid);
			systemrequest.setDetails(systemrequestDetails);
			systemrequest = requestDao.save(systemrequest);
			logger.info("System request - " + systemrequest.getId());


			jobCreator.createJobs(systemrequest, null);
		}catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Done");
	}

	// TODO - Unnecessary conversion happening...
	private JsonNode getRequestDetails(FormatRequest formatRequest) {
		JsonNode postBodyJson = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			String postBody = mapper.writeValueAsString(formatRequest);
			postBodyJson = mapper.readValue(postBody, JsonNode.class);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return postBodyJson;
	}
}

