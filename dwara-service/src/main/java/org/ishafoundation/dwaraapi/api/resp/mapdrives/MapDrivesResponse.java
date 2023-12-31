package org.ishafoundation.dwaraapi.api.resp.mapdrives;

public class MapDrivesResponse {

    private int userRequestId;

    private String action;

    private String requestedBy;

    private String requestedAt;
    
    private SystemRequestForMapDriveResponse systemRequest;

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

	public SystemRequestForMapDriveResponse getSystemRequest() {
		return systemRequest;
	}

	public void setSystemRequest(SystemRequestForMapDriveResponse systemRequest) {
		this.systemRequest = systemRequest;
	}
}
