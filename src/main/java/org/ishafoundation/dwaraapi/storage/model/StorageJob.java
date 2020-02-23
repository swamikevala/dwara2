package org.ishafoundation.dwaraapi.storage.model;

import org.ishafoundation.dwaraapi.db.model.master.storage.Storageformat;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.model.Storagetype;
import org.ishafoundation.dwaraapi.model.Volume;
import org.ishafoundation.dwaraapi.storage.constants.StorageOperation;

public class StorageJob {
	
	private StorageOperation storageOperation; // Ingest or Restore
	private int libraryId;
	private String librarypathToBeCopied;
	private Job job; 
	
	private Storagetype storagetype;
	private boolean optimizeTapeAccess; // TODO is an attribute of storagetype. Should fit this in storagetype
	
	private int driveNo; // TODO is an attribute of storagetype. Should fit this in storagetype
	
	private Volume volume; // Archive Operation determines choosing the volume - For Ingest a volume from pool of volumes fitting library size - Restore based on fileId and copyNumber...
	private Storageformat storageformat;
	
	private int priority;
	private boolean encrypted;
	private int copyNumber;
	
	private boolean concurrentCopies;
	private boolean noFileRecords;
	
	private int fileId;
	private String destinationPath;
	private int block;
	private int offset;
	
	public StorageOperation getStorageOperation() {
		return storageOperation;
	}
	public void setStorageOperation(StorageOperation storageOperation) {
		this.storageOperation = storageOperation;
	}
	public int getLibraryId() {
		return libraryId;
	}
	public void setLibraryId(int libraryId) {
		this.libraryId = libraryId;
	}
	public String getLibrarypathToBeCopied() {
		return librarypathToBeCopied;
	}
	public void setLibrarypathToBeCopied(String librarypathToBeCopied) {
		this.librarypathToBeCopied = librarypathToBeCopied;
	}
	public Job getJob() {
		return job;
	}
	public void setJob(Job job) {
		this.job = job;
	}
	public Storagetype getStoragetype() {
		return storagetype;
	}
	public void setStoragetype(Storagetype storagetype) {
		this.storagetype = storagetype;
	}
	public boolean isOptimizeTapeAccess() {
		return optimizeTapeAccess;
	}
	public void setOptimizeTapeAccess(boolean optimizeTapeAccess) {
		this.optimizeTapeAccess = optimizeTapeAccess;
	}
	public int getDriveNo() {
		return driveNo;
	}
	public void setDriveNo(int driveNo) {
		this.driveNo = driveNo;
	}
	public Volume getVolume() {
		return volume;
	}
	public void setVolume(Volume volume) {
		this.volume = volume;
	}
	public Storageformat getStorageformat() {
		return storageformat;
	}
	public void setStorageformat(Storageformat storageformat) {
		this.storageformat = storageformat;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public boolean isEncrypted() {
		return encrypted;
	}
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}
	public int getCopyNumber() {
		return copyNumber;
	}
	public void setCopyNumber(int copyNumber) {
		this.copyNumber = copyNumber;
	}
	public boolean isConcurrentCopies() {
		return concurrentCopies;
	}
	public void setConcurrentCopies(boolean concurrentCopies) {
		this.concurrentCopies = concurrentCopies;
	}
	public boolean isNoFileRecords() {
		return noFileRecords;
	}
	public void setNoFileRecords(boolean noFileRecords) {
		this.noFileRecords = noFileRecords;
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
