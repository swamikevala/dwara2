package org.ishafoundation.dwaraapi.storage.storagetask;


import org.ishafoundation.dwaraapi.db.dao.master.ArchiveformatDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
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
	private ArchiveformatDao archiveformatDao;
	
	@Override
	public StorageJob buildStorageJob(Job job) {
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		
		Request request = job.getRequest();

		Volume volume = getVolume(request.getDetails());
		storageJob.setVolume(volume);

		return storageJob;	
	}
	
	private Volume getVolume(RequestDetails requestDetails) {
		Volume volume = new Volume();
		
		String volumeBarcode = requestDetails.getVolume_uid();
		String volumeGroupUid = requestDetails.getVolume_group_uid();
		
		
		String archiveformat = requestDetails.getArchiveformat();
		String checksumalgorithm = requestDetails.getChecksum_algorithm();
		String encryptionalgorithm = requestDetails.getEncryption_algorithm();
		
		Volume latestVolumeEntry = volumeDao.findTopByOrderByIdDesc();
		int id = latestVolumeEntry.getId() + 1;
		volume.setId(id);
		volume.setUid(volumeBarcode);
		volume.setVolumetype(Volumetype.physical);
		
		Volume volumeRef = volumeDao.findByUid(volumeGroupUid);
		volume.setVolumeRef(volumeRef);
		
		volume.setChecksumtype(Checksumtype.getChecksumtype(checksumalgorithm));
		volume.setFinalized(false);
		volume.setImported(false);
	 	volume.setArchiveformat(archiveformatDao.findById(archiveformat).get());
		volume.setCapacity(requestDetails.getCapacity());


		// Inherited from group
		volume.setStoragetype(volumeRef.getStoragetype());
		volume.setStoragelevel(volumeRef.getStoragelevel());
		volume.setLocation(volumeRef.getLocation());

		Integer blocksize = requestDetails.getVolume_blocksize();
		Integer generation = requestDetails.getGeneration();
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
