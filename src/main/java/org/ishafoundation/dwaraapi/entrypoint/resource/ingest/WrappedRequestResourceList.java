package org.ishafoundation.dwaraapi.entrypoint.resource.ingest;

import java.util.List;

import org.ishafoundation.dwaraapi.entrypoint.resource.PagingAndSortingDetails;

public class WrappedRequestResourceList extends PagingAndSortingDetails{
	
	private List<Request> request;

	
	public List<Request> getRequest() {
		return request;
	}

	public void setRequest(List<Request> request) {
		this.request = request;
	}
}
