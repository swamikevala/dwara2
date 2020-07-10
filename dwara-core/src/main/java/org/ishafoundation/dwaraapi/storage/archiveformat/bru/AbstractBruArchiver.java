package org.ishafoundation.dwaraapi.storage.archiveformat.bru;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.BruResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.BruResponseParser;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.components.File;
import org.ishafoundation.dwaraapi.storage.model.ArchiveformatJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.utils.Md5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractBruArchiver implements IArchiveformatter {

	private static final Logger logger = LoggerFactory.getLogger(AbstractBruArchiver.class);
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Override
	public ArchiveResponse write(ArchiveformatJob archiveformatJob) throws Exception {
		String artifactSourcePath = archiveformatJob.getArtifactSourcePath();
		String artifactNameToBeWritten = archiveformatJob.getArtifactNameToBeWritten();
		
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		int archiveformatBlocksize = archiveformatJob.getArchiveformatBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		
		int bruBufferSize = volumeBlocksize; // in bytes...
		
		String bruCopyCommand = "bru -B -clOjvvvvvvvvv -QX -b " + bruBufferSize + " -f " + deviceName + " " + artifactNameToBeWritten;
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + artifactSourcePath + " ; " + bruCopyCommand);
		
		logger.debug(" Bru write " +  bruCopyCommand);
		String commandOutput = executeWriteCommand(commandList, artifactNameToBeWritten, volumeBlocksize);

		logger.trace("Before parsing bru response - " + commandOutput);
		BruResponseParser bruResponseParser = new BruResponseParser();
		BruResponse bruResponse = bruResponseParser.parseBruResponse(commandOutput);
		logger.trace("Parsed bru response object - " + bruResponse);		
		return convertBruResponseToArchiveResponse(bruResponse, artifactNameToBeWritten, volumeBlocksize, archiveformatBlocksize);
	}
	
	protected abstract String executeWriteCommand(List<String> bruCommandParamsList, String artifactName, int volumeBlocksize) throws Exception;
	
	protected ArchiveResponse convertBruResponseToArchiveResponse(BruResponse bruResponse, String artifactName, int volumeBlocksize, int archiveformatBlocksize){
		ArchiveResponse ar = new ArchiveResponse();
		ar.setArchiveId(bruResponse.getArchiveId());
		ar.setArtifactName(artifactName);
		
		/*
		1 tape block = N archive blocks
		lets say
		1 tape block = 262144 bytes
		1 archive block = 2048 bytes
		
		so N = 262144/2048 = 128
		
		lets call N - the blocking factor here...
		
		so,
			128 archive blocks = one tape block
			bruResponse.getArchiveBlocks() = how many tape blocks?
			
			// 1 tape block * bruResponse.getArchiveBlocks() = how many tape blocks? * 128
			bruResponse.getArchiveBlocks()/128 = how many tape blocks?
		*/		
		int blockingFactor = volumeBlocksize/archiveformatBlocksize; 
		int artifactEndVolumeBlock = (int) Math.ceil(bruResponse.getArchiveBlocks()/blockingFactor);
		if(artifactEndVolumeBlock > 0)
			artifactEndVolumeBlock = artifactEndVolumeBlock - 1;
		ar.setArtifactEndVolumeBlock(artifactEndVolumeBlock);
		
		int artifactStartVolumeBlock = 0;
		List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();
		List<File> bruedFileList = bruResponse.getFileList();
		
		for (Iterator<File> iterator = bruedFileList.iterator(); iterator.hasNext();) {
			File bruedFile = (File) iterator.next();
			ArchivedFile af = new ArchivedFile();
		
			String filePathname = bruedFile.getFilePathName();
			af.setFilePathName(filePathname);
			
			// volumeBlockOffset starts with 0 and not -1
			int volumeBlockOffset = bruedFile.getVolumeBlockOffset() + 1; // +1, because bru "copy" responds with -1 block for BOT, while bru "t - table of contents"/"x - extraction" shows the block as 0 for same. Also while seek +1 followed by t/x returns faster results...
			if(filePathname.equals(artifactName)) {
				artifactStartVolumeBlock = volumeBlockOffset;
			}
			
			af.setVolumeBlockOffset(volumeBlockOffset); 
			Long archiveRunningTotalDataInKB =  bruedFile.getArchiveRunningTotalDataInKB();
			Long archiveRunningTotalDataInBytes = archiveRunningTotalDataInKB * 1024; // KB to bytes...
			int archiveBlockOffset = (int) Math.ceil(archiveRunningTotalDataInBytes/archiveformatBlocksize);
			if(archiveBlockOffset > 0)
				archiveBlockOffset = archiveBlockOffset - 1; // - 1 because the first block starts with 0...
			af.setArchiveBlockOffset(archiveBlockOffset);
			archivedFileList.add(af);
		}
		ar.setArchivedFileList(archivedFileList);

		ar.setArtifactStartVolumeBlock(artifactStartVolumeBlock);
		return ar;
	}

	@Override
	public ArchiveResponse verify(ArchiveformatJob archiveformatJob) throws Exception {
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		String destinationPath = archiveformatJob.getDestinationPath();
		
		StorageJob storageJob = archiveformatJob.getStoragetypeJob().getStorageJob();
		String filePathNameToBeRestored = storageJob.getArtifact().getName();
		
		List<String> commandList = frameRestoreCommand(volumeBlocksize, deviceName, destinationPath, filePathNameToBeRestored);
		
		executeRestoreCommand(commandList);
		
		boolean success = compareChecksum(storageJob.getArtifactFileList(), destinationPath, filePathNameToBeRestored, storageJob.getVolume().getChecksumtype());
		if(!success)
			throw new Exception("Verification failed");
		return new ArchiveResponse();
	}
	
	private boolean compareChecksum(List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList,
			String destinationPath, String filePathNameToBeVerified, Checksumtype checksumtype) {
		
		// caching the source file' checksum...
		HashMap<String, byte[]> filePathNameToChecksumObj = new LinkedHashMap<String, byte[]>();
		for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : artifactFileList) {
			String filePathName = nthFile.getPathname();
			byte[] checksum = nthFile.getChecksum();
			filePathNameToChecksumObj.put(filePathName, checksum);
		}
		
		// calculating the restored file' checksum
		boolean verify = true;
		java.io.File artifactToBeVerified = new java.io.File(destinationPath + java.io.File.separator + filePathNameToBeVerified);
		Collection<java.io.File> artifactFileAndDirsList = FileUtils.listFilesAndDirs(artifactToBeVerified, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		
		for (java.io.File file : artifactFileAndDirsList) {
			if(file.isDirectory())
				continue;
			byte[] checksum = Md5Util.getChecksum(file, checksumtype);
			
			String filePathName = file.getAbsolutePath();
			filePathName = filePathName.replace(destinationPath + java.io.File.separator, "");
			byte[] originalChecksum = filePathNameToChecksumObj.get(filePathName);
			
			if(!Arrays.equals(checksum, originalChecksum)) {
				verify = false;
				logger.error("Checksum mismatch " + filePathName + " restored checksum : " + checksum + " original checksum : " + originalChecksum);
			}	
		}
		logger.debug("verification status " + verify);
		artifactToBeVerified.delete();
		logger.trace(artifactToBeVerified + " deleted");
		return verify;
	}
	
	@Override
	public ArchiveResponse restore(ArchiveformatJob archiveformatJob) throws Exception {
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		String destinationPath = archiveformatJob.getDestinationPath();
		String filePathNameToBeRestored = getFilePathNameToBeRestored(archiveformatJob.getStoragetypeJob().getStorageJob());

		List<String> commandList = frameRestoreCommand(volumeBlocksize, deviceName, destinationPath, filePathNameToBeRestored);

		executeRestoreCommand(commandList);
		
		return new ArchiveResponse();
	}

	private List<String> frameRestoreCommand(int volumeBlocksize, String deviceName, String destinationPath, String filePathNameToBeRestored) {
		// FIXME: mbuffer -s 1M -m 2G -i /dev/st1 | bru -xvvvvvvvvv -b1024k -QV -C ***-f*** - 25079_Cauvery-Calling_Day15-Crowd-Shots-At-Closing-Event_CODISSIA-Cbe_17-Sep-2019_Z280V_B-Rolls
		String bruRestoreCommand = "bru -B -xvvvvvvvvv -QV -b " + volumeBlocksize + " -f " + deviceName + " " + filePathNameToBeRestored;
		logger.debug("Bru restoring to " + destinationPath + " - " +  bruRestoreCommand);
	
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + destinationPath + " ; " + bruRestoreCommand);
		
		return commandList;
	}
	
	protected abstract String executeRestoreCommand(List<String> restoreCommandParamsList) throws Exception;
	
	private String getFilePathNameToBeRestored(StorageJob storageJob) {
		Domain domain = storageJob.getDomain();
		int fileIdToBeRestored = storageJob.getFileId();
		FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
		org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = domainSpecificFileRepository.findById(fileIdToBeRestored).get();
		
		return file.getPathname();
	}
}
