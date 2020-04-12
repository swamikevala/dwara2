package org.ishafoundation.dwaraapi.entrypoint.resource.ingest;

import java.time.LocalDateTime;
import java.util.List;

public class Request {
	
	private int requestId;
	
	private String action;

	private String libraryclassName;

	private String requestedBy;

	private LocalDateTime requestedAt;
	
	private String[] permittedActions;
	
	private List<Subrequest> subrequestList;
	

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getLibraryclassName() {
		return libraryclassName;
	}

	public void setLibraryclassName(String libraryclassName) {
		this.libraryclassName = libraryclassName;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(LocalDateTime requestedAt) {
		this.requestedAt = requestedAt;
	}

	public String[] getPermittedActions() {
		return permittedActions;
	}

	public void setPermittedActions(String[] permittedActions) {
		this.permittedActions = permittedActions;
	}

	public List<Subrequest> getSubrequestList() {
		return subrequestList;
	}

	public void setSubrequestList(List<Subrequest> subrequestList) {
		this.subrequestList = subrequestList;
	}


}
