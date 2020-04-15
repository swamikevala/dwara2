package org.ishafoundation.dwaraapi.entrypoint.resource;

import java.util.List;

public class WrappedRequestResourceList extends PagingAndSortingDetails{
	
	private List<RequestWithSubrequestDetails> request;

	
	public List<RequestWithSubrequestDetails> getRequest() {
		return request;
	}

	public void setRequest(List<RequestWithSubrequestDetails> request) {
		this.request = request;
	}
}
