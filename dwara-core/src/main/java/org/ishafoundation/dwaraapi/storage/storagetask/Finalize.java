package org.ishafoundation.dwaraapi.storage.storagetask;


import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("finalize")
//@Profile({ "!dev & !stage" })
public class Finalize extends AbstractStoragetaskAction{

    private static final Logger logger = LoggerFactory.getLogger(Finalize.class);
    
	@Autowired
	private VolumeDao volumeDao;
	
    @Override
    public StorageJob buildStorageJob(Job job) throws Exception {
    	StorageJob storageJob = super.buildStorageJob(job);
    	
    	Request request = job.getRequest();
		String volumeUid = request.getDetails().getVolumeId();
    	Volume volume = volumeDao.findById(volumeUid).get();
    	
    	storageJob.setVolume(volume);
    	return storageJob;
    }

}
