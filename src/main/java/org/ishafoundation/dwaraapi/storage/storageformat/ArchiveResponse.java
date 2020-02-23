package org.ishafoundation.dwaraapi.storage.storageformat;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.storage.storageformat.bru.response.components.File;

public class ArchiveResponse {
	private List<File> fileList = new ArrayList<File>();

	public List<File> getFileList() {
		return fileList;
	}

	public void setFileList(List<File> fileList) {
		this.fileList = fileList;
	}
	
}
