package org.ishafoundation.dwaraapi.api.resp.initialize;

import java.util.List;

public class InitializeResponse
{
    private int userRequestId;

    private String action;

    private String requestedBy;

    private String requestedAt;

    private List<SystemRequestsForInitializeResponse> systemRequests;


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
	public void setRequestedAt(String requestedAt){
        this.requestedAt = requestedAt;
    }
    public String getRequestedAt(){
        return this.requestedAt;
    }
	public List<SystemRequestsForInitializeResponse> getSystemRequests() {
		return systemRequests;
	}
	public void setSystemRequests(List<SystemRequestsForInitializeResponse> systemRequests) {
		this.systemRequests = systemRequests;
	}

}
