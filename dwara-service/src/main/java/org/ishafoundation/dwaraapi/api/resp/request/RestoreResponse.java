package org.ishafoundation.dwaraapi.api.resp.request;

import java.time.LocalDateTime;
import java.util.List;

public class RestoreResponse  {
private String name;
private LocalDateTime requestedAt;
private String requestedBy;
private String status;
private String destinationPath;
private List<Tape> tapes ;
private long size;
private String eta;
private List<RestoreFile> files;
private String vpTicket;
private int priority;
public int getPriority() {
	return priority;
}
public void setPriority(int priority) {
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
public String getEta() {
	return eta;
}
public void setEta(String eta) {
	this.eta = eta;
}
public List<RestoreFile> getRestoreFiles() {
	return files;
}
public void setRestoreFiles(List<RestoreFile> restoreFiles) {
	this.files = restoreFiles;
}

}
