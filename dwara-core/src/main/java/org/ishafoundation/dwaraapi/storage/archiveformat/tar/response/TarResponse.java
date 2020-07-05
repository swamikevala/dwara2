package org.ishafoundation.dwaraapi.storage.archiveformat.tar.response;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.components.File;


public class TarResponse {
	private List<File> fileList = new ArrayList<File>();
	

	public List<File> getFileList() {
		return fileList;
	}
	public void setFileList(List<File> fileList) {
		this.fileList = fileList;
	}
	@Override
	public String toString() {
		return "fileList : " + fileList;
	}
}
