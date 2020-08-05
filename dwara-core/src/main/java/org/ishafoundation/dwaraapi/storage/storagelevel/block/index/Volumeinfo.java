package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

public class Volumeinfo {
	 private String volumeuid;
	 private int volumeblocksize;
	 private String archiveformat;
	 private int archiveblocksize;
	 private String checksumalgorithm;
	 private String encryptionalgorithm;
	 private String artifactclassuid;
	 
	 
	public String getVolumeuid() {
		return volumeuid;
	}
	public void setVolumeuid(String volumeuid) {
		this.volumeuid = volumeuid;
	}
	public int getVolumeblocksize() {
		return volumeblocksize;
	}
	public void setVolumeblocksize(int volumeblocksize) {
		this.volumeblocksize = volumeblocksize;
	}
	public String getArchiveformat() {
		return archiveformat;
	}
	public void setArchiveformat(String archiveformat) {
		this.archiveformat = archiveformat;
	}
	public int getArchiveblocksize() {
		return archiveblocksize;
	}
	public void setArchiveblocksize(int archiveblocksize) {
		this.archiveblocksize = archiveblocksize;
	}
	public String getChecksumalgorithm() {
		return checksumalgorithm;
	}
	public void setChecksumalgorithm(String checksumalgorithm) {
		this.checksumalgorithm = checksumalgorithm;
	}
	public String getEncryptionalgorithm() {
		return encryptionalgorithm;
	}
	public void setEncryptionalgorithm(String encryptionalgorithm) {
		this.encryptionalgorithm = encryptionalgorithm;
	}
	public String getArtifactclassuid() {
		return artifactclassuid;
	}
	public void setArtifactclassuid(String artifactclassuid) {
		this.artifactclassuid = artifactclassuid;
	}
}

