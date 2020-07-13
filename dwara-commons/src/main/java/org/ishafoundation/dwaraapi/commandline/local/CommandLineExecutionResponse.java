package org.ishafoundation.dwaraapi.commandline.local;

public class CommandLineExecutionResponse {
	
	private boolean isComplete;
	private boolean isCancelled;
	private String failureReason;
	private String stdOutResponse;
	
	public boolean isComplete() {
		return isComplete;
	}
	public void setIsComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
	public boolean isCancelled() {
		return isCancelled;
	}
	public void setIsCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}
	public String getFailureReason() {
		return failureReason;
	}
	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}
	public String getStdOutResponse() {
		return stdOutResponse;
	}
	public void setStdOutResponse(String stdOutResponse) {
		this.stdOutResponse = stdOutResponse;
	}

}
