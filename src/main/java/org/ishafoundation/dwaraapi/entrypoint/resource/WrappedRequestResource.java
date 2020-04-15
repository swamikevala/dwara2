package org.ishafoundation.dwaraapi.entrypoint.resource;

public class WrappedRequestResource extends PagingAndSortingDetails{
	
	private RequestWithSubrequestDetails request;

	
	public RequestWithSubrequestDetails getRequest() {
		return request;
	}

	public void setRequest(RequestWithSubrequestDetails request) {
		this.request = request;
	}
}
