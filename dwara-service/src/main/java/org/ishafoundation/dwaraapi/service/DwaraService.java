package org.ishafoundation.dwaraapi.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DwaraService {

	private static final Logger logger = LoggerFactory.getLogger(DwaraService.class);
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	protected RequestDao requestDao;

	@Value("${dwara.keycloak.enabled}")
    private boolean KEYCLOAK_ENABLED;
	
	@Value("${keycloak.resource}")
	private String KEYCLOAK_RESOURCE;

	public String getUserFromContext() {
		if(KEYCLOAK_ENABLED) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (!(authentication instanceof AnonymousAuthenticationToken)) {
				KeycloakPrincipal principal = (KeycloakPrincipal)authentication.getPrincipal();
				AccessToken token = principal.getKeycloakSecurityContext().getToken();
				String userName = token.getPreferredUsername();

				return userName;

			}
			return null;
		}
		else {
			return SecurityContextHolder.getContext().getAuthentication().getName();
		}
	}

	public Set<String> getUserRoles() {
		if(KEYCLOAK_ENABLED) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			//System.out.println(authentication.toString());

			//realm roles
			/* SimpleKeycloakAccount keycloakAccount = (SimpleKeycloakAccount)authentication.getDetails();
            Set<String> roles = keycloakAccount.getRoles();
			return roles; */

			//client roles
			KeycloakPrincipal principal = (KeycloakPrincipal)authentication.getPrincipal();
			AccessToken token = principal.getKeycloakSecurityContext().getToken();
			return token.getResourceAccess().get(KEYCLOAK_RESOURCE).getRoles();
		}
		return null;
	}
	
	public User getUserObjFromContext() {
		if(KEYCLOAK_ENABLED) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (!(authentication instanceof AnonymousAuthenticationToken)) {
				User uInfo = new User();
				KeycloakPrincipal principal = (KeycloakPrincipal)authentication.getPrincipal();
				AccessToken token = principal.getKeycloakSecurityContext().getToken();
				String userName = token.getPreferredUsername();
				// String userId = token.getId();
				String email = token.getEmail();
				// String displayName = token.getName();
				Map<String, Object> customClaims = token.getOtherClaims();
				int dwaraUserId = -1;
				if (customClaims.containsKey("dwaraUserId")) {
					dwaraUserId = Integer.parseInt(String.valueOf(customClaims.get("dwaraUserId")));
				}

				uInfo.setName(userName);
				uInfo.setId(dwaraUserId);
				uInfo.setEmail(email);
				return uInfo;
			}
			return null;
		}
		else {
			String requestedBy = getUserFromContext();
			User user = userDao.findByName(requestedBy);
			return user;
		}
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
	
	protected Request createUserRequest(Action requestedBusinessAction, Object requestPayload) {
		return createUserRequest(requestedBusinessAction, Status.queued, requestPayload);
	}
		
	protected Request createUserRequest(Action requestedBusinessAction, Status status, Object requestPayload) {		
		return createUserRequest(requestedBusinessAction, status, requestPayload, null);
	}
	
	//Overload for scheduler
	protected Request createUserRequest(Action requestedBusinessAction, Object requestPayload, User user) {
		return createUserRequest(requestedBusinessAction, Status.queued, requestPayload, user);
	}
	
	protected Request createUserRequest(Action requestedBusinessAction, Status status, Object requestPayload, User sentUser) {
		Request request = new Request();
		request.setType(RequestType.user);
		request.setActionId(requestedBusinessAction);
		request.setStatus(status);
		User user = sentUser != null ? sentUser : getUserObjFromContext();
		String requestedBy = user.getName();

		LocalDateTime requestedAt = LocalDateTime.now();
		request.setRequestedAt(requestedAt);
		request.setRequestedBy(user);
		RequestDetails details = new RequestDetails();
		JsonNode postBodyJson = getRequestDetails(requestPayload);
		details.setBody(postBodyJson);
		request.setDetails(details);

		request = requestDao.save(request);
		logger.info(DwaraConstants.USER_REQUEST + request.getId());

		return request;
	}
	
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
