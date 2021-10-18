package org.ishafoundation.dwaraapi.api.resp._import;

import java.util.List;

import org.ishafoundation.dwaraapi.staged.scan.Error;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportResponse {
	private int userRequestId;
	private String action;
	private String requestedBy;
	private String requestedAt;
	private List<Artifact> artifacts;
	private List<Error> errors;
	
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
	public List<Artifact> getArtifacts() {
		return artifacts;
	}
	public void setArtifacts(List<Artifact> artifacts) {
		this.artifacts = artifacts;
	}
	public List<Error> getErrors() {
		return errors;
	}
	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}
}