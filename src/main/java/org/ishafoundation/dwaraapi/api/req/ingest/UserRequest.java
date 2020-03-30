package org.ishafoundation.dwaraapi.api.req.ingest;

import java.util.List;

public class UserRequest {
	
	private int libraryclassId;

	private List<LibraryParams> libraryParamsList;

	
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}

	public List<LibraryParams> getLibraryParamsList() {
		return libraryParamsList;
	}

	public void setLibraryParamsList(List<LibraryParams> libraryParamsList) {
		this.libraryParamsList = libraryParamsList;
	}
}
