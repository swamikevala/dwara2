package org.ishafoundation.dwaraapi.entrypoint.resource;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Job {
	private int id;
	
	private String taskName;

	private Library inputLibrary; // cannot be null, but is null initially for derived jobs and only later gets updated when the parent job's output is generated...

	private Library outputLibrary; // can be null
	
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

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Library getInputLibrary() {
		return inputLibrary;
	}

	public void setInputLibrary(Library inputLibrary) {
		this.inputLibrary = inputLibrary;
	}

	public Library getOutputLibrary() {
		return outputLibrary;
	}

	public void setOutputLibrary(Library outputLibrary) {
		this.outputLibrary = outputLibrary;
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
