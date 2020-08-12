package org.ishafoundation.dwaraapi.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.format.FormatRequest;
import org.ishafoundation.dwaraapi.api.req.ingest.mapper.RequestToEntityObjectMapper;
import org.ishafoundation.dwaraapi.api.resp.format.FormatResponse;
import org.ishafoundation.dwaraapi.api.resp.format.SystemRequestsForFormatResponse;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class VolumeService {

	private static final Logger logger = LoggerFactory.getLogger(VolumeService.class);

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private JobCreator jobCreator;
	
	@Autowired
	private UserDao userDao;

	@Autowired
	private RequestToEntityObjectMapper requestToEntityObjectMapper; 


	public FormatResponse format(List<FormatRequest> formatRequestList) throws Exception{	
		
		FormatResponse formatResponse = new FormatResponse();
		Request request = new Request();
		request.setActionId(Action.format);
    	String requestedBy = getUserFromContext();
    	User user = userDao.findByName(requestedBy);
    	request.setUser(user);
    	LocalDateTime requestedAt = LocalDateTime.now();
		request.setRequestedAt(requestedAt);
		
		RequestDetails details = new RequestDetails();
		JsonNode postBodyJson = getRequestDetails(formatRequestList); 
		details.setBody(postBodyJson);
		request.setDetails(details);

		request = requestDao.save(request);
		int requestId = request.getId();
		logger.info("Request - " + requestId);
		
		formatResponse.setRequestId(requestId);
		formatResponse.setAction(request.getActionId().name());
		ZonedDateTime zdt = requestedAt.atZone(ZoneId.of("UTC"));
		String requestedAtForResponse = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").format(zdt);
		formatResponse.setRequestedAt(requestedAtForResponse);
		formatResponse.setUser(requestedBy);
		List<SystemRequestsForFormatResponse> systemRequests = new ArrayList<SystemRequestsForFormatResponse>();
		
		for (FormatRequest nthFormatRequest : formatRequestList) {
			Request systemrequest = new Request();
			systemrequest.setRequestRef(request);
			systemrequest.setActionId(request.getActionId());
			systemrequest.setUser(request.getUser());
			systemrequest.setRequestedAt(LocalDateTime.now());
			systemrequest.setDomain(request.getDomain());


			RequestDetails systemrequestDetails = requestToEntityObjectMapper.getRequestDetails(nthFormatRequest);

			systemrequest.setDetails(systemrequestDetails);
			systemrequest = requestDao.save(systemrequest);
			logger.info("System request - " + systemrequest.getId());

			Job job = jobCreator.createJobs(systemrequest, null).get(0);
			int jobId = job.getId();
			Status status = job.getStatus();
			
			SystemRequestsForFormatResponse systemRequestsForFormatResponse = new SystemRequestsForFormatResponse();
			systemRequestsForFormatResponse.setId(systemrequest.getId());
			systemRequestsForFormatResponse.setJobId(jobId);
			systemRequestsForFormatResponse.setStatus(status);
			systemRequestsForFormatResponse.setVolume(nthFormatRequest.getVolume());
			
			systemRequests.add(systemRequestsForFormatResponse);
		}
		
		formatResponse.setSystemRequests(systemRequests);
		return formatResponse;	
	}

	private String getUserFromContext() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	
	// TODO : Why domain needed? Filevolume and Artifactvolume needed for generating the index are domain-ed
	public ResponseEntity<String> finalize(String volumeId, Domain domain){

		try {

			Request request = new Request();
			request.setActionId(Action.finalize);
			// request.setUser(userDao.findByName(requestedBy));
			// request.setUser(user);
			request.setRequestedAt(LocalDateTime.now());
			request.setDomain(domain);
			RequestDetails details = new RequestDetails();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode postBodyJson = mapper.readValue("{\"volume_id\":\""+volumeId+"\"}", JsonNode.class);
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
			systemrequestDetails.setVolume_id(volumeId);
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
	private JsonNode getRequestDetails(List<FormatRequest> formatRequestList) {
		JsonNode postBodyJson = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			String postBody = mapper.writeValueAsString(formatRequestList);
			postBodyJson = mapper.readValue(postBody, JsonNode.class);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return postBodyJson;
	}
}

