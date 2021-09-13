package org.ishafoundation.dwara.misc.watcher;

public class ArtifactValidationResponse {
	
	private boolean valid;
	private String failureReason;
	
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public String getFailureReason() {
		return failureReason;
	}
	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}
}
