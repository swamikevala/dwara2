package org.ishafoundation.dwaraapi.model;

import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.CommonResponse;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;

public class WrappedRequestList extends CommonResponse{
	
	private List<Request> request; // named request instead of requestList so output json will have an array of request...

	public List<Request> getRequestList() {
		return request;
	}

	public void setRequestList(List<Request> requestList) {
		this.request = requestList;
	}


}
