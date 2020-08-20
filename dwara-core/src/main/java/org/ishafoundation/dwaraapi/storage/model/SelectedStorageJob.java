package org.ishafoundation.dwaraapi.storage.model;

import java.util.HashMap;
import java.util.List;

public class SelectedStorageJob {
	
	private StorageJob storageJob;
	
	private String deviceWwnId;
	
	private String junkFilesStagedDirName;
	
	// Verify
	private Integer artifactStartVolumeBlock;
	private Integer artifactEndVolumeBlock;

	// restore stuff
	private org.ishafoundation.dwaraapi.db.model.transactional.domain.File file;
	private boolean useBuffering;
	
	// common for both verify and restore
	private List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList;
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
	
	public org.ishafoundation.dwaraapi.db.model.transactional.domain.File getFile() {
		return file;
	}
	
	public void setFile(org.ishafoundation.dwaraapi.db.model.transactional.domain.File file) {
		this.file = file;
	}

	public boolean isUseBuffering() {
		return useBuffering;
	}

	public void setUseBuffering(boolean useBuffering) {
		this.useBuffering = useBuffering;
	}

	public List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> getArtifactFileList() {
		return artifactFileList;
	}
	
	public void setArtifactFileList(List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList) {
		this.artifactFileList = artifactFileList;
	}
	
	public HashMap<String, byte[]> getFilePathNameToChecksum() {
		return filePathNameToChecksum;
	}
	
	public void setFilePathNameToChecksum(HashMap<String, byte[]> filePathNameToChecksum) {
		this.filePathNameToChecksum = filePathNameToChecksum;
	}
}
