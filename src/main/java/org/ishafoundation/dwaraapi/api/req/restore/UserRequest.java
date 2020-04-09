package org.ishafoundation.dwaraapi.api.req.restore;

import java.util.List;

public class UserRequest {
	
	private int copyNumber;
	private int targetvolumeId;
	private String outputFolder;
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
	public List<FileParams> getFileParams() {
		return fileParams;
	}
	public void setFileParams(List<FileParams> fileParams) {
		this.fileParams = fileParams;
	}
	
}
