package org.ishafoundation.dwaraapi.storage.model;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.enumreferences.Domain;

/**
 * Wraps the Job with more Storagetask related info like volume to be used, artifact/file, block details etc., 
 *
 */

public class StorageJob {

	private Job job; 
	private Volume volume; // Archive Operation determines choosing the volume - For Ingest a volume from pool of volumes fitting library size - Restore based on fileId and copyNumber...

	private Domain domain;

	private boolean encrypted;
	
	// Format stuff
	private boolean force;
	
	// Ingest stuff
	private Artifact artifact;
	private String artifactName;
	private String artifactPrefixPath;
	private long artifactSize;
	private boolean concurrentCopies;
	
	// Restore stuff
	private int fileId;
	private String destinationPath;
	private String outputFolder;
	private String targetLocationPath;
	private Integer volumeBlock;
	private Integer archiveBlock;
	private boolean restoreVerify;
	
	private int priority = 0;

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

	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
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

	public long getArtifactSize() {
		return artifactSize;
	}

	public void setArtifactSize(long artifactSize) {
		this.artifactSize = artifactSize;
	}

	public boolean isConcurrentCopies() {
		return concurrentCopies;
	}

	public void setConcurrentCopies(boolean concurrentCopies) {
		this.concurrentCopies = concurrentCopies;
	}

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

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public String getTargetLocationPath() {
		return targetLocationPath;
	}

	public void setTargetLocationPath(String targetLocationPath) {
		this.targetLocationPath = targetLocationPath;
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
	
	public boolean isRestoreVerify() {
		return restoreVerify;
	}

	public void setRestoreVerify(boolean restoreVerify) {
		this.restoreVerify = restoreVerify;
	}
	
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
}
