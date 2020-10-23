package org.ishafoundation.dwaraapi.storage.archiveformat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArchiveResponse {
	
	private String artifactName;
	
	private String archiveId;
	
	private int artifactStartVolumeBlock;
	
	private int artifactEndVolumeBlock;
	
	private List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();

	private Map<String, Integer> archivedFilePathNameToHeaderBlockCnt = new LinkedHashMap<String, Integer>();

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

	public int getArtifactEndVolumeBlock() {
		return artifactEndVolumeBlock;
	}

	public void setArtifactEndVolumeBlock(int artifactEndVolumeBlock) {
		this.artifactEndVolumeBlock = artifactEndVolumeBlock;
	}

	public List<ArchivedFile> getArchivedFileList() {
		return archivedFileList;
	}

	public void setArchivedFileList(List<ArchivedFile> archivedFileList) {
		this.archivedFileList = archivedFileList;
	}

	public Map<String, Integer> getArchivedFilePathNameToHeaderBlockCnt() {
		return archivedFilePathNameToHeaderBlockCnt;
	}

	public void setArchivedFilePathNameToHeaderBlockCnt(Map<String, Integer> archivedFilePathNameToHeaderBlockCnt) {
		this.archivedFilePathNameToHeaderBlockCnt = archivedFilePathNameToHeaderBlockCnt;
	}
	
}
