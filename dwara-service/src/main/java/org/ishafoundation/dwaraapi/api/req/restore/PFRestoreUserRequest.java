package org.ishafoundation.dwaraapi.api.req.restore;

import java.util.List;

public class PFRestoreUserRequest {
	private Integer copy;
	private String outputFolder;
	private String destinationPath;
	private boolean verify;
	private List<FileDetails> files;

	public Integer getCopy() {
		return copy;
	}

	public void setCopy(Integer copy) {
		this.copy = copy;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public String getDestinationPath() {
		return destinationPath;
	}

	public void setDestinationPath(String destinationPath) {
		this.destinationPath = destinationPath;
	}

	public boolean isVerify() {
		return verify;
	}

	public void setVerify(boolean verify) {
		this.verify = verify;
	}
	
	public List<FileDetails> getFiles() {
		return files;
	}

	public void setFiles(List<FileDetails> files) {
		this.files = files;
	}
}
