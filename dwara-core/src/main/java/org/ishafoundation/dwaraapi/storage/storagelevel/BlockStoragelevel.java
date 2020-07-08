package org.ishafoundation.dwaraapi.storage.storagelevel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Archiveformat;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File1;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File2;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.archiveformat.tar.TarBlockCalculatorUtil;
import org.ishafoundation.dwaraapi.storage.model.ArchiveformatJob;
import org.ishafoundation.dwaraapi.storage.model.DiskJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("block"+DwaraConstants.STORAGELEVEL_SUFFIX)
//@Profile({ "!dev & !stage" })
public class BlockStoragelevel implements IStoragelevel {

	private static final Logger logger = LoggerFactory.getLogger(BlockStoragelevel.class);
	
	@Autowired
	private Map<String, IArchiveformatter> iArchiveformatterMap;

	@Autowired
	private DomainUtil domainUtil;
	
	@Override
	public StorageResponse format(StoragetypeJob job) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StorageResponse write(StoragetypeJob storagetypeJob) throws Exception{
		StorageResponse storageResponse = new StorageResponse();
		StorageJob storageJob = storagetypeJob.getStorageJob();
		logger.debug("Writing blocks");
		Archiveformat archiveformat = storageJob.getVolume().getArchiveformat();
    	IArchiveformatter archiveFormatter = iArchiveformatterMap.get(archiveformat.getId() + DwaraConstants.ARCHIVER_SUFFIX);
    	
    	ArchiveformatJob archiveformatJob = instantiateArchiveJobWithCommonFields(storagetypeJob);
//		String artifactSourcePath = storageJob.getArtifactPrefixPath();
//		String artifactNameToBeWritten = storageJob.getArtifactName();
//		
//		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
//		int archiveBlocksize = archiveformatJob.getArchiveBlocksize();
//		String deviceName = archiveformatJob.getDeviceName();
//		try {
//			 ArchiveResponse archiveResponse = archiveFormatter.write(artifactSourcePath, volumeBlocksize, archiveBlocksize, deviceName, artifactNameToBeWritten);

    	archiveformatJob.setArtifactSourcePath(storageJob.getArtifactPrefixPath());
    	archiveformatJob.setArtifactNameToBeWritten(storageJob.getArtifactName());
		
		try {
			 ArchiveResponse archiveResponse = archiveFormatter.write(archiveformatJob);
	 
			 storageResponse.setArchiveResponse(archiveResponse);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return storageResponse;
	}

	@Override
	public StorageResponse verify(StoragetypeJob storagetypeJob) throws Exception{
		logger.debug("Verifying blocks");
		Archiveformat archiveformat = storagetypeJob.getStorageJob().getVolume().getArchiveformat();
    	IArchiveformatter archiveFormatter = iArchiveformatterMap.get(archiveformat.getId() + DwaraConstants.ARCHIVER_SUFFIX);
//    	ArchiveResponse archiveResponse = archiveFormatter.verify();
    	return null;
	}

	@Override
	public StorageResponse finalize(StoragetypeJob job) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public StorageResponse restore(StoragetypeJob storagetypeJob) throws Exception {
		logger.debug("Reading blocks");
		StorageResponse storageResponse = new StorageResponse();
		StorageJob storageJob = storagetypeJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		Archiveformat archiveformat = volume.getArchiveformat();
    	IArchiveformatter archiveFormatter = iArchiveformatterMap.get(archiveformat.getId() + DwaraConstants.ARCHIVER_SUFFIX);
    	
    	ArchiveformatJob archiveformatJob = instantiateArchiveJobWithCommonFields(storagetypeJob);
    	archiveformatJob.setDestinationPath(storageJob.getDestinationPath());
    	
  /*
    	Domain domain = storageJob.getDomain();
    	int fileIdToBeRestored = storageJob.getFileId();
		FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
		File file = domainSpecificFileRepository.findById(fileIdToBeRestored).get();
		
		// for bru
		String filePathname = file.getPathname();
		archiveformatJob.setFilePathNameToBeRestored(filePathname);
		
		// for tar
		int noOfBlocksToBeRead = 0;
		int skipByteCount = 0;
		
		Long fileSize = file.getSize();
		int archiveBlocksize = archiveformatJob.getArchiveBlocksize();
		int blockingFactor = TarBlockCalculatorUtil.getBlockingFactor(archiveBlocksize, archiveformatJob.getVolumeBlocksize()); 

		if(StringUtils.isBlank(FilenameUtils.getExtension(filePathname))) { //if file is folder
			// TODO : move this out of here to some domain specific class
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
				skipByteCount = 0;
			}else {
		    	Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod(FileRepository.FIND_ALL_BY_ARTIFACT_ID.replace("<<DOMAIN_SPECIFIC_ARTIFACT>>", artifact.getClass().getSimpleName()), int.class);
				List<File> fileList = (List<File>) fileDaoFindAllBy.invoke(domainSpecificFileRepository, artifact.getId());
				int firstFileVolumeBlock = 0;
				int lastFileVolumeBlock = 0;
				int lastFileEndVolumeBlock = 0;
				for (File nthFile : fileList) {
					String nthArtifactFilePathname = nthFile.getPathname();//FilenameUtils.separatorsToUnix(nthArtifactFile.getPathname());
					if(nthArtifactFilePathname.startsWith(filePathname)) {
						FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, nthFile.getId(), volume.getId());// lets just let users use the util consistently
						Integer volumeBlock = fileVolume.getVolumeBlock();
						Integer archiveBlock = fileVolume.getArchiveBlock();
						if(nthArtifactFilePathname.equals(filePathname)) { // first file
							firstFileVolumeBlock = volumeBlock;
							skipByteCount = TarBlockCalculatorUtil.getSkipByteCount(archiveBlock, volumeBlock, blockingFactor, file.getPathname()); // TODO : should this be path name or file name?
						}
						else if(volumeBlock > lastFileVolumeBlock) {
							lastFileVolumeBlock = volumeBlock;
							lastFileEndVolumeBlock = TarBlockCalculatorUtil.getFileEndVolumeBlock(nthArtifactFilePathname, archiveBlock, fileSize, archiveBlocksize, blockingFactor);
						}
					}
				}
				//noOfBlocksToBeRead = (lastFileVolumeBlock + lastfileEndolumeBlock) - firstFileVolumeBlock;
				noOfBlocksToBeRead = lastFileEndVolumeBlock - firstFileVolumeBlock;
			}
			 
		}else {
			int fileEndArchiveBlock = TarBlockCalculatorUtil.getArchiveBlocksCount(fileSize, archiveBlocksize);
			noOfBlocksToBeRead = TarBlockCalculatorUtil.getVolumeBlocksCount(fileEndArchiveBlock, blockingFactor);
			
			FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, fileIdToBeRestored, volume.getId());// lets just let users use the util consistently
			
			int blockNumberToSeek = storageJob.getVolumeBlock();
			
			skipByteCount = TarBlockCalculatorUtil.getSkipByteCount(fileVolume.getArchiveBlock(), blockNumberToSeek, blockingFactor, file.getPathname()); // TODO : should this be path name or file name? 		
		}
		
		archiveformatJob.setNoOfBlocksToBeRead(noOfBlocksToBeRead);
		archiveformatJob.setSkipByteCount(skipByteCount);
*/		
//		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
//		int archiveBlocksize = archiveformatJob.getArchiveBlocksize();
//		String deviceName = archiveformatJob.getDeviceName();
//		
//		String destinationPath = storageJob.getDestinationPath();
//		Integer noOfBlocksToBeRead = null;
//		Integer skipByteCount = null;
//		String filePathNameToBeRestored = storageJob.getFilePathname();
//		try {
//			ArchiveResponse archiveResponse = archiveFormatter.restore(destinationPath, volumeBlocksize, archiveBlocksize, deviceName, noOfBlocksToBeRead, skipByteCount, filePathNameToBeRestored);
    	
    	
    	
		try {
			 ArchiveResponse archiveResponse = archiveFormatter.restore(archiveformatJob);

			 storageResponse.setArchiveResponse(archiveResponse);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return storageResponse;
	}
	
	private ArchiveformatJob instantiateArchiveJobWithCommonFields(StoragetypeJob storagetypeJob) {
		ArchiveformatJob archiveformatJob = new ArchiveformatJob();
		archiveformatJob.setStoragetypeJob(storagetypeJob);
		
		StorageJob storageJob = storagetypeJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		VolumeDetails volumeDetails = volume.getDetails();
		int volumeBlocksize = volumeDetails.getBlocksize();
		int archiveformatBlocksize = volume.getArchiveformat().getBlocksize();
		String deviceName = null;
		
		if(storagetypeJob instanceof TapeJob) {
			TapeJob tj = (TapeJob) storagetypeJob;
			deviceName = tj.getTapedriveUid();
		} else if(storagetypeJob instanceof DiskJob) {
//			DiskJob dj = (DiskJob) storagetypeJob;
			deviceName = storageJob.getVolume().getDetails().getMountpoint();
		}
		archiveformatJob.setVolumeBlocksize(volumeBlocksize);
		archiveformatJob.setArchiveformatBlocksize(archiveformatBlocksize);
		archiveformatJob.setDeviceName(deviceName);
		return archiveformatJob;	

	}
}
