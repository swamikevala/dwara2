package org.ishafoundation.dwaraapi.api.resp.restore;

import org.ishafoundation.dwaraapi.api.resp.CommonResponse;

public class SubrequestResp extends CommonResponse{

	private Subrequest subrequest;

	
	public Subrequest getSubrequest() {
		return subrequest;
	}

	public void setSubrequest(Subrequest subrequest) {
		this.subrequest = subrequest;
	}
}
