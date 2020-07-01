package org.ishafoundation.dwaraapi.storage.archiveformat;

import java.util.ArrayList;
import java.util.List;

public class ArchiveResponse {
	
	private String artifactName;
	
	private int artifactBlockNumber;
	
	private List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();

	public String getArtifactName() {
		return artifactName;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public int getArtifactBlockNumber() {
		return artifactBlockNumber;
	}

	public void setArtifactBlockNumber(int artifactBlockNumber) {
		this.artifactBlockNumber = artifactBlockNumber;
	}

	public List<ArchivedFile> getArchivedFileList() {
		return archivedFileList;
	}

	public void setArchivedFileList(List<ArchivedFile> archivedFileList) {
		this.archivedFileList = archivedFileList;
	}
	
}
