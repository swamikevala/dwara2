package org.ishafoundation.dwaraapi.storage.storagetype.thread;

import java.util.List;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.storage.model.DiskJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("disk"+DwaraConstants.StorageTypeJobManagerSuffix)
@Scope("prototype")
public class DiskJobManager extends AbstractStoragetypeJobManager {


    private static final Logger logger = LoggerFactory.getLogger(DiskJobManager.class);

	@Override
    public void run() {

		List<StorageJob> storageJobsList = getStorageJobList();
		StorageJob storageJob = storageJobsList.get(0); //selected Job
		logger.debug("Processing disk job " + storageJob.getJob().getId());		
		DiskJob dj = new DiskJob();
		dj.setStorageJob(storageJob);
		
		manage(dj);
		

	}
}