package org.ishafoundation.dwaraapi.storage.archiveformat;

import org.ishafoundation.dwaraapi.storage.model.ArchiveformatJob;

// TODO Instead of being an interface let this be an superclass so the subclassses just frames the command and let the super class call the common invoking the commandline part...  
public interface IArchiveformatter {
	
	// TODO : check on the blockSizeInKB parameter...

	/*
	 * 
	 * args[0] - artifactSourcePath - where the app need to changed directory to run the archive command
	 * args[1] - volumeBlocksize - configured in volume.details.blocksize
	 * args[2] - archiveformatBlocksize - configured in archiveformat.blocksize
	 * args[3] - deviceName - for tape its dataTransferElementName/ disk its the destination directory  
	 * args[4] - artifactNameToBeWritten - the artifact name which need to be written
	 */
//	public ArchiveResponse write(String artifactSourcePath, int volumeBlocksize, int archiveformatBlocksize, String deviceName, String artifactNameToBeWritten) throws Exception;
	public ArchiveResponse write(ArchiveformatJob archiveformatJob) throws Exception;
	
	
//	checksum of file list
//	artifact
//	restore
//	
//	
	public ArchiveResponse verify(ArchiveformatJob archiveformatJob) throws Exception;
	
//	//For Tar
//	public abstract ArchiveResponse restore(String destinationPath, int blockSizeInKB, String deviceName, int noOfBlocksToBeRead, int skipByteCount, String filePathNameToBeRestored) throws Exception;
//	// For Bru
//	public abstract ArchiveResponse restore(String destinationPath, int blockSizeInKB, String deviceName, String filePathNameToBeRestored) throws Exception;

	// so both together - for Bru noOfBlocksToBeRead and skipByteCount can be set to Null... and for tar filePathNameToBeRestored can be set to Null
	// how about checksum to verify???
//	public ArchiveResponse restore(String destinationPath, int volumeBlocksize, int archiveformatBlocksize, String deviceName, Integer noOfBlocksToBeRead, Integer skipByteCount, String filePathNameToBeRestored) throws Exception;
	public ArchiveResponse restore(ArchiveformatJob archiveformatJob) throws Exception;
}
