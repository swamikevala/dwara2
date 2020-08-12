package org.ishafoundation.dwaraapi.api.resp.format;

import java.util.List;

public class FormatResponse
{
    private int requestId;

    private String action;

    private String user;

    private String requestedAt;

    private List<SystemRequestsForFormatResponse> systemRequests;

    public void setRequestId(int requestId){
        this.requestId = requestId;
    }
    public int getRequestId(){
        return this.requestId;
    }
    public void setAction(String action){
        this.action = action;
    }
    public String getAction(){
        return this.action;
    }
    public void setUser(String user){
        this.user = user;
    }
    public String getUser(){
        return this.user;
    }
    public void setRequestedAt(String requestedAt){
        this.requestedAt = requestedAt;
    }
    public String getRequestedAt(){
        return this.requestedAt;
    }
	public List<SystemRequestsForFormatResponse> getSystemRequests() {
		return systemRequests;
	}
	public void setSystemRequests(List<SystemRequestsForFormatResponse> systemRequests) {
		this.systemRequests = systemRequests;
	}

}
