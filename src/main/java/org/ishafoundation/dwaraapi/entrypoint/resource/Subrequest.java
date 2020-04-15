package org.ishafoundation.dwaraapi.entrypoint.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Subrequest{

	private int id;
	
	private Integer fileId;
	
	private String sourcePath;

	private String skipTasks;

	private Boolean rerun;

	private Integer rerunNo;

	private Integer priority;

	private String prevSequenceCode;

	private Library library;
	
	private String status;

	private String[] permittedActions;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Integer getFileId() {
		return fileId;
	}

	public void setFileId(Integer fileId) {
		this.fileId = fileId;
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

	public Boolean getRerun() {
		return rerun;
	}

	public void setRerun(Boolean rerun) {
		this.rerun = rerun;
	}

	public Integer getRerunNo() {
		return rerunNo;
	}

	public void setRerunNo(Integer rerunNo) {
		this.rerunNo = rerunNo;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String[] getPermittedActions() {
		return permittedActions;
	}

	public void setPermittedActions(String[] permittedActions) {
		this.permittedActions = permittedActions;
	}
}
