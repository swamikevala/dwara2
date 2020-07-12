package org.ishafoundation.dwaraapi.storage.storagelevel;

import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Archiveformat;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.model.ArchiveformatJob;
import org.ishafoundation.dwaraapi.storage.model.DiskJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.LabelManager;
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
	private LabelManager labelManager;
	
	
	
	@Override
	public StorageResponse format(StoragetypeJob storagetypeJob) throws Exception{
		
		boolean status = labelManager.writeVolumeLabel(storagetypeJob);
		logger.debug("Labelling success ? - " + status);
		
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
		StorageJob storageJob = storagetypeJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		Archiveformat archiveformat = volume.getArchiveformat();
    	IArchiveformatter archiveFormatter = iArchiveformatterMap.get(archiveformat.getId() + DwaraConstants.ARCHIVER_SUFFIX);
		StorageResponse storageResponse = new StorageResponse();
    	ArchiveformatJob archiveformatJob = instantiateArchiveJobWithCommonFields(storagetypeJob);
    	archiveformatJob.setDestinationPath(storageJob.getDestinationPath());
 		try {		
			 ArchiveResponse archiveResponse = archiveFormatter.verify(archiveformatJob);
	
			 storageResponse.setArchiveResponse(archiveResponse);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return storageResponse;
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
		String deviceName = storagetypeJob.getDeviceUid();
		
		archiveformatJob.setVolumeBlocksize(volumeBlocksize);
		archiveformatJob.setArchiveformatBlocksize(archiveformatBlocksize);
		archiveformatJob.setDeviceName(deviceName);
		return archiveformatJob;	

	}
}
