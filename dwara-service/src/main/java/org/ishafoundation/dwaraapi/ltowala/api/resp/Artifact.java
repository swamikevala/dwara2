package org.ishafoundation.dwaraapi.ltowala.api.resp;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Artifact {
	
	private String name;
	private int fileCount;
	private long totalSize; // This should be file written size = 12.5%
	private String completedAt;
	private List<File> file;
	private Volume volume;
	
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
	public String getCompletedAt() {
		return completedAt;
	}
	public void setCompletedAt(String completedAt) {
		this.completedAt = completedAt;
	}
	public List<File> getFile() {
		return file;
	}
	public void setFile(List<File> file) {
		this.file = file;
	}
	public Volume getVolume() {
		return volume;
	}
	public void setVolume(Volume volume) {
		this.volume = volume;
	}
	
}
