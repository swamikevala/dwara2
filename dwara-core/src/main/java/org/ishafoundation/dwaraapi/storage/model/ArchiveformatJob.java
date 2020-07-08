package org.ishafoundation.dwaraapi.storage.model;

public class ArchiveformatJob {
	
	private StoragetypeJob storagetypeJob;

	// Common
	private int volumeBlocksize;
	private int archiveformatBlocksize;
	private String deviceName;

	// Ingest
	private String artifactSourcePath;
	private String artifactNameToBeWritten;
	
	// Restore
	private String destinationPath;
	// TAR
	private int noOfBlocksToBeRead;
	private int skipByteCount;
	// BRU
	private String filePathNameToBeRestored;
	

	// Verify
	
	
	
	public StoragetypeJob getStoragetypeJob() {
		return storagetypeJob;
	}
	public void setStoragetypeJob(StoragetypeJob storagetypeJob) {
		this.storagetypeJob = storagetypeJob;
	}
	public int getVolumeBlocksize() {
		return volumeBlocksize;
	}
	public void setVolumeBlocksize(int volumeBlocksize) {
		this.volumeBlocksize = volumeBlocksize;
	}
	public int getArchiveformatBlocksize() {
		return archiveformatBlocksize;
	}
	public void setArchiveformatBlocksize(int archiveformatBlocksize) {
		this.archiveformatBlocksize = archiveformatBlocksize;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getArtifactSourcePath() {
		return artifactSourcePath;
	}
	public void setArtifactSourcePath(String artifactSourcePath) {
		this.artifactSourcePath = artifactSourcePath;
	}
	public String getArtifactNameToBeWritten() {
		return artifactNameToBeWritten;
	}
	public void setArtifactNameToBeWritten(String artifactNameToBeWritten) {
		this.artifactNameToBeWritten = artifactNameToBeWritten;
	}
	public String getDestinationPath() {
		return destinationPath;
	}
	public void setDestinationPath(String destinationPath) {
		this.destinationPath = destinationPath;
	}
	public int getNoOfBlocksToBeRead() {
		return noOfBlocksToBeRead;
	}
	public void setNoOfBlocksToBeRead(int noOfBlocksToBeRead) {
		this.noOfBlocksToBeRead = noOfBlocksToBeRead;
	}
	public int getSkipByteCount() {
		return skipByteCount;
	}
	public void setSkipByteCount(int skipByteCount) {
		this.skipByteCount = skipByteCount;
	}
	public String getFilePathNameToBeRestored() {
		return filePathNameToBeRestored;
	}
	public void setFilePathNameToBeRestored(String filePathNameToBeRestored) {
		this.filePathNameToBeRestored = filePathNameToBeRestored;
	}	
}
