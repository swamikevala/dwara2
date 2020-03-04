package org.ishafoundation.dwaraapi.storage.storageformat;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.storage.storageformat.ArchivedFile;

public class ArchiveResponse {
	private List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();

	public List<ArchivedFile> getArchivedFileList() {
		return archivedFileList;
	}

	public void setArchivedFileList(List<ArchivedFile> archivedFileList) {
		this.archivedFileList = archivedFileList;
	}
	
}
