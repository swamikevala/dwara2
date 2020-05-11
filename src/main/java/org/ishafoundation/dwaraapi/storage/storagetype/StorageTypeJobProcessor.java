package org.ishafoundation.dwaraapi.storage.storagetype;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.storage.StorageFormatFactory;
import org.ishafoundation.dwaraapi.storage.constants.StorageOperation;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storageformat.AbstractStorageFormatArchiver;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchiveResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class StorageTypeJobProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(StorageTypeJobProcessor.class);
	
	@Autowired
	private ApplicationContext applicationContext;	
	
	public boolean format(StorageJob storageJob) throws Throwable{
		boolean isSuccess = false;
		ArchiveResponse archiveResponse = service(storageJob);
		if(archiveResponse != null)
			isSuccess = true;
		return isSuccess;
    }
	
    public ArchiveResponse write(StorageJob storageJob) throws Throwable{
    	return service(storageJob); 
    }
    
    public ArchiveResponse read(StorageJob storageJob) throws Throwable{
		return service(storageJob);    	
    }
    
    
    
    public ArchiveResponse service(StorageJob storageJob) throws Throwable{
    	// based on format
		String storageformat = storageJob.getStorageformat().getName();
		AbstractStorageFormatArchiver storageFormatter = StorageFormatFactory.getInstance(applicationContext, storageformat);
		ArchiveResponse archiveResponse = null;
		try {
			if(storageJob.getStorageOperation() == StorageOperation.WRITE)
				archiveResponse = storageFormatter.write(storageJob); 
			else if(storageJob.getStorageOperation() == StorageOperation.READ)
				archiveResponse = storageFormatter.read(storageJob);
			else if(storageJob.getStorageOperation() == StorageOperation.FORMAT) {
				boolean isSuccess = storageFormatter.format(storageJob);
				if(isSuccess)
					archiveResponse = new ArchiveResponse();
				else
					archiveResponse = null;
			}
		}catch (Exception e) {
			logger.error("Storage type implementation responded with error", e);
			throw e;
		}
		return archiveResponse;
    }
    
    public boolean cancel(Job job){
		return false;
    	
    }


}
