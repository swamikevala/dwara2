package org.ishafoundation.dwaraapi.entrypoint.resource.restore;

public class Subrequest {

	private int subrequestId;
	
	private int requestId;
	
	private int fileId;
	
	private boolean optimizeTapeAccess;

	private int priority;

	private int subrequestRefId;

	private String status;
	

	public int getSubrequestId() {
		return subrequestId;
	}

	public void setSubrequestId(int subrequestId) {
		this.subrequestId = subrequestId;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public boolean isOptimizeTapeAccess() {
		return optimizeTapeAccess;
	}

	public void setOptimizeTapeAccess(boolean optimizeTapeAccess) {
		this.optimizeTapeAccess = optimizeTapeAccess;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getSubrequestRefId() {
		return subrequestRefId;
	}

	public void setSubrequestRefId(int subrequestRefId) {
		this.subrequestRefId = subrequestRefId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
