package org.ishafoundation.dwaraapi.storage.storagetask;


import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("verify")
//@Profile({ "!dev & !stage" })
public class Verify extends AbstractStoragetaskAction{

    private static final Logger logger = LoggerFactory.getLogger(Verify.class);

	
	@Autowired
	private VolumeDao volumeDao; 
	
	@Override
	public StorageJob buildStorageJob(Job job){
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		
		Job writeJobToBeVerified = job.getJobRef();
		Integer volumeIdWriteJobUsed = writeJobToBeVerified.getDetails().getVolume_id();
		Volume volume = volumeDao.findById(volumeIdWriteJobUsed).get();
		storageJob.setVolume(volume);
		
		/** lazy loading other details once the job is selected for processing - Refer AbstractStoragetypeJobProcessor.verify()**/
		
		return storageJob;
	}
}
