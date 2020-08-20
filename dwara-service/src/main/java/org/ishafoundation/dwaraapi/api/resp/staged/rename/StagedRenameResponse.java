package org.ishafoundation.dwaraapi.api.resp.staged.rename;

import java.util.List;

public class StagedRenameResponse {
	private int userRequestId;
	private String status;
	private List<StagedRenameFileForResponse> stagedFiles;
	
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
	public List<StagedRenameFileForResponse> getStagedFiles() {
		return stagedFiles;
	}
	public void setStagedFiles(List<StagedRenameFileForResponse> stagedFiles) {
		this.stagedFiles = stagedFiles;
	}
}
