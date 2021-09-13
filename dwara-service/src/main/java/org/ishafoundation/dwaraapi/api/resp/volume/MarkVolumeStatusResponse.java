package org.ishafoundation.dwaraapi.api.resp.volume;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class MarkVolumeStatusResponse {
	private int requestId;
	private String action;
	private String requestedBy;
	
    private String requestedAt;
    private String completedAt;
    private String status;

    private String volumeId;
    private String volumeStatus;
	public int getRequestId() {
		return requestId;
	}
	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getRequestedBy() {
		return requestedBy;
	}
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	public String getRequestedAt() {
		return requestedAt;
	}
	public void setRequestedAt(String requestedAt) {
		this.requestedAt = requestedAt;
	}
	public String getCompletedAt() {
		return completedAt;
	}
	public void setCompletedAt(String completedAt) {
		this.completedAt = completedAt;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getVolumeId() {
		return volumeId;
	}
	public void setVolumeId(String volumeId) {
		this.volumeId = volumeId;
	}
	public String getVolumeStatus() {
		return volumeStatus;
	}
	public void setVolumeStatus(String volumeStatus) {
		this.volumeStatus = volumeStatus;
	}
}
