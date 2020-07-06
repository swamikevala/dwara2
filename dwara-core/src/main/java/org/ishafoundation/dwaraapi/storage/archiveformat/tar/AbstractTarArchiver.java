package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.TarResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.TarResponseParser;
import org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.components.File;
import org.ishafoundation.dwaraapi.storage.model.ArchiveformatJob;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractTarArchiver implements IArchiveformatter {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractTarArchiver.class);
	
	@Autowired
	private DomainUtil domainUtil;

	@Override
	public ArchiveResponse write(ArchiveformatJob archiveformatJob) throws Exception {
		String artifactSourcePath = archiveformatJob.getArtifactSourcePath();
		String artifactNameToBeWritten = archiveformatJob.getArtifactNameToBeWritten();
		
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		int archiveformatBlocksize = archiveformatJob.getArchiveBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		
		int tarBlockingFactor = volumeBlocksize/archiveformatBlocksize;
		String tarCopyCommand = "tar cvvv -R -b " + tarBlockingFactor + " -f " + deviceName + " " + artifactNameToBeWritten;
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + artifactSourcePath + " ; " + tarCopyCommand);
		
		logger.debug(" Tar write " +  tarCopyCommand);
		String commandOutput = executeCommand(commandList, artifactNameToBeWritten, volumeBlocksize);

		logger.trace("Before parsing tar response - " + commandOutput);
		TarResponseParser tarResponseParser = new TarResponseParser();
		TarResponse tarResponse = tarResponseParser.parseTarResponse(commandOutput);
		logger.trace("Parsed tar response object - " + tarResponse);	
		
		return convertTarResponseToArchiveResponse(tarResponse, archiveformatJob.getStoragetypeJob(), artifactNameToBeWritten, volumeBlocksize, archiveformatBlocksize);
	}
	
	protected abstract String executeCommand(List<String> tarCommandParamsList, String artifactName, int volumeBlocksize) throws Exception;

	private ArchiveResponse convertTarResponseToArchiveResponse(TarResponse tarResponse, StoragetypeJob storagetypeJob, String artifactName, int volumeBlocksize, int archiveformatBlocksize){
		ArchiveResponse archiveResponse = new ArchiveResponse();
		
		
		Domain domain = storagetypeJob.getStorageJob().getDomain();
		Volume volume = storagetypeJob.getStorageJob().getVolume();
		ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
		ArtifactVolume artifactVolume = domainSpecificArtifactVolumeRepository.findTopByVolumeIdOrderByIdDesc(volume.getId());

		int artifactStartVolumeBlock = 0; // Get the last/most recent artifact_volume records' end volume block and + 1(+1 because next archive starts a fresh)
		if(artifactVolume != null)
			artifactStartVolumeBlock = artifactVolume.getDetails().getStart_volume_block() + artifactVolume.getDetails().getTotal_volume_blocks() + 1; // TODO : ??? Check the + 1, previous 
		
		int artifactTotalVolumeBlocks = 0;

		
		int blockingFactor = volumeBlocksize/archiveformatBlocksize; 

		
		List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();
		List<File> taredFileList = tarResponse.getFileList();
		int lastNFinalFileArchiveBlockOffset = 0;
		Long lastNFinalFileSize = 0L;
		for (Iterator<File> iterator = taredFileList.iterator(); iterator.hasNext();) {
			File taredFile = (File) iterator.next();
			ArchivedFile archivedFile = new ArchivedFile();
			String filePathName = taredFile.getFilePathName();
			if(filePathName.endsWith("/"))
				filePathName = FilenameUtils.getFullPathNoEndSeparator(filePathName);
			archivedFile.setFilePathName(filePathName);
			int archiveBlockOffset = taredFile.getArchiveBlockOffset();
			int volumeBlockOffset =  artifactStartVolumeBlock + (archiveBlockOffset/blockingFactor);
			archivedFile.setVolumeBlockOffset(volumeBlockOffset); 
			archivedFile.setArchiveBlockOffset(archiveBlockOffset);
			archivedFileList.add(archivedFile);
			if(archiveBlockOffset > lastNFinalFileArchiveBlockOffset) { // The highest valued arhive block offset is the last file...
				lastNFinalFileArchiveBlockOffset = archiveBlockOffset;
				lastNFinalFileSize = taredFile.getFileSize();
			}
		}
		
		// TODO : better this...
		int additionalHeaderBlocks = 1;
		if(artifactName.length() > 10) {
			additionalHeaderBlocks = 3;
		}
			
		int lastFileEndArchiveBlock = lastNFinalFileArchiveBlockOffset + (int) (lastNFinalFileSize/archiveformatBlocksize) + additionalHeaderBlocks;
		if(lastNFinalFileSize%archiveformatBlocksize > 0)
			lastFileEndArchiveBlock = lastFileEndArchiveBlock + 1; // +1 because for end files we need to round the division factor above...
		
		artifactTotalVolumeBlocks = (int) (lastFileEndArchiveBlock/blockingFactor);
		if(lastFileEndArchiveBlock%blockingFactor > 0)
			artifactTotalVolumeBlocks = artifactTotalVolumeBlocks + 1;
		archiveResponse.setArtifactTotalVolumeBlocks(artifactTotalVolumeBlocks);
		archiveResponse.setArtifactStartVolumeBlock(artifactStartVolumeBlock);
		archiveResponse.setArchivedFileList(archivedFileList);
		return archiveResponse;
	}
	
	
	@Override
	public ArchiveResponse restore(ArchiveformatJob archiveformatJob) throws Exception {
		logger.debug("Tar read");
		return new ArchiveResponse();
	}
}
