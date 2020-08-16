package org.ishafoundation.dwaraapi.api.resp.staged.ingest;

import java.util.ArrayList;
import java.util.List;

public class IngestResponse {

	private int userRequestId;
	private String action;
	private String artifactclass;
	private String requestedBy;
	private String requestedAt;
	private List<IngestSystemRequest> systemRequests = new ArrayList<IngestSystemRequest>();
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
	public String getArtifactclass() {
		return artifactclass;
	}
	public void setArtifactclass(String artifactclass) {
		this.artifactclass = artifactclass;
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
	public List<IngestSystemRequest> getSystemRequests() {
		return systemRequests;
	}
	public void setSystemRequests(List<IngestSystemRequest> systemRequests) {
		this.systemRequests = systemRequests;
	}
}
