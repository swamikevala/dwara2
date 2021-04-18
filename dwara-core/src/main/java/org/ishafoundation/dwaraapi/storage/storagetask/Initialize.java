package org.ishafoundation.dwaraapi.storage.storagetask;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
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
		
		String volumeBarcode = requestDetails.getVolumeId();

		String volumeGroupId = requestDetails.getVolumeGroupId();
		if(volumeGroupId == null) {
			volumeGroupId = StringUtils.substring(volumeBarcode, 0, 2);
		}
			
		volume.setId(volumeBarcode);
		volume.setUuid(UUID.randomUUID().toString());
		volume.setType(Volumetype.physical);

		Volume volumeGroup = volumeDao.findById(volumeGroupId).get();
		volume.setGroupRef(volumeGroup);

		String checksumalgorithm = configuration.getChecksumType();

		volume.setChecksumtype(Checksumtype.valueOf(checksumalgorithm));//Checksumtype.getChecksumtype(checksumalgorithm));
		volume.setFinalized(false);
		volume.setImported(false);

		String storagesubtype = requestDetails.getStoragesubtype();
		if(storagesubtype == null) {
			String storagesubtypeSuffix = StringUtils.substring(volumeBarcode, volumeBarcode.length()-2, volumeBarcode.length());
			Set<String> storagesubtypeSet = storagesubtypeMap.keySet();
			for (String nthStoragesubtypeImpl : storagesubtypeSet) {
				if(storagesubtypeSuffix.equals(storagesubtypeMap.get(nthStoragesubtypeImpl).getSuffixToEndWith())) {
					storagesubtype = nthStoragesubtypeImpl;
					break;
				}
			}
		}
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
		
		Integer blocksize = volumeGroup.getDetails().getBlocksize();//CR on release-Day 23rd Sept - requestDetails.getVolumeBlocksize();
		
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
