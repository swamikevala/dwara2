package org.ishafoundation.dwaraapi.storage.archiveformat;

import java.util.ArrayList;
import java.util.List;

public class ArchiveResponse {
	
	private String artifactName;
	
	private String archiveId;
	
	private int artifactStartVolumeBlock;
	
	private int artifactTotalVolumeBlocks;
	
	private List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();

	

	public String getArtifactName() {
		return artifactName;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public String getArchiveId() {
		return archiveId;
	}

	public void setArchiveId(String archiveId) {
		this.archiveId = archiveId;
	}

	public int getArtifactStartVolumeBlock() {
		return artifactStartVolumeBlock;
	}

	public void setArtifactStartVolumeBlock(int artifactStartVolumeBlock) {
		this.artifactStartVolumeBlock = artifactStartVolumeBlock;
	}

	public int getArtifactTotalVolumeBlocks() {
		return artifactTotalVolumeBlocks;
	}

	public void setArtifactTotalVolumeBlocks(int artifactTotalVolumeBlocks) {
		this.artifactTotalVolumeBlocks = artifactTotalVolumeBlocks;
	}

	public List<ArchivedFile> getArchivedFileList() {
		return archivedFileList;
	}

	public void setArchivedFileList(List<ArchivedFile> archivedFileList) {
		this.archivedFileList = archivedFileList;
	}
	
}
