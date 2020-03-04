package org.ishafoundation.dwaraapi.api.resp.restore;

import org.ishafoundation.dwaraapi.api.resp.CommonResponse;

public class Response extends CommonResponse{
	
	private Request request;

	
	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}
}
