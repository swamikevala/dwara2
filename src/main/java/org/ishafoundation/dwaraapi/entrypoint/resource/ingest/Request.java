package org.ishafoundation.dwaraapi.entrypoint.resource.ingest;

import java.time.LocalDateTime;
import java.util.List;

public class Request {
	
	private int requestId;
	
	private String requesttype;

	private String libraryclassName;

	private String requestedBy;

	private LocalDateTime requestedAt;
	
	private Integer[] permittedRequestTypeIds;
	
	private List<Subrequest> subrequestList;
	

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public String getRequesttype() {
		return requesttype;
	}

	public void setRequesttype(String requesttype) {
		this.requesttype = requesttype;
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

	public Integer[] getPermittedRequestTypeIds() {
		return permittedRequestTypeIds;
	}

	public void setPermittedRequestTypeIds(Integer[] permittedRequestTypeIds) {
		this.permittedRequestTypeIds = permittedRequestTypeIds;
	}

	public List<Subrequest> getSubrequestList() {
		return subrequestList;
	}

	public void setSubrequestList(List<Subrequest> subrequestList) {
		this.subrequestList = subrequestList;
	}


}
