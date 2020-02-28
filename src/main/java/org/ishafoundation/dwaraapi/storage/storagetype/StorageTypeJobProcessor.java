package org.ishafoundation.dwaraapi.storage.storagetype;

import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.model.Volume;
import org.ishafoundation.dwaraapi.storage.StorageFormatFactory;
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
	private JobDao jobDao;	
	
	@Autowired
	private ApplicationContext applicationContext;	
	
	public boolean format(Volume volume) {
		return false;
    }
	
    public ArchiveResponse write(StorageJob storageJob) throws Throwable{
    	// some common code goes in here... 
		Job job = (Job) storageJob.getJob();
		job.setStartedAt(System.currentTimeMillis());
		job.setStatusId(Status.IN_PROGRESS.getStatusId());
		logger.debug("DB Job Updation " + Status.IN_PROGRESS);
		jobDao.save(job);
		logger.debug("DB Job Updation - Success");
		
    	// based on format
		String storageformat = storageJob.getStorageformat().getName();
		AbstractStorageFormatArchiver storageFormatter = StorageFormatFactory.getInstance(applicationContext, storageformat);
		ArchiveResponse archiveResponse = storageFormatter.write(storageJob); // tapeJobProcessor.write(tapeJob); // go on a seperate thread... create a task and allocate it to the thread
    	
		job.setStatusId(Status.COMPLETED.getStatusId());
		job.setCompletedAt(System.currentTimeMillis());
		logger.debug("DB Job Updation " + Status.COMPLETED);
		jobDao.save(job);
		logger.debug("DB Job Updation - Success");
		return archiveResponse;
    }
    
    public ArchiveResponse read(StorageJob storageJob) throws Throwable{
    	// some common code goes in here... 
		Job job = (Job) storageJob.getJob();
		job.setStatusId(Status.IN_PROGRESS.getStatusId());
		jobDao.save(job);

    	// based on format
		String format = storageJob.getStorageformat().getName();
		AbstractStorageFormatArchiver storageFormatArchiver = StorageFormatFactory.getInstance(applicationContext, format);
		ArchiveResponse archiveResponse = storageFormatArchiver.read(storageJob); // tapeJobProcessor.write(tapeJob); // go on a seperate thread... create a task and allocate it to the thread
    	
		
		job.setStatusId(Status.COMPLETED.getStatusId());
		jobDao.save(job);
		// some common code here...
		return archiveResponse;    	
    }
    
    public boolean cancel(Job job){
		return false;
    	
    }


}
