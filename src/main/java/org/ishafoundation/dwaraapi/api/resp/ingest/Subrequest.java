package org.ishafoundation.dwaraapi.api.resp.ingest;

import org.ishafoundation.dwaraapi.db.model.transactional.Library;

public class Subrequest{

	private int subrequestId;
	
	private int requestId;
	
	private String sourcePath;

	private String skipTasks;

	private boolean rerun;

	private int rerunNo;

	private boolean optimizeTapeAccess;

	private int priority;

	private int subrequestRefId;

	private String oldFilename;
	
	private String newFilename;

	private String prevSequenceCode;

	private Library library;
	
	private int statusId;
	

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

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getSkipTasks() {
		return skipTasks;
	}

	public void setSkipTasks(String skipTasks) {
		this.skipTasks = skipTasks;
	}

	public boolean isRerun() {
		return rerun;
	}

	public void setRerun(boolean rerun) {
		this.rerun = rerun;
	}

	public int getRerunNo() {
		return rerunNo;
	}

	public void setRerunNo(int rerunNo) {
		this.rerunNo = rerunNo;
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

	public String getOldFilename() {
		return oldFilename;
	}

	public void setOldFilename(String oldFilename) {
		this.oldFilename = oldFilename;
	}

	public String getNewFilename() {
		return newFilename;
	}

	public void setNewFilename(String newFilename) {
		this.newFilename = newFilename;
	}

	public String getPrevSequenceCode() {
		return prevSequenceCode;
	}

	public void setPrevSequenceCode(String prevSequenceCode) {
		this.prevSequenceCode = prevSequenceCode;
	}

	public Library getLibrary() {
		return library;
	}

	public void setLibrary(Library library) {
		this.library = library;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}
}
