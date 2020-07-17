package org.ishafoundation.dwaraapi.storage.storagetask;


import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("map_tapedrives")
//@Profile({ "!dev & !stage" })
public class MapTapedrives extends AbstractStoragetaskAction{

    private static final Logger logger = LoggerFactory.getLogger(MapTapedrives.class);

    @Override
    public StorageJob buildStorageJob(Job job) {
    	StorageJob storageJob = super.buildStorageJob(job);
    	
    	// Setting dummy volume here so that the storagetype is passed on to the delegator
    	Volume volume = new Volume();
    	volume.setStoragetype(Storagetype.tape);
    	
    	storageJob.setVolume(volume);
    	return storageJob;
    }
}
