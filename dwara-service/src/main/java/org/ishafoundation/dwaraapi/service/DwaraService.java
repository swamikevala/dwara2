package org.ishafoundation.dwaraapi.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DwaraService {

	@Autowired
	private UserDao userDao;

	protected String getUserFromContext() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}
	
	protected User getUserObjFromContext() {
		String requestedBy = getUserFromContext();
		User user = userDao.findByName(requestedBy);
		return user;
	}

	protected JsonNode getRequestDetails(Object payload) {
		JsonNode postBodyJson = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			String postBody = mapper.writeValueAsString(payload);
			postBodyJson = mapper.readValue(postBody, JsonNode.class);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return postBodyJson;
	}

	protected String getDateForUI(LocalDateTime _tedAt) { // requestedAt, createdAt, startedAt
		String dateForUI = null;
		if(_tedAt != null) {
			ZonedDateTime zdt = _tedAt.atZone(ZoneId.of("UTC"));
			dateForUI = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").format(zdt);
		}
		return dateForUI;
	}
	
//	protected Request createUserRequest(Action requestedBusinessAction, Object requestPayload) {
//		Request request = new Request();
//		request.setType(RequestType.user);
//		request.setActionId(requestedBusinessAction);
//		request.setStatus(Status.queued);
//    	User user = getUserObjFromContext();
//    	String requestedBy = user.getName();
//
//    	LocalDateTime requestedAt = LocalDateTime.now();
//		request.setRequestedAt(requestedAt);
//		request.setRequestedBy(user);
//		RequestDetails details = new RequestDetails();
//		JsonNode postBodyJson = getRequestDetails(requestPayload); 
//		details.setBody(postBodyJson);
//		request.setDetails(details);
//
//		request = requestDao.save(request);
//		logger.info(DwaraConstants.USER_REQUEST + request.getId());
//
//		return request;
//	}
	
	
//	protected Request createSystemRequest(Request userRequest) {
//		Request systemRequest = new Request();
//		systemRequest.setType(RequestType.system);
//		systemRequest.setRequestRef(userRequest);
//		systemRequest.setActionId(userRequest.getActionId());
//		systemRequest.setStatus(Status.queued);
//		systemRequest.setRequestedBy(userRequest.getRequestedBy());
//		systemRequest.setRequestedAt(LocalDateTime.now());
//	
//		RequestDetails systemrequestDetails = requestToEntityObjectMapper.getRequestDetailsForInitialize(nthInitializeRequest);
//	
//		systemRequest.setDetails(systemrequestDetails);
//		systemRequest = requestDao.save(systemRequest);
//		logger.info(DwaraConstants.SYSTEM_REQUEST + systemRequest.getId());
//
//		return systemRequest;
//	}
	
}
