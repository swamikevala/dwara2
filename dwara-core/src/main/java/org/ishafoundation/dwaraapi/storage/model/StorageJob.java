package org.ishafoundation.dwaraapi.storage.model;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;

/**
 * Wraps the Job with more Storagetask related info like volume to be used  
 *
 */

public class StorageJob {

	private Job job; 
//	private Storagetype storagetype;
	private Volume volume; // Archive Operation determines choosing the volume - For Ingest a volume from pool of volumes fitting library size - Restore based on fileId and copyNumber...

	private Domain domain;
	// Ingest stuff
	private Artifact artifact;
	private String artifactName;
	private String artifactPrefixPath;
	

//	private Storageformat storageformat;
//	
//	private int priority = 0; // TODO Hardcoded for phase1
//	private boolean encrypted;
//	private int copyNumber;
//	
//	private boolean concurrentCopies;
//
//	// Restore stuff
	private int fileId;
	private String filePathname;
	private String destinationPath;
	private int block;
	private int offset;
//	
//
//	public Storagetask getStoragetask() {
//		return storagetask;
//	}
//	public void setStoragetask(Storagetask storagetask) {
//		this.storagetask = storagetask;
//	}
//	public Library getLibrary() {
//		return library;
//	}
//	public void setLibrary(Library library) {
//		this.library = library;
//	}
//	public String getLibraryToBeCopied() {
//		return libraryToBeCopied;
//	}
//	public void setLibraryToBeCopied(String libraryToBeCopied) {
//		this.libraryToBeCopied = libraryToBeCopied;
//	}
//	public String getLibraryPrefixPath() {
//		return libraryPrefixPath;
//	}
//	public void setLibraryPrefixPath(String libraryPrefixPath) {
//		this.libraryPrefixPath = libraryPrefixPath;
//	}
	public Job getJob() {
		return job;
	}
	public void setJob(Job job) {
		this.job = job;
	}
//	public Storagetype getStoragetype() {
//		return storagetype;
//	}
//	public void setStoragetype(Storagetype storagetype) {
//		this.storagetype = storagetype;
//	}
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
	

	

//	public Storageformat getStorageformat() {
//		return storageformat;
//	}
//	public void setStorageformat(Storageformat storageformat) {
//		this.storageformat = storageformat;
//	}

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
//	public int getCopyNumber() {
//		return copyNumber;
//	}
//	public void setCopyNumber(int copyNumber) {
//		this.copyNumber = copyNumber;
//	}
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
	public String getFilePathname() {
		return filePathname;
	}
	public void setFilePathname(String filePathname) {
		this.filePathname = filePathname;
	}
	public String getDestinationPath() {
		return destinationPath;
	}
	public void setDestinationPath(String destinationPath) {
		this.destinationPath = destinationPath;
	}
	public int getBlock() {
		return block;
	}
	public void setBlock(int block) {
		this.block = block;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
}
