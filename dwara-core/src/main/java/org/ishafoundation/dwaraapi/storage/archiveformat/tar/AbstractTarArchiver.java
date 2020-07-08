package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File1;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File2;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.TarResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.TarResponseParser;
import org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.components.File;
import org.ishafoundation.dwaraapi.storage.model.ArchiveformatJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
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
		int archiveformatBlocksize = archiveformatJob.getArchiveformatBlocksize();
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

		int artifactStartVolumeBlock = getArtifactStartVolumeBlock(storagetypeJob);
		
		int artifactTotalVolumeBlocks = 0;
		
		int blockingFactor = TarBlockCalculatorUtil.getBlockingFactor(archiveformatBlocksize, volumeBlocksize); 
		
		List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();
		List<File> taredFileList = tarResponse.getFileList();
		File lastTaredFile = null;
		for (Iterator<File> iterator = taredFileList.iterator(); iterator.hasNext();) {
			File taredFile = (File) iterator.next();
			ArchivedFile archivedFile = new ArchivedFile();
			String filePathName = taredFile.getFilePathName();
			if(filePathName.endsWith("/"))
				filePathName = FilenameUtils.getFullPathNoEndSeparator(filePathName);
			archivedFile.setFilePathName(filePathName);
			int archiveBlockOffset = taredFile.getArchiveBlockOffset();
			int volumeBlockOffset =  artifactStartVolumeBlock + archiveBlockOffset/blockingFactor;// TODO ?? should this be TarBlockCalculatorUtil.getVolumeBlocksCount(archiveBlockOffset, blockingFactor); // running total of volume block start
			archivedFile.setVolumeBlockOffset(volumeBlockOffset); 
			archivedFile.setArchiveBlockOffset(archiveBlockOffset);
			archivedFileList.add(archivedFile);
			if(!iterator.hasNext())
				lastTaredFile = taredFile;
		}
		
		artifactTotalVolumeBlocks = TarBlockCalculatorUtil.getFileVolumeBlockEnd(lastTaredFile.getFilePathName(), lastTaredFile.getArchiveBlockOffset(), lastTaredFile.getFileSize(), archiveformatBlocksize, blockingFactor);
		
		archiveResponse.setArtifactTotalVolumeBlocks(artifactTotalVolumeBlocks);
		archiveResponse.setArtifactStartVolumeBlock(artifactStartVolumeBlock);
		archiveResponse.setArchivedFileList(archivedFileList);
		return archiveResponse;
	}

	protected int getArtifactStartVolumeBlock(StoragetypeJob storagetypeJob) {
		Domain domain = storagetypeJob.getStorageJob().getDomain();
		Volume volume = storagetypeJob.getStorageJob().getVolume();
		ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
		ArtifactVolume artifactVolume = domainSpecificArtifactVolumeRepository.findTopByVolumeIdOrderByIdDesc(volume.getId());
	
		int artifactStartVolumeBlock = 0; // Get the last/most recent artifact_volume records' end volume block and + 1(+1 because next archive starts a fresh)
		if(artifactVolume != null)
			artifactStartVolumeBlock =  getArtifactStartVolumeBlock(artifactVolume);
		
		return artifactStartVolumeBlock;
	}
	
	private int	getArtifactStartVolumeBlock(ArtifactVolume artifactVolume) {
		return artifactVolume.getDetails().getStart_volume_block() + artifactVolume.getDetails().getTotal_volume_blocks();// + 1; not needed as we start on 0 // TODO: verify this === + 1, because the next file starts in a fresh block...
	}

	@Override
	public ArchiveResponse restore(ArchiveformatJob archiveformatJob) throws Exception {
		archiveformatJob = getDecoratedArchiveformatJobForRestore(archiveformatJob);
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		String destinationPath = archiveformatJob.getDestinationPath();
		
		int bufferSize = volumeBlocksize;
		
		int noOfTapeBlocksToBeRead = archiveformatJob.getNoOfBlocksToBeRead();
		
		int skipByteCount = archiveformatJob.getSkipByteCount();
		
		String restoreCommand = "dd if=" + deviceName + " bs=" + bufferSize	+ " count=" + noOfTapeBlocksToBeRead;
		logger.debug("Tar restoration - " +  restoreCommand);
		logger.debug("Will be skipping - " +  skipByteCount);
		logger.debug("Will be restoring to - " + destinationPath);
		
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add(restoreCommand);

		return new ArchiveResponse();
	}
	
	private ArchiveformatJob getDecoratedArchiveformatJobForRestore(ArchiveformatJob archiveformatJob) throws Exception {
		StorageJob storageJob = archiveformatJob.getStoragetypeJob().getStorageJob();
    	Domain domain = storageJob.getDomain();
    	int fileIdToBeRestored = storageJob.getFileId();
		FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
		org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = domainSpecificFileRepository.findById(fileIdToBeRestored).get();
		
		// for tar
		int noOfBlocksToBeRead = 0;
		int skipByteCount = 0;
		Volume volume = storageJob.getVolume();
		
		Long fileSize = file.getSize();
		int archiveformatBlocksize = archiveformatJob.getArchiveformatBlocksize();
		int blockingFactor = TarBlockCalculatorUtil.getBlockingFactor(archiveformatBlocksize, archiveformatJob.getVolumeBlocksize()); 
		String filePathname = file.getPathname();
		int seekedVolumeBlock = storageJob.getVolumeBlock();
		if(StringUtils.isBlank(FilenameUtils.getExtension(filePathname))) { //if file is folder

			Artifact artifact = null;
			if(file instanceof File1) {
				File1 file1 = (File1) file;
				artifact = file1.getArtifact1();
			}else if(file instanceof File2) {
				File2 file2 = (File2) file;
				artifact = file2.getArtifact2();
			}else {
				throw new Exception("File" + (domain.ordinal() + 1) + " not supported yet");
			}
			
			if(artifact.getName().equals(filePathname)) {// if file is artifact
				ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
				ArtifactVolume artifactVolume = domainUtil.getDomainSpecificArtifactVolume(domain, artifact.getId(), volume.getId());
				noOfBlocksToBeRead = artifactVolume.getDetails().getTotal_volume_blocks();
				skipByteCount = 0; // bang on artifact
			}else {
				String domainSpecificArtifactTableName = artifact.getClass().getSimpleName();
				domainSpecificArtifactTableName = StringUtils.substringBefore(domainSpecificArtifactTableName, "$");
		    	Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod(FileRepository.FIND_ALL_BY_ARTIFACT_ID.replace("<<DOMAIN_SPECIFIC_ARTIFACT>>", domainSpecificArtifactTableName), int.class);
				List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileList = (List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File>) fileDaoFindAllBy.invoke(domainSpecificFileRepository, artifact.getId());
				int firstFileVolumeBlock = 0;
				int lastFileVolumeBlock = 0;
				int lastFileEndVolumeBlock = 0;
				for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : fileList) {
					String nthArtifactFilePathname = nthFile.getPathname();//FilenameUtils.separatorsToUnix(nthArtifactFile.getPathname());
					if(nthArtifactFilePathname.startsWith(filePathname)) {
						FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, nthFile.getId(), volume.getId());// lets just let users use the util consistently
						Integer filevolumeBlock = fileVolume.getVolumeBlock();
						Integer filearchiveBlock = fileVolume.getArchiveBlock();
						if(nthArtifactFilePathname.equals(filePathname)) { // first file
							firstFileVolumeBlock = filevolumeBlock;
							skipByteCount = TarBlockCalculatorUtil.getSkipByteCount(filePathname, filearchiveBlock, archiveformatBlocksize, seekedVolumeBlock, blockingFactor);
						}
						else if(filevolumeBlock > lastFileVolumeBlock) {
							lastFileVolumeBlock = filevolumeBlock;
							lastFileEndVolumeBlock = TarBlockCalculatorUtil.getFileVolumeBlockEnd(nthArtifactFilePathname, filearchiveBlock, nthFile.getSize(), archiveformatBlocksize, blockingFactor);
						}
					}
				}
				//noOfBlocksToBeRead = (lastFileVolumeBlock + lastfileEndolumeBlock) - firstFileVolumeBlock;
				noOfBlocksToBeRead = lastFileEndVolumeBlock - firstFileVolumeBlock;
			}
			 
		}else {
//			int fileEndArchiveBlock = TarBlockCalculatorUtil.getFileArchiveBlocksCount(fileSize, archiveBlocksize);
//			noOfBlocksToBeRead = TarBlockCalculatorUtil.getFileVolumeBlocksCount(fileEndArchiveBlock, blockingFactor);

			FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, fileIdToBeRestored, volume.getId());// lets just let users use the util consistently
			Integer filearchiveBlock = fileVolume.getArchiveBlock();
			noOfBlocksToBeRead = TarBlockCalculatorUtil.getFileVolumeBlockEnd(filePathname, filearchiveBlock, fileSize, archiveformatBlocksize, blockingFactor) - seekedVolumeBlock;
			skipByteCount = TarBlockCalculatorUtil.getSkipByteCount(filePathname, filearchiveBlock, archiveformatBlocksize, seekedVolumeBlock, blockingFactor); // TODO : should this be path name or file name? 		
		}
		
		archiveformatJob.setNoOfBlocksToBeRead(noOfBlocksToBeRead);
		archiveformatJob.setSkipByteCount(skipByteCount);
		return archiveformatJob;
	}

	

}
