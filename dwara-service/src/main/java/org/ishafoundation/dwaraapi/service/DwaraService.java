package org.ishafoundation.dwaraapi.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.format.FormatUserRequest;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
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

	protected String getDateForUI(LocalDateTime requestedAt) {
		ZonedDateTime zdt = requestedAt.atZone(ZoneId.of("UTC"));
		return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").format(zdt);
	}
}
