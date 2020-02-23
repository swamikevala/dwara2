package org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.task;

import org.ishafoundation.dwaraapi.storage.constants.StorageOperation;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeJobProcessor;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeJobsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeTask implements Runnable{
	
	private static final Logger logger = LoggerFactory.getLogger(TapeTask.class);
	
	@Autowired
	private TapeJobProcessor tapeJobProcessor;	
	
	private StorageJob archiveJob;


	public StorageJob getArchiveJob() {
		return archiveJob;
	}

	public void setArchiveJob(StorageJob archiveJob) {
		this.archiveJob = archiveJob;
	}

	@Override
	public void run() {
		
		// TapeLibraryManager.load(tapeLibraryName, seSNo, driveSNo)
		logger.trace("TLM loaded volume on to drive");
		
		int archiveOperationId = archiveJob.getStorageOperation().getStorageOperationId();
		try {
			if(StorageOperation.WRITE.getStorageOperationId() == archiveOperationId) {
				
				// TapeDriveManager.setTapeHeadPositionForWriting()
				logger.trace("TDM Tape Head positioned for writing");
				
				tapeJobProcessor.write(archiveJob);
			}
			else if(StorageOperation.READ.getStorageOperationId() == archiveOperationId) {
				// TapeDriveManager.setTapeHeadPositionForReading()
				tapeJobProcessor.read(archiveJob);
			}
		} catch (Throwable e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("Unable to copy" + archiveJob.toString());
		}
	}	
}
