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
