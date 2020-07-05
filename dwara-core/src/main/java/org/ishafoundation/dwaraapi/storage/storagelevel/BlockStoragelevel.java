package org.ishafoundation.dwaraapi.storage.storagelevel;

import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Archiveformat;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
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

	
	@Override
	public StorageResponse format(StoragetypeJob job) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StorageResponse write(StoragetypeJob storagetypeJob) {
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
	public StorageResponse verify(StoragetypeJob storagetypeJob) {
		logger.debug("Verifying blocks");
		Archiveformat archiveformat = storagetypeJob.getStorageJob().getVolume().getArchiveformat();
    	IArchiveformatter archiveFormatter = iArchiveformatterMap.get(archiveformat.getId() + DwaraConstants.ARCHIVER_SUFFIX);
//    	ArchiveResponse archiveResponse = archiveFormatter.verify();
    	return null;
	}

	@Override
	public StorageResponse finalize(StoragetypeJob job) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public StorageResponse restore(StoragetypeJob storagetypeJob) {
		logger.debug("Reading blocks");
		StorageResponse storageResponse = new StorageResponse();
		StorageJob storageJob = storagetypeJob.getStorageJob();
		Archiveformat archiveformat = storageJob.getVolume().getArchiveformat();
    	IArchiveformatter archiveFormatter = iArchiveformatterMap.get(archiveformat.getId() + DwaraConstants.ARCHIVER_SUFFIX);
    	
    	ArchiveformatJob archiveformatJob = instantiateArchiveJobWithCommonFields(storagetypeJob);
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
    	archiveformatJob.setDestinationPath(storageJob.getDestinationPath());
    	archiveformatJob.setFilePathNameToBeRestored(storageJob.getFilePathname());
    	
		try {
			 ArchiveResponse archiveResponse = archiveFormatter.restore(archiveformatJob);

			 storageResponse.setArchiveResponse(archiveResponse);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		int archiveBlocksize = volume.getArchiveformat().getBlocksize();
		String deviceName = null;
		
		if(storagetypeJob instanceof TapeJob) {
			TapeJob tj = (TapeJob) storagetypeJob;
			deviceName = tj.getTapedriveUid();
		} else if(storagetypeJob instanceof DiskJob) {
//			DiskJob dj = (DiskJob) storagetypeJob;
			deviceName = storageJob.getVolume().getDetails().getMountpoint();
		}
		archiveformatJob.setVolumeBlocksize(volumeBlocksize);
		archiveformatJob.setArchiveBlocksize(archiveBlocksize);
		archiveformatJob.setDeviceName(deviceName);
		return archiveformatJob;	

	}
}
