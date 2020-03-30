package org.ishafoundation.dwaraapi.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.JsonNode;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DwaraException extends RuntimeException{

	private static final long serialVersionUID = -8467898727766076723L;

	private JsonNode details;
	
	public DwaraException(String message, JsonNode jsonNode) {
		super(message);
		this.setDetails(jsonNode);
	}

	public JsonNode getDetails() {
		return details;
	}

	public void setDetails(JsonNode jsonNode) {
		this.details = jsonNode;
	}
}
