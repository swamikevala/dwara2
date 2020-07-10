package org.ishafoundation.dwaraapi.storage.model;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.enumreferences.Domain;

/**
 * Wraps the Job with more Storagetask related info like volume to be used  
 *
 */

public class StorageJob {

	private Job job; 
	private Volume volume; // Archive Operation determines choosing the volume - For Ingest a volume from pool of volumes fitting library size - Restore based on fileId and copyNumber...

	private Domain domain;
	
	// Ingest stuff
	private Artifact artifact;
	private String artifactName;
	private String artifactPrefixPath;

	// Verify
	private Integer artifactStartVolumeBlock;
	private Integer artifactEndVolumeBlock;
	private List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList;
	
//	private int priority = 0; // TODO Hardcoded for phase1
//	private boolean encrypted;
//	private boolean concurrentCopies;
//
	// Restore stuff
	private int fileId;
	private String destinationPath;
	private Integer volumeBlock;
	private Integer archiveBlock;

	public Job getJob() {
		return job;
	}
	public void setJob(Job job) {
		this.job = job;
	}
	public Volume getVolume() {
		return volume;
	}
	public void setVolume(Volume volume) {
		this.volume = volume;
	}
	public Domain getDomain() {
		return domain;
	}
	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	public Artifact getArtifact() {
		return artifact;
	}
	public void setArtifact(Artifact artifact) {
		this.artifact = artifact;
	}
	public String getArtifactName() {
		return artifactName;
	}
	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}
	public String getArtifactPrefixPath() {
		return artifactPrefixPath;
	}
	public void setArtifactPrefixPath(String artifactPrefixPath) {
		this.artifactPrefixPath = artifactPrefixPath;
	}
	


//	public int getPriority() {
//		return priority;
//	}
//	public void setPriority(int priority) {
//		this.priority = priority;
//	}
//	public boolean isEncrypted() {
//		return encrypted;
//	}
//	public void setEncrypted(boolean encrypted) {
//		this.encrypted = encrypted;
//	}

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
	public List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> getArtifactFileList() {
		return artifactFileList;
	}
	public void setArtifactFileList(List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList) {
		this.artifactFileList = artifactFileList;
	}
	//	public boolean isConcurrentCopies() {
//		return concurrentCopies;
//	}
//	public void setConcurrentCopies(boolean concurrentCopies) {
//		this.concurrentCopies = concurrentCopies;
//	}
	public int getFileId() {
		return fileId;
	}
	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	public String getDestinationPath() {
		return destinationPath;
	}
	public void setDestinationPath(String destinationPath) {
		this.destinationPath = destinationPath;
	}
	public Integer getVolumeBlock() {
		return volumeBlock;
	}
	public void setVolumeBlock(Integer volumeBlock) {
		this.volumeBlock = volumeBlock;
	}
	public Integer getArchiveBlock() {
		return archiveBlock;
	}
	public void setArchiveBlock(Integer archiveBlock) {
		this.archiveBlock = archiveBlock;
	}
}
