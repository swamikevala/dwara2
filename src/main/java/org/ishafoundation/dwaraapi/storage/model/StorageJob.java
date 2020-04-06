package org.ishafoundation.dwaraapi.storage.model;

import org.ishafoundation.dwaraapi.db.model.master.Storageformat;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.model.Storagetype;
import org.ishafoundation.dwaraapi.model.Volume;
import org.ishafoundation.dwaraapi.storage.constants.StorageOperation;

public class StorageJob {
	
	private StorageOperation storageOperation; // Ingest or Restore
	
	// Ingest stuff
	private Library library;
	private String libraryToBeCopied;
	private String libraryPrefixPath;
	private Job job; 
	
	private Storagetype storagetype;
	private boolean optimizeTapeAccess = true; // by default we need tape optimisation // TODO is an attribute of storagetype. Should fit this in storagetype
	
	private int driveNo; // TODO is an attribute of storagetype. Should fit this in storagetype
	private String deviceWwid;
	
	
	private Volume volume; // Archive Operation determines choosing the volume - For Ingest a volume from pool of volumes fitting library size - Restore based on fileId and copyNumber...
	private Storageformat storageformat;
	
	private boolean driveAlreadyLoadedWithTape;
	
	private int priority = 0; // TODO Hardcoded for phase1
	private boolean encrypted;
	private int copyNumber;
	
	private boolean concurrentCopies;

	// Restore stuff
	private int fileId;
	private String filePathname;
	private String destinationPath;
	private int block;
	private int offset;
	
	public StorageOperation getStorageOperation() {
		return storageOperation;
	}
	public void setStorageOperation(StorageOperation storageOperation) {
		this.storageOperation = storageOperation;
	}
	public Library getLibrary() {
		return library;
	}
	public void setLibrary(Library library) {
		this.library = library;
	}
	public String getLibraryToBeCopied() {
		return libraryToBeCopied;
	}
	public void setLibraryToBeCopied(String libraryToBeCopied) {
		this.libraryToBeCopied = libraryToBeCopied;
	}
	public String getLibraryPrefixPath() {
		return libraryPrefixPath;
	}
	public void setLibraryPrefixPath(String libraryPrefixPath) {
		this.libraryPrefixPath = libraryPrefixPath;
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
	public String getDeviceWwid() {
		return deviceWwid;
	}
	public void setDeviceWwid(String deviceWwid) {
		this.deviceWwid = deviceWwid;
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
	public boolean isDriveAlreadyLoadedWithTape() {
		return driveAlreadyLoadedWithTape;
	}
	public void setDriveAlreadyLoadedWithTape(boolean driveAlreadyLoadedWithTape) {
		this.driveAlreadyLoadedWithTape = driveAlreadyLoadedWithTape;
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
