package org.ishafoundation.dwaraapi.storage.archiveformat;

// TODO Instead of being an interface let this be an superclass so the subclassses just frames the command and let the super class call the common invoking the commandline part...  
public interface IArchiveformatter {
	
	// TODO : check on the blockSizeInKB parameter...

	public ArchiveResponse write(String artifactSourcePath, int blockSizeInKB, String deviceName, String artifactNameToBeWritten) throws Exception;
	
//	// For Bru
//	public abstract ArchiveResponse restore(String destinationPath, int blockSizeInKB, String deviceName, String filePathNameToBeRestored) throws Exception;
//	//For Tar
//	public abstract ArchiveResponse restore(String destinationPath, int blockSizeInKB, String deviceName, int noOfBlocksToBeRead, int skipByteCount, String filePathNameToBeRestored) throws Exception;

	// so both together - for Bru noOfBlocksToBeRead and skipByteCount can be set to Null...
	public ArchiveResponse restore(String destinationPath, int blockSizeInKB, String deviceName, Integer noOfBlocksToBeRead, Integer skipByteCount, String filePathNameToBeRestored) throws Exception;
}
