package org.ishafoundation.dwaraapi.api.resp.ingest;

import org.ishafoundation.dwaraapi.api.resp.CommonResponse;
import org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest;

public class ResponseHeaderWrappedSubrequest extends CommonResponse{

	private Subrequest subrequest;

	
	public Subrequest getSubrequest() {
		return subrequest;
	}

	public void setSubrequest(Subrequest subrequest) {
		this.subrequest = subrequest;
	}
}
