package org.ishafoundation.dwaraapi.api.req.restore;

import java.util.List;

public class UserRequest {
	
	private int copyNumber;
	private int targetvolumeId;
	private String outputFolder;
	private String requestedBy;
	private long requestedAt;
	private List<FileParams> fileParams;

	public int getCopyNumber() {
		return copyNumber;
	}
	public void setCopyNumber(int copyNumber) {
		this.copyNumber = copyNumber;
	}
	public int getTargetvolumeId() {
		return targetvolumeId;
	}
	public void setTargetvolumeId(int targetvolumeId) {
		this.targetvolumeId = targetvolumeId;
	}
	public String getOutputFolder() {
		return outputFolder;
	}
	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}
	public String getRequestedBy() {
		return requestedBy;
	}
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	public long getRequestedAt() {
		return requestedAt;
	}
	public void setRequestedAt(long requestedAt) {
		this.requestedAt = requestedAt;
	}
	public List<FileParams> getFileParams() {
		return fileParams;
	}
	public void setFileParams(List<FileParams> fileParams) {
		this.fileParams = fileParams;
	}
	
}
