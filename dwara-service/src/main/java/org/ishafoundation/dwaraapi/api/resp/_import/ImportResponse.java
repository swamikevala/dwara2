package org.ishafoundation.dwaraapi.api.resp._import;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportResponse {
	private int userRequestId;
	private String action;
	private String requestedBy;
	private String requestedAt;
	
	public int getUserRequestId() {
		return userRequestId;
	}
	public void setUserRequestId(int userRequestId) {
		this.userRequestId = userRequestId;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getRequestedBy() {
		return requestedBy;
	}
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	public String getRequestedAt() {
		return requestedAt;
	}
	public void setRequestedAt(String requestedAt) {
		this.requestedAt = requestedAt;
	}
}