package org.ishafoundation.dwaraapi.entrypoint.resource.ingest;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.reference.Requesttype;
import org.ishafoundation.dwaraapi.db.model.master.reference.Status;

public class AllReferences {
	
	private List<Status> status;
	
	private List<Requesttype> requesttype;
	

	public List<Status> getStatus() {
		return status;
	}

	public void setStatus(List<Status> status) {
		this.status = status;
	}

	public List<Requesttype> getRequesttype() {
		return requesttype;
	}

	public void setRequesttype(List<Requesttype> requesttype) {
		this.requesttype = requesttype;
	}
}
