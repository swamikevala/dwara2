package org.ishafoundation.dwaraapi.storage.storagetask;


import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
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
	
	@Override
	public StorageJob buildStorageJob(Job job) {
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		
		Request request = job.getRequest();
		String volumeBarcode = request.getDetails().getVolume_uid();
		String volumeGroup = request.getDetails().getVolume_group_uid();
		Volume volume = getVolume(volumeBarcode, volumeGroup);
		storageJob.setVolume(volume);

		return storageJob;	
	}
	
	private Volume getVolume(String volumeBarcode, String volumeGroupUid) {
		Volume volume = new Volume();
		
		Volume latestVolumeEntry = volumeDao.findTopByOrderByIdDesc();
		int id = latestVolumeEntry.getId() + 1;
		volume.setId(id);
		volume.setUid(volumeBarcode);
		volume.setVolumetype(Volumetype.physical);
		Volume volumeRef = volumeDao.findByUid(volumeGroupUid);
		volume.setVolumeRef(volumeRef);

		// TODO : where to get these details from?
//		volume.setStoragetype(storagetype);
//		volume.setStoragelevel(storagelevel);
//		volume.setChecksumtype(checksumtype);
//		volume.setFinalized(finalized);
//		volume.setImported(imported);
//	 	volume.setArchiveformat(archiveformat);
//		volume.setCapacity(capacity);
//		volume.setLocation(location);
//		volume.setDetails(details);

		return volume;
	}
}
