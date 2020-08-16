package org.ishafoundation.dwaraapi.api.resp.staged.rename;

import java.util.List;

public class StagedRenameResponse {
	private int userRequestId;
	private String status;
	private List<StagedRenameFile> stagedFiles;
	
	public int getUserRequestId() {
		return userRequestId;
	}
	public void setUserRequestId(int userRequestId) {
		this.userRequestId = userRequestId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<StagedRenameFile> getStagedFiles() {
		return stagedFiles;
	}
	public void setStagedFiles(List<StagedRenameFile> stagedFiles) {
		this.stagedFiles = stagedFiles;
	}
}
