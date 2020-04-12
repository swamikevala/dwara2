package org.ishafoundation.dwaraapi.entrypoint.resource.ingest;

import java.util.List;

import org.ishafoundation.dwaraapi.entrypoint.resource.PagingAndSortingDetails;

public class WrappedSubrequestWithJobDetailsResourceList extends PagingAndSortingDetails{

	private List<SubrequestWithJobDetails> subrequest;

	
	public List<SubrequestWithJobDetails> getSubrequest() {
		return subrequest;
	}

	public void setSubrequest(List<SubrequestWithJobDetails> subrequest) {
		this.subrequest = subrequest;
	}
}
