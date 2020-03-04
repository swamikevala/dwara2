package org.ishafoundation.dwaraapi.api.req.ingest;

import java.util.List;

public class UserRequest {
	
	private int libraryclassId;

	private List<LibraryParams> subrequest;

	
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}

	public List<LibraryParams> getSubrequest() {
		return subrequest;
	}

	public void setSubrequest(List<LibraryParams> subrequest) {
		this.subrequest = subrequest;
	}
}
