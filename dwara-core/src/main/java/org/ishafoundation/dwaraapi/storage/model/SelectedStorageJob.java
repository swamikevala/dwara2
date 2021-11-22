package org.ishafoundation.dwaraapi.storage.model;

import java.util.HashMap;
import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;

public class SelectedStorageJob {
	
	private StorageJob storageJob;
	
	private String deviceWwnId;
	
	private String junkFilesStagedDirName;
	
	// Verify
	private String lastWrittenArtifactName;
	private Integer artifactStartVolumeBlock;
	private Integer artifactEndVolumeBlock;

	// restore stuff
	private org.ishafoundation.dwaraapi.db.model.transactional.File file;
	private boolean useBuffering;
	private ArtifactVolume artifactVolume;
	private String filePathNameToBeRestored;
	
	// common for both verify and restore
	private List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileList;
	private HashMap<String, byte[]> filePathNameToChecksum;


	public StorageJob getStorageJob() {
		return storageJob;
	}

	public void setStorageJob(StorageJob storageJob) {
		this.storageJob = storageJob;
	}

	public String getDeviceWwnId() {
		return deviceWwnId;
	}

	public void setDeviceWwnId(String deviceWwnId) {
		this.deviceWwnId = deviceWwnId;
	}

	public String getJunkFilesStagedDirName() {
		return junkFilesStagedDirName;
	}

	public void setJunkFilesStagedDirName(String junkFilesStagedDirName) {
		this.junkFilesStagedDirName = junkFilesStagedDirName;
	}
	
	public String getLastWrittenArtifactName() {
		return lastWrittenArtifactName;
	}

	public void setLastWrittenArtifactName(String lastWrittenArtifactName) {
		this.lastWrittenArtifactName = lastWrittenArtifactName;
	}

	public Integer getArtifactStartVolumeBlock() {
		return artifactStartVolumeBlock;
	}
	
	public void setArtifactStartVolumeBlock(Integer artifactStartVolumeBlock) {
		this.artifactStartVolumeBlock = artifactStartVolumeBlock;
	}
	
	public Integer getArtifactEndVolumeBlock() {
		return artifactEndVolumeBlock;
	}
	
	public void setArtifactEndVolumeBlock(Integer artifactEndVolumeBlock) {
		this.artifactEndVolumeBlock = artifactEndVolumeBlock;
	}
	
	public org.ishafoundation.dwaraapi.db.model.transactional.File getFile() {
		return file;
	}
	
	public void setFile(org.ishafoundation.dwaraapi.db.model.transactional.File file) {
		this.file = file;
	}

	public boolean isUseBuffering() {
		return useBuffering;
	}

	public void setUseBuffering(boolean useBuffering) {
		this.useBuffering = useBuffering;
	}

	public ArtifactVolume getArtifactVolume() {
		return artifactVolume;
	}

	public void setArtifactVolume(ArtifactVolume artifactVolume) {
		this.artifactVolume = artifactVolume;
	}

	public String getFilePathNameToBeRestored() {
		return filePathNameToBeRestored;
	}

	public void setFilePathNameToBeRestored(String filePathNameToBeRestored) {
		this.filePathNameToBeRestored = filePathNameToBeRestored;
	}

	public List<org.ishafoundation.dwaraapi.db.model.transactional.File> getArtifactFileList() {
		return artifactFileList;
	}
	
	public void setArtifactFileList(List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileList) {
		this.artifactFileList = artifactFileList;
	}
	
	public HashMap<String, byte[]> getFilePathNameToChecksum() {
		return filePathNameToChecksum;
	}
	
	public void setFilePathNameToChecksum(HashMap<String, byte[]> filePathNameToChecksum) {
		this.filePathNameToChecksum = filePathNameToChecksum;
	}
}
