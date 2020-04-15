package org.ishafoundation.dwaraapi.entrypoint.resource;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Request {
	
	private int id;
	
	private String action;

	private String libraryclassName;

	private String requestedBy;

	private LocalDateTime requestedAt;
	
	private Integer copyNumber;
	
	private String targetvolumeName;

	private String outputFolder;
	
	private Integer requestId;
	
	private Integer subrequestId;
	
	private Integer jobId;
	
	private Integer libraryId;
	
	private String[] permittedActions;
	
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getLibraryclassName() {
		return libraryclassName;
	}

	public void setLibraryclassName(String libraryclassName) {
		this.libraryclassName = libraryclassName;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(LocalDateTime requestedAt) {
		this.requestedAt = requestedAt;
	}

	public Integer getCopyNumber() {
		return copyNumber;
	}

	public void setCopyNumber(Integer copyNumber) {
		this.copyNumber = copyNumber;
	}

	public String getTargetvolumeName() {
		return targetvolumeName;
	}

	public void setTargetvolumeName(String targetvolumeName) {
		this.targetvolumeName = targetvolumeName;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public Integer getRequestId() {
		return requestId;
	}

	public void setRequestId(Integer requestId) {
		this.requestId = requestId;
	}

	public Integer getSubrequestId() {
		return subrequestId;
	}

	public void setSubrequestId(Integer subrequestId) {
		this.subrequestId = subrequestId;
	}

	public Integer getJobId() {
		return jobId;
	}

	public void setJobId(Integer jobId) {
		this.jobId = jobId;
	}

	public Integer getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(Integer libraryId) {
		this.libraryId = libraryId;
	}

	public String[] getPermittedActions() {
		return permittedActions;
	}

	public void setPermittedActions(String[] permittedActions) {
		this.permittedActions = permittedActions;
	}
}
