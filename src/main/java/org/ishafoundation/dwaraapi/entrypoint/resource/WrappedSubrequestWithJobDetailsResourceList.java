package org.ishafoundation.dwaraapi.entrypoint.resource;

import java.util.List;

public class WrappedSubrequestWithJobDetailsResourceList extends PagingAndSortingDetails{

	private List<SubrequestWithJobDetails> subrequest;

	
	public List<SubrequestWithJobDetails> getSubrequest() {
		return subrequest;
	}

	public void setSubrequest(List<SubrequestWithJobDetails> subrequest) {
		this.subrequest = subrequest;
	}
}
