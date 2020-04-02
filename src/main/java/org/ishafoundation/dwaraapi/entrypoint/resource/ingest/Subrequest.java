package org.ishafoundation.dwaraapi.entrypoint.resource.ingest;

public class Subrequest{

	private int id;
	
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
	
	private Integer[] permittedRequestTypeIds;
	
	private String status;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public Integer[] getPermittedRequestTypeIds() {
		return permittedRequestTypeIds;
	}

	public void setPermittedRequestTypeIds(Integer[] permittedRequestTypeIds) {
		this.permittedRequestTypeIds = permittedRequestTypeIds;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
