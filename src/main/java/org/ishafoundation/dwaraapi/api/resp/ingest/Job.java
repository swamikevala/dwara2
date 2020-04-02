package org.ishafoundation.dwaraapi.api.resp.ingest;

import java.time.LocalDateTime;

public class Job {
	private int id;
	
	private int taskId;

	private Integer inputLibraryId; // cannot be null, but is null initially for derived jobs and only later gets updated when the parent job's output is generated...

	private Integer outputLibraryId; // can be null
	
	private LocalDateTime completedAt;

	private LocalDateTime createdAt;

	private int subrequestId;
	
	private LocalDateTime startedAt;

	private String status;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public Integer getInputLibraryId() {
		return inputLibraryId;
	}

	public void setInputLibraryId(Integer inputLibraryId) {
		this.inputLibraryId = inputLibraryId;
	}

	public Integer getOutputLibraryId() {
		return outputLibraryId;
	}

	public void setOutputLibraryId(Integer outputLibraryId) {
		this.outputLibraryId = outputLibraryId;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public int getSubrequestId() {
		return subrequestId;
	}

	public void setSubrequestId(int subrequestId) {
		this.subrequestId = subrequestId;
	}

	public LocalDateTime getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(LocalDateTime startedAt) {
		this.startedAt = startedAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
