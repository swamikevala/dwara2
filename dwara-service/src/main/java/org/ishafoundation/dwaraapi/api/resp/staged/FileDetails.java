package org.ishafoundation.dwaraapi.api.resp.staged;

public class FileDetails {
	private String path;
	private String name;
	private String suggestedName;
	private int fileCount;
	private long totalSize;
	private long fileSizeInBytes;
	private String prevSequenceCode;
	private boolean prevSequenceCodeExpected;
	private String errorType;
	private String errorMessage;
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSuggestedName() {
		return suggestedName;
	}
	public void setSuggestedName(String suggestedName) {
		this.suggestedName = suggestedName;
	}
	public int getFileCount() {
		return fileCount;
	}
	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}
	public long getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}
	public long getFileSizeInBytes() {
		return fileSizeInBytes;
	}
	public void setFileSizeInBytes(long fileSizeInBytes) {
		this.fileSizeInBytes = fileSizeInBytes;
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
