package org.ishafoundation.dwaraapi.api.req.ingest;

public class FileAttributes {

	private String sourcePath;

	private String oldLibraryname;
	
	private String newLibraryname;

	
	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getOldLibraryname() {
		return oldLibraryname;
	}

	public void setOldLibraryname(String oldLibraryname) {
		this.oldLibraryname = oldLibraryname;
	}

	public String getNewLibraryname() {
		return newLibraryname;
	}

	public void setNewLibraryname(String newLibraryname) {
		this.newLibraryname = newLibraryname;
	}
}
