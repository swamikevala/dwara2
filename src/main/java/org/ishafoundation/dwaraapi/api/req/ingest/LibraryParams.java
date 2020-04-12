package org.ishafoundation.dwaraapi.api.req.ingest;

public class LibraryParams{

	private String sourcePath;

	private String name;
	
	private String skipTasks;

	private boolean rerun;

	private String prevSequenceCode;
	

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getSkipTasks() {
		return skipTasks;
	}

	public void setSkipTasks(String skipTasks) {
		this.skipTasks = skipTasks;
	}

	public boolean isRerun() {
		return rerun;
	}

	public void setRerun(boolean rerun) {
		this.rerun = rerun;
	}

	public String getPrevSequenceCode() {
		return prevSequenceCode;
	}

	public void setPrevSequenceCode(String prevSequenceCode) {
		this.prevSequenceCode = prevSequenceCode;
	}
}
