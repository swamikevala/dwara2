package org.ishafoundation.dwaraapi.api.resp.restore;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class File {
	private int id;
	private Integer systemRequestId;
	private int priority;
	private String artifactclass;
	private String pathname;
	private long size;
	private String checksum;
	private String checksumType;
	private String previewProxyUrl;
	private Boolean directory;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Integer getSystemRequestId() {
		return systemRequestId;
	}
	public void setSystemRequestId(Integer systemRequestId) {
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
	public String getPreviewProxyUrl() {
		return previewProxyUrl;
	}
	public void setPreviewProxyUrl(String previewProxyUrl) {
		this.previewProxyUrl = previewProxyUrl;
	}
	public Boolean getDirectory() {
		return directory;
	}
	public void setDirectory(Boolean directory) {
		this.directory = directory;
	}
}