package org.ishafoundation.dwaraapi.entrypoint.resource.ingest;

import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.CommonResponse;

public class WrappedRequestResourceList extends CommonResponse{
	
	private List<Request> request;

	
	public List<Request> getRequest() {
		return request;
	}

	public void setRequest(List<Request> request) {
		this.request = request;
	}
}
