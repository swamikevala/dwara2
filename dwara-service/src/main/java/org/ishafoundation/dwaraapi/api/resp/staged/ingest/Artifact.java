package org.ishafoundation.dwaraapi.api.resp.staged.ingest;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Artifact {
	
	private int artifactId;
	private String artifactclass;
	private String sequenceCode;
	private String prevSequenceCode;
	private String name;
	private int fileCount;
	private long totalSize;
	private boolean deleted;
	private String md5;
	private Integer artifactIdRef;
	
	public int getArtifactId() {
		return artifactId;
	}
	public void setArtifactId(int artifactId) {
		this.artifactId = artifactId;
	}
	public String getArtifactclass() {
		return artifactclass;
	}
	public void setArtifactclass(String artifactclass) {
		this.artifactclass = artifactclass;
	}
	public String getSequenceCode() {
		return sequenceCode;
	}
	public void setSequenceCode(String sequenceCode) {
		this.sequenceCode = sequenceCode;
	}
	public String getPrevSequenceCode() {
		return prevSequenceCode;
	}
	public void setPrevSequenceCode(String prevSequenceCode) {
		this.prevSequenceCode = prevSequenceCode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getFileCount() {
		return fileCount;
	}
	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}
	public long getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public Integer getArtifactIdRef() {
		return artifactIdRef;
	}
	public void setArtifactIdRef(Integer artifactIdRef) {
		this.artifactIdRef = artifactIdRef;
	}
}
