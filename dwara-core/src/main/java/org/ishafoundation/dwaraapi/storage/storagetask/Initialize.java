package org.ishafoundation.dwaraapi.storage.storagetask;


import java.time.LocalDateTime;
import java.util.Map;

import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagesubtype.AbstractStoragesubtype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("initialize")
//@Profile({ "!dev & !stage" })
public class Initialize extends AbstractStoragetaskAction{

    private static final Logger logger = LoggerFactory.getLogger(Initialize.class);
	
	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
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
		volume.setType(Volumetype.physical);
		
		volume.setGroupRef(volumeGroup);

		String checksumalgorithm = configuration.getChecksumType();

		volume.setChecksumtype(Checksumtype.valueOf(checksumalgorithm));//Checksumtype.getChecksumtype(checksumalgorithm));
		volume.setFinalized(false);
		volume.setImported(false);

		String storagesubtype = requestDetails.getStoragesubtype();
		volume.setStoragesubtype(storagesubtype);
		
		AbstractStoragesubtype storagesubtypeImpl = storagesubtypeMap.get(storagesubtype);//storagesubtypeMap.get(storagesubtype.name());
		volume.setCapacity(storagesubtypeImpl.getCapacity());
	
		// setting to default location
		// During initialisation the physical tape actually is in the default location where tape library is even though its expected to be in the 
		// There will be some other action that will reset this location and use the volumeGroup.getLocation();
		Location location = configurationTablesUtil.getDefaultLocation(); //volumeGroup.getLocation();
		volume.setLocation(location);

		// Inherited from group
		volume.setStoragetype(volumeGroup.getStoragetype());
		volume.setStoragelevel(volumeGroup.getStoragelevel());
		volume.setArchiveformat(volumeGroup.getArchiveformat());
		volume.setInitializedAt(LocalDateTime.now());
		
		Integer blocksize = requestDetails.getVolume_blocksize();
		
		VolumeDetails volumeDetails = new VolumeDetails();
		volumeDetails.setBarcoded(true);
		volumeDetails.setBlocksize(blocksize);
		

//		String mountpoint = requestDetails.getMountpoint();
//		volumeDetails.setMountpoint(mountpoint);
//		
//		Integer providerId = requestDetails.getProvider_id();
//		volumeDetails.setProvider_id(provider_id);
		volume.setDetails(volumeDetails);
		return volume;
	}
}
