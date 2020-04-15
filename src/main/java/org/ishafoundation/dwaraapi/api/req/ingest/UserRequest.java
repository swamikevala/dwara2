package org.ishafoundation.dwaraapi.api.req.ingest;

import java.util.List;

public class UserRequest {
	
	private String libraryclass;

	private List<LibraryParams> library;

	
	public String getLibraryclass() {
		return libraryclass;
	}

	public void setLibraryclass(String libraryclass) {
		this.libraryclass = libraryclass;
	}
	
	public List<LibraryParams> getLibrary() {
		return library;
	}

	public void setLibrary(List<LibraryParams> library) {
		this.library = library;
	}
}
