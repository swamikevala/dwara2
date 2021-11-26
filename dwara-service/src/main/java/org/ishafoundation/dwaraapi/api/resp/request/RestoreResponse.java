package org.ishafoundation.dwaraapi.api.resp.request;

import java.time.LocalDateTime;
import java.util.List;

public class RestoreResponse implements Comparable {
private String name;
private LocalDateTime requestedAt;
private String requestedBy;
private String status;
private String destinationPath;
private List<Tape> tapes ;
private long size;
private long eta;
private List<RestoreFile> files;
private String vpTicket;
private String priority;
private int userRequestId;
private long percentageRestored;
private String elapsedTime;
private long elapsedTimeNumber;

	public long getElapsedTimeNumber() {
		return elapsedTimeNumber;
	}

	public void setElapsedTimeNumber(long elapsedTimeNumber) {
		this.elapsedTimeNumber = elapsedTimeNumber;
	}

	public String getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(String elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public long getPercentageRestored() {
		return percentageRestored;
	}

	public void setPercentageRestored(long percentageRestored) {
		this.percentageRestored = percentageRestored;
	}

	public List<RestoreFile> getFiles() {
		return files;
	}

	public void setFiles(List<RestoreFile> files) {
		this.files = files;
	}

	public int getUserRequestId() {
		return userRequestId;
	}

	public void setUserRequestId(int userRequestId) {
		this.userRequestId = userRequestId;
	}

	public String getPriority() {
	return priority;
}
public void setPriority(String priority) {
	this.priority = priority;
}
public String getVpTicket() {
	return vpTicket;
}
public void setVpTicket(String vpTicket) {
	this.vpTicket = vpTicket;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public LocalDateTime getRequestedAt() {
	return requestedAt;
}
public void setRequestedAt(LocalDateTime requestedAt) {
	this.requestedAt = requestedAt;
}
public String getRequestedBy() {
	return requestedBy;
}
public void setRequestedBy(String requestedBy) {
	this.requestedBy = requestedBy;
}
public String getStatus() {
	return status;
}
public void setStatus(String status) {
	this.status = status;
}
public String getDestinationPath() {
	return destinationPath;
}
public void setDestinationPath(String destinationPath) {
	this.destinationPath = destinationPath;
}
public List<Tape> getTapes() {
	return tapes;
}
public void setTapes(List<Tape> tapes) {
	this.tapes = tapes;
}
public long getSize() {
	return size;
}
public void setSize(long size) {
	this.size = size;
}
public long getEta() {
	return eta;
}
public void setEta(long eta) {
	this.eta = eta;
}
public List<RestoreFile> getRestoreFiles() {
	return files;
}
public void setRestoreFiles(List<RestoreFile> restoreFiles) {
	this.files = restoreFiles;
}

	@Override
	public int compareTo(Object o) {
		return (this.getElapsedTimeNumber() < ((RestoreResponse) o).getElapsedTimeNumber() ? -1 : (this.getElapsedTimeNumber() == ((RestoreResponse) o).getElapsedTimeNumber() ? 0 : 1));
	}
}
