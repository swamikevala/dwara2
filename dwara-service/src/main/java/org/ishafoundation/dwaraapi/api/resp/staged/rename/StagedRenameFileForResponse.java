package org.ishafoundation.dwaraapi.api.resp.staged.rename;

public class StagedRenameFileForResponse extends  org.ishafoundation.dwaraapi.api.req.staged.rename.StagedRenameFile{
	private String status;
	private String errorMessage;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
