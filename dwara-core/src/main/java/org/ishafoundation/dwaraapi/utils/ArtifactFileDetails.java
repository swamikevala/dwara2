package org.ishafoundation.dwaraapi.utils;

import java.io.File;
import java.util.Collection;

public class ArtifactFileDetails {
	
	private int count;
	private long totalSize;
	private Collection<File> fileList;
	
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public long getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}
	public Collection<File> getFileList() {
		return fileList;
	}
	public void setFileList(Collection<File> fileList) {
		this.fileList = fileList;
	}
	
}