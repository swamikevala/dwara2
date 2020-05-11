package org.ishafoundation.dwaraapi.db.model.transactional;

public class IngestColumns {
	
	private String sourcePath;

	private String skipTasks;

	private Boolean rerun;

	private Integer rerunNo;

	private int libraryId; // foreign key constraint not possible...

	private String libraryName;

	private String prevSequenceCode;
	
	

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getSkipTasks() {
		return skipTasks;
	}

	public void setSkipTasks(String skipTasks) {
		this.skipTasks = skipTasks;
	}

	public Boolean getRerun() {
		return rerun;
	}

	public void setRerun(Boolean rerun) {
		this.rerun = rerun;
	}

	public Integer getRerunNo() {
		return rerunNo;
	}

	public void setRerunNo(Integer rerunNo) {
		this.rerunNo = rerunNo;
	}

	public int getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(int libraryId) {
		this.libraryId = libraryId;
	}

	public String getLibraryName() {
		return libraryName;
	}

	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}

	public String getPrevSequenceCode() {
		return prevSequenceCode;
	}

	public void setPrevSequenceCode(String prevSequenceCode) {
		this.prevSequenceCode = prevSequenceCode;
	}
	
	
}
