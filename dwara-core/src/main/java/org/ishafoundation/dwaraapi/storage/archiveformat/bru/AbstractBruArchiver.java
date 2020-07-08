package org.ishafoundation.dwaraapi.storage.archiveformat.bru;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.BruResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.BruResponseParser;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.components.File;
import org.ishafoundation.dwaraapi.storage.model.ArchiveformatJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
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
		String commandOutput = executeCommand(commandList, artifactNameToBeWritten, volumeBlocksize);

		logger.trace("Before parsing bru response - " + commandOutput);
		BruResponseParser bruResponseParser = new BruResponseParser();
		BruResponse bruResponse = bruResponseParser.parseBruResponse(commandOutput);
		logger.trace("Parsed bru response object - " + bruResponse);		
		return convertBruResponseToArchiveResponse(bruResponse, artifactNameToBeWritten, volumeBlocksize, archiveformatBlocksize);
	}
	
	protected abstract String executeCommand(List<String> bruCommandParamsList, String artifactName, int volumeBlocksize) throws Exception;
	
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
		int artifactTotalVolumeBlocks = getFileVolumeBlocksCount(bruResponse.getArchiveBlocks(), blockingFactor); // Though blocks start at 0 , we are not subtracting 1 because this is the total no. of blocks the artifact has used...
		ar.setArtifactTotalVolumeBlocks(artifactTotalVolumeBlocks);
		
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
			int archiveBlockOffset = getFileArchiveBlocksCount(archiveRunningTotalDataInBytes, archiveformatBlocksize);
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
	public ArchiveResponse restore(ArchiveformatJob archiveformatJob) throws Exception {
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		String destinationPath = archiveformatJob.getDestinationPath();
		String filePathNameToBeRestored = getFilePathNameToBeRestored(archiveformatJob.getStoragetypeJob().getStorageJob());

		int bruBufferSize = volumeBlocksize; // in bytes...
		String bruRestoreCommand = "bru -B -xvvvvvvvvv -QV -b " + bruBufferSize + " -f " + deviceName + " " + filePathNameToBeRestored;
		logger.debug("Bru restoring to " + destinationPath + " - " +  bruRestoreCommand);
	
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + destinationPath + " ; " + bruRestoreCommand);

		return new ArchiveResponse();
	}

	private String getFilePathNameToBeRestored(StorageJob storageJob) {
		Domain domain = storageJob.getDomain();
		int fileIdToBeRestored = storageJob.getFileId();
		FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
		org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = domainSpecificFileRepository.findById(fileIdToBeRestored).get();
		
		return file.getPathname();
	}
	
	// calculates the no. of Archive blocks a file consumes based on its size - excludes the file name header
	private int getFileArchiveBlocksCount(Long archiveRunningTotalDataInBytes, int archiveformatBlocksize) {
		int archiveBlocksUsedByThisFile = (int) (archiveRunningTotalDataInBytes / archiveformatBlocksize);
		if(archiveRunningTotalDataInBytes % archiveformatBlocksize > 0) // If there is remainder bits means it still occupies that block...
			archiveBlocksUsedByThisFile = archiveBlocksUsedByThisFile + 1; // +1 because - we need to round it "UP"... Eg, 99.09 = 100 or 77.75 = 78.

		return archiveBlocksUsedByThisFile;
	}

	// calculates the no. of Volume block's a file consumes based on archiveBlock
	private int getFileVolumeBlocksCount(int fileArchiveBlock, int blockingFactor) {
		int fileVolumeBlock = (int) (fileArchiveBlock / blockingFactor);
		if(fileArchiveBlock % blockingFactor > 0) // If there is remainder bits means it still occupies that block...
			fileVolumeBlock = fileVolumeBlock + 1; // +1 because - we need to round it "UP"... Eg, 99.09 = 100 or 77.75 = 78.

		return fileVolumeBlock;
	}
}
