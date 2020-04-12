package org.ishafoundation.dwaraapi.entrypoint.resource.ingest;

public class IngestFile {
	
	private String sourcePath;
	private String libraryName;// Holds the folder name that the user named the folder with
	private String suggestedLibraryName;// Holds the suggested folder name by the system refer SourceDirScanner.getCustomFolderName
	private int fileCount;
	private double totalSize;
	private String prevSequenceCode;
	private boolean prevSequenceCodeExpected = false;
	private String errorType;
	private String errorMessage;
	
	
	public String getSourcePath() {
		return sourcePath;
	}
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}
	public String getLibraryName() {
		return libraryName;
	}
	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}
	public String getSuggestedLibraryName() {
		return suggestedLibraryName;
	}
	public void setSuggestedLibraryName(String suggestedLibraryName) {
		this.suggestedLibraryName = suggestedLibraryName;
	}
	public int getFileCount() {
		return fileCount;
	}
	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}
	public double getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(double totalSize) {
		this.totalSize = totalSize;
	}
	public String getPrevSequenceCode() {
		return prevSequenceCode;
	}
	public void setPrevSequenceCode(String prevSequenceCode) {
		this.prevSequenceCode = prevSequenceCode;
	}
	public boolean isPrevSequenceCodeExpected() {
		return prevSequenceCodeExpected;
	}
	public void setPrevSequenceCodeExpected(boolean prevSequenceCodeExpected) {
		this.prevSequenceCodeExpected = prevSequenceCodeExpected;
	}
	public String getErrorType() {
		return errorType;
	}
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
