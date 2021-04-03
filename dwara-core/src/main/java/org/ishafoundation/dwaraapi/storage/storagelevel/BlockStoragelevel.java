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
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.VolumeindexManager;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.LabelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("block"+DwaraConstants.STORAGELEVEL_SUFFIX)
//@Profile({ "!dev & !stage" })
public class BlockStoragelevel implements IStoragelevel {

	private static final Logger logger = LoggerFactory.getLogger(BlockStoragelevel.class);
	
	@Autowired
	private Map<String, IArchiveformatter> iArchiveformatterMap;

	@Autowired
	private LabelManager labelManager;

	@Autowired
	private VolumeindexManager volumeindexManager;
	
	@Value("${filesystem.temporarylocation}")
	private String filesystemTemporarylocation;

	@Override
	public StorageResponse copy(SelectedStorageJob selectedStorageJob) throws Exception{
		throw new Exception("Copy not supported");
	}
		
	@Override
	public StorageResponse initialize(SelectedStorageJob selectedStorageJob) throws Exception{
		
		boolean status = labelManager.writeVolumeLabel(selectedStorageJob);
		logger.debug("Labelling success? - " + status);
		
		return new StorageResponse();
	}

	@Override
	public StorageResponse write(SelectedStorageJob selectedStorageJob) throws Exception{
		StorageResponse storageResponse = new StorageResponse();
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		logger.debug("Writing blocks");
		Archiveformat archiveformat = storageJob.getVolume().getArchiveformat();
    	IArchiveformatter archiveFormatter = iArchiveformatterMap.get(archiveformat.getId() + DwaraConstants.ARCHIVER_SUFFIX);
    	
    	ArchiveformatJob archiveformatJob = instantiateArchiveJobWithCommonFields(selectedStorageJob);
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
		
    	ArchiveResponse archiveResponse = archiveFormatter.write(archiveformatJob);
    	storageResponse.setArchiveResponse(archiveResponse);
		return storageResponse;
	}

	@Override
	public StorageResponse verify(SelectedStorageJob selectedStorageJob) throws Exception{
		logger.debug("Verifying blocks");
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		Archiveformat archiveformat = volume.getArchiveformat();
    	IArchiveformatter archiveFormatter = iArchiveformatterMap.get(archiveformat.getId() + DwaraConstants.ARCHIVER_SUFFIX);
		StorageResponse storageResponse = new StorageResponse();
    	ArchiveformatJob archiveformatJob = instantiateArchiveJobWithCommonFields(selectedStorageJob);
    	archiveformatJob.setTargetLocationPath(storageJob.getTargetLocationPath());
	
		ArchiveResponse archiveResponse = archiveFormatter.verify(archiveformatJob);
		storageResponse.setArchiveResponse(archiveResponse);

		return storageResponse;
	}

	@Override
	public StorageResponse finalize(SelectedStorageJob selectedStorageJob) throws Exception{
		boolean status = volumeindexManager.writeVolumeindex(selectedStorageJob);
		logger.debug("Indexing success? - " + status);
				
		return new StorageResponse();
	}
	
	@Override
	public StorageResponse restore(SelectedStorageJob selectedStorageJob) throws Exception {
		logger.debug("Reading blocks");
		StorageResponse storageResponse = new StorageResponse();
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		Archiveformat archiveformat = volume.getArchiveformat();
    	IArchiveformatter archiveFormatter = iArchiveformatterMap.get(archiveformat.getId() + DwaraConstants.ARCHIVER_SUFFIX);
    	
    	ArchiveformatJob archiveformatJob = instantiateArchiveJobWithCommonFields(selectedStorageJob);
    	archiveformatJob.setTargetLocationPath(storageJob.getTargetLocationPath());
		ArchiveResponse archiveResponse = archiveFormatter.restore(archiveformatJob);
		storageResponse.setArchiveResponse(archiveResponse);
		return storageResponse;
	}
	
	private ArchiveformatJob instantiateArchiveJobWithCommonFields(SelectedStorageJob selectedStorageJob) {
		ArchiveformatJob archiveformatJob = new ArchiveformatJob();
		archiveformatJob.setSelectedStorageJob(selectedStorageJob);
		
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		VolumeDetails volumeDetails = volume.getDetails();
		int volumeBlocksize = volumeDetails.getBlocksize();
		int archiveformatBlocksize = volume.getArchiveformat().getBlocksize();
		String deviceName = selectedStorageJob.getDeviceWwnId();
		
		archiveformatJob.setVolumeBlocksize(volumeBlocksize);
		archiveformatJob.setArchiveformatBlocksize(archiveformatBlocksize);
		archiveformatJob.setDeviceName(deviceName);
		return archiveformatJob;	

	}
}
