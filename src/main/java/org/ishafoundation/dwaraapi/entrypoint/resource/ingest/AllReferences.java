package org.ishafoundation.dwaraapi.entrypoint.resource.ingest;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.reference.Action;
import org.ishafoundation.dwaraapi.db.model.master.reference.Status;

public class AllReferences {
	
	private List<Status> status;
	
	private List<Action> action;
	

	public List<Status> getStatus() {
		return status;
	}

	public void setStatus(List<Status> status) {
		this.status = status;
	}

	public List<Action> getAction() {
		return action;
	}

	public void setAction(List<Action> action) {
		this.action = action;
	}
}
