package org.ishafoundation.dwaraapi.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class UserRequestHelper {

	private static final Logger logger = LoggerFactory.getLogger(UserRequestHelper.class);
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	protected RequestDao requestDao;

	public Request createUserRequest(Action requestedBusinessAction, String userName, Object requestPayload) {
		return createUserRequest(requestedBusinessAction, userName, Status.queued, requestPayload);
	}
	
	public Request createUserRequest(Action requestedBusinessAction, String userName, Status status, Object requestPayload) {
		Request request = new Request();
		request.setType(RequestType.user);
		request.setActionId(requestedBusinessAction);
		request.setStatus(status);

		User requestedByUser = userDao.findByName(userName);
    	request.setRequestedBy(requestedByUser);
    	request.setRequestedAt(LocalDateTime.now());
		
		RequestDetails details = new RequestDetails();
		JsonNode postBodyJson = getRequestDetails(requestPayload); 
		details.setBody(postBodyJson);
		request.setDetails(details);

		request = requestDao.save(request);
		logger.info(DwaraConstants.USER_REQUEST + request.getId() + " - " + requestedBusinessAction.name());

		return request;
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
}
