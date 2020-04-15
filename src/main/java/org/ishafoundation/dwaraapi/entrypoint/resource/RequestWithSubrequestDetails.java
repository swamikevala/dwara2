package org.ishafoundation.dwaraapi.entrypoint.resource;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class RequestWithSubrequestDetails extends Request{
	
	private List<Subrequest> subrequestList;
	

	public List<Subrequest> getSubrequestList() {
		return subrequestList;
	}

	public void setSubrequestList(List<Subrequest> subrequestList) {
		this.subrequestList = subrequestList;
	}
}
