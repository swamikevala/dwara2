package org.ishafoundation.dwaraapi.api.resp.job;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupedJobResponse {
	
	private String columnName;
	private Integer copy;
	private String status;
	private String message;
	private String volume;
	private List<JobResponse> placeholderJob;
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public Integer getCopy() {
		return copy;
	}
	public void setCopy(Integer copy) {
		this.copy = copy;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	public List<JobResponse> getPlaceholderJob() {
		return placeholderJob;
	}
	public void setPlaceholderJob(List<JobResponse> placeholderJob) {
		this.placeholderJob = placeholderJob;
	}
}
