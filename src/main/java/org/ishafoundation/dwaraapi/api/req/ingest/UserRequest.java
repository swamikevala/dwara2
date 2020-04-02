package org.ishafoundation.dwaraapi.api.req.ingest;

import java.util.List;

public class UserRequest {
	
	private int libraryclassId;

	private List<LibraryParams> library;

	
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}

	public List<LibraryParams> getLibrary() {
		return library;
	}

	public void setLibrary(List<LibraryParams> library) {
		this.library = library;
	}
}
