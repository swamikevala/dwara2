package org.ishafoundation.dwaraapi.api.req.restore;

public class FileParams {

	private int fileId;
	
	private boolean optimizeTapeAccess;

	private int priority;

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

}
