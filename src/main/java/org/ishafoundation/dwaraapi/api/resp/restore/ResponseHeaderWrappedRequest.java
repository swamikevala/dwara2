package org.ishafoundation.dwaraapi.api.resp.restore;

import org.ishafoundation.dwaraapi.api.resp.CommonResponse;

public class ResponseHeaderWrappedRequest extends CommonResponse{
	
	private RequestWithWrappedSubrequest request;

	
	public RequestWithWrappedSubrequest getRequest() {
		return request;
	}

	public void setRequest(RequestWithWrappedSubrequest request) {
		this.request = request;
	}
}
