package org.ishafoundation.dwaraapi.model;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;

public class WrappedSubrequestList extends PagingAndSortingDetails{
	
	private List<Subrequest> subrequest; // named subrequest instead of subrequestList so output json will have an array of subrequest...

	public List<Subrequest> getSubrequestList() {
		return subrequest;
	}

	public void setSubrequestList(List<Subrequest> subrequestList) {
		this.subrequest = subrequestList;
	}
}
