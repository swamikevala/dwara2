package org.ishafoundation.dwaraapi.api.resp.ingest;

import java.util.List;

public class RequestWithWrappedSubrequest {
	
	private int requestId;
	
	private int requesttypeId;

	private int libraryclassId;

	private String requestedBy;

	private long requestedAt;
	
	private List<ResponseHeaderWrappedSubrequest> subrequestResp;
	

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getRequesttypeId() {
		return requesttypeId;
	}

	public void setRequesttypeId(int requesttypeId) {
		this.requesttypeId = requesttypeId;
	}

	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public long getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(long requestedAt) {
		this.requestedAt = requestedAt;
	}

	public List<ResponseHeaderWrappedSubrequest> getSubrequestResp() {
		return subrequestResp;
	}

	public void setSubrequestResp(List<ResponseHeaderWrappedSubrequest> subrequestResp) {
		this.subrequestResp = subrequestResp;
	}
}
