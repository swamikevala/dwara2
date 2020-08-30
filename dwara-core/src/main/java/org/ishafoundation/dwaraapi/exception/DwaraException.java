package org.ishafoundation.dwaraapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.JsonNode;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DwaraException extends RuntimeException{

	private static final long serialVersionUID = -8467898727766076723L;

	private ExceptionType exceptionType;
	private JsonNode details;

	public DwaraException(String message) {
		super(message);
		this.setExceptionType(ExceptionType.error);
		this.setDetails(null);
	}
	
	public DwaraException(String message, JsonNode jsonNode) {
		super(message);
		this.setExceptionType(ExceptionType.error);
		this.setDetails(jsonNode);
	}
	
	public DwaraException(String message, ExceptionType exceptionType, JsonNode jsonNode) {
		super(message);
		this.setExceptionType(exceptionType);
		this.setDetails(jsonNode);
	}
	
	public ExceptionType getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(ExceptionType exceptionType) {
		this.exceptionType = exceptionType;
	}

	public JsonNode getDetails() {
		return details;
	}

	public void setDetails(JsonNode jsonNode) {
		this.details = jsonNode;
	}
}
