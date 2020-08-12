package org.ishafoundation.dwaraapi.storage.storagetask;


import java.time.LocalDateTime;
import java.util.Map;

import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagesubtype.AbstractStoragesubtype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("format")
//@Profile({ "!dev & !stage" })
public class Format extends AbstractStoragetaskAction{

    private static final Logger logger = LoggerFactory.getLogger(Format.class);
	
	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private Map<String, AbstractStoragesubtype> storagesubtypeMap;
	
	@Override
	public StorageJob buildStorageJob(Job job) {
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		
		Request request = job.getRequest();
		RequestDetails requestDetails = request.getDetails();
		
		Volume volume = getVolume(requestDetails);
		storageJob.setVolume(volume);

		storageJob.setForce(requestDetails.isForce());
		return storageJob;	
	}
	
	private Volume getVolume(RequestDetails requestDetails) {
		Volume volume = new Volume();
		
		String volumeBarcode = requestDetails.getVolume_id();
		String volumeGroupId = requestDetails.getVolume_group_id();
		Volume volumeGroup = volumeDao.findById(volumeGroupId);
		
		
		volume.setId(volumeBarcode);
		volume.setVolumetype(Volumetype.physical);
		
		volume.setVolumeRef(volumeGroup);

		String checksumalgorithm = configuration.getChecksumType();

		volume.setChecksumtype(Checksumtype.valueOf(checksumalgorithm));//Checksumtype.getChecksumtype(checksumalgorithm));
		volume.setFinalized(false);
		volume.setImported(false);

		String storagesubtype = requestDetails.getStoragesubtype();
		volume.setStoragesubtype(storagesubtype);
		
		AbstractStoragesubtype storagesubtypeImpl = storagesubtypeMap.get(storagesubtype);//storagesubtypeMap.get(storagesubtype.name());
		volume.setCapacity(storagesubtypeImpl.getCapacity());
		Integer generation = storagesubtypeImpl.getGeneration();
		

		// Inherited from group
		volume.setStoragetype(volumeGroup.getStoragetype());
		volume.setStoragelevel(volumeGroup.getStoragelevel());
		volume.setLocation(volumeGroup.getLocation());
		volume.setArchiveformat(volumeGroup.getArchiveformat());
		volume.setFormattedAt(LocalDateTime.now());
		
		Integer blocksize = requestDetails.getVolume_blocksize();
		
		VolumeDetails volumeDetails = new VolumeDetails();
		volumeDetails.setBarcoded(true);
		volumeDetails.setBlocksize(blocksize);
		volumeDetails.setGeneration(generation);

//		String mountpoint = requestDetails.getMountpoint();
//		volumeDetails.setMountpoint(mountpoint);
//		
//		Integer providerId = requestDetails.getProvider_id();
//		volumeDetails.setProvider_id(provider_id);
		volume.setDetails(volumeDetails);
		return volume;
	}
}
