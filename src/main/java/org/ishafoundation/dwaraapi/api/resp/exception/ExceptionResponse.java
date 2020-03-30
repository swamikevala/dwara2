package org.ishafoundation.dwaraapi.api.resp.exception;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;

public class ExceptionResponse {
	  private Date timestamp;
	  private Integer status;
	  private String error;
	  private String message;
	  private JsonNode details;
	  
	  public ExceptionResponse(Date timestamp, Integer status, String error, String message, JsonNode detailsJsonNode) {
	    super();
	    this.timestamp = timestamp;
	    this.status = status;
	    this.error = error;
	    this.message = message;
	    this.details = detailsJsonNode;
	  }

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public JsonNode getDetails() {
		return details;
	}

	public void setDetails(JsonNode details) {
		this.details = details;
	}
}
