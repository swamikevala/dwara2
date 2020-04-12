package org.ishafoundation.dwaraapi.entrypoint.resource.restore;

import org.ishafoundation.dwaraapi.entrypoint.resource.PagingAndSortingDetails;

public class WrappedRequestResource extends PagingAndSortingDetails{
	
	private Request request;

	
	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}
}
