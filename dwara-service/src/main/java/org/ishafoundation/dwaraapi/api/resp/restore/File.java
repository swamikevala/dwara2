package org.ishafoundation.dwaraapi.api.resp.restore;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class File {
	private int id;
	private int systemRequestId;
	private int priority;
	private String artifactclass;
	private String pathname;
	private long size;
	private String checksum;
	private String checksumType;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSystemRequestId() {
		return systemRequestId;
	}
	public void setSystemRequestId(int systemRequestId) {
		this.systemRequestId = systemRequestId;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public String getArtifactclass() {
		return artifactclass;
	}
	public void setArtifactclass(String artifactclass) {
		this.artifactclass = artifactclass;
	}
	public String getPathname() {
		return pathname;
	}
	public void setPathname(String pathname) {
		this.pathname = pathname;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getChecksum() {
		return checksum;
	}
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
	public String getChecksumType() {
		return checksumType;
	}
	public void setChecksumType(String checksumType) {
		this.checksumType = checksumType;
	}
}