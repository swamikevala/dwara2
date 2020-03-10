package org.ishafoundation.dwaraapi.api.resp.ingest;

public class IngestFile {
	
	private String sourcePath;
	private String oldFilename;// Holds the folder name that the user named the folder with
	private String newFilename;// Holds the suggested folder name by the system
	private int fileCount;
	private double fileSizeInBytes;
	private String prevSequenceCode;
	private boolean prevSequenceCodeExpected = false;
	private String warning;
	
	
	public String getSourcePath() {
		return sourcePath;
	}
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}
	public String getOldFilename() {
		return oldFilename;
	}
	public void setOldFilename(String oldFilename) {
		this.oldFilename = oldFilename;
	}
	public String getNewFilename() {
		return newFilename;
	}
	public void setNewFilename(String newFilename) {
		this.newFilename = newFilename;
	}
	public int getFileCount() {
		return fileCount;
	}
	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}
	public double getFileSizeInBytes() {
		return fileSizeInBytes;
	}
	public void setFileSizeInBytes(double fileSizeInBytes) {
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
	public String getWarning() {
		return warning;
	}
	public void setWarning(String warning) {
		this.warning = warning;
	}
}
