package org.ishafoundation.dwaraapi.storage.archiveformat;

import java.util.ArrayList;
import java.util.List;

public class ArchiveResponse {
	
	private String libraryName;
	
	private int libraryBlockNumber;
	
	private List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();

	public String getLibraryName() {
		return libraryName;
	}

	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}

	public int getLibraryBlockNumber() {
		return libraryBlockNumber;
	}

	public void setLibraryBlockNumber(int libraryBlockNumber) {
		this.libraryBlockNumber = libraryBlockNumber;
	}

	public List<ArchivedFile> getArchivedFileList() {
		return archivedFileList;
	}

	public void setArchivedFileList(List<ArchivedFile> archivedFileList) {
		this.archivedFileList = archivedFileList;
	}
	
}
