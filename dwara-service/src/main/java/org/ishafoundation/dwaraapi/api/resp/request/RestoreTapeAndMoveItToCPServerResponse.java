package org.ishafoundation.dwaraapi.api.resp.request;

public class RestoreTapeAndMoveItToCPServerResponse {

	private String serverName;
	
	private RequestResponse response;

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public RequestResponse getResponse() {
		return response;
	}

	public void setResponse(RequestResponse response) {
		this.response = response;
	}
}
