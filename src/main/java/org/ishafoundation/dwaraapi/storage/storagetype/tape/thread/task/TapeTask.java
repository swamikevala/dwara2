package org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.task;

import org.ishafoundation.dwaraapi.db.model.master.storage.Tape;
import org.ishafoundation.dwaraapi.storage.constants.StorageOperation;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeJobProcessor;
import org.ishafoundation.dwaraapi.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.tape.library.TapeLibraryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class TapeTask implements Runnable{
	
	private static final Logger logger = LoggerFactory.getLogger(TapeTask.class);
	
	@Autowired
	private TapeJobProcessor tapeJobProcessor;	
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@Autowired
	private TapeDriveManager tapeDriveManager;
	
	private StorageJob storageJob;


	public StorageJob getStorageJob() {
		return storageJob;
	}

	public void setStorageJob(StorageJob storageJob) {
		this.storageJob = storageJob;
	}

	@Override
	public void run() {
		int driveSNo = storageJob.getDriveNo();
		logger.trace("Now running storagejob " + storageJob.getJob().getJobId() + " on drive " + driveSNo);
		Tape tapeToBeUsed = storageJob.getVolume().getTape();
		logger.trace("Checking if drive " + driveSNo + " is already loaded with any tape");
		if(storageJob.isDriveAlreadyLoadedWithTape()) {
			logger.trace("Tape " + tapeToBeUsed.getBarcode() + " is already loaded on to drive " + driveSNo);
		}
		else {
			try {
				logger.trace("Now loading tape " + tapeToBeUsed + " on to drive " + driveSNo);
				tapeLibraryManager.loadTapeOnToDrive(tapeToBeUsed, driveSNo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		int storageOperationId = storageJob.getStorageOperation().getStorageOperationId();
		try {
			if(StorageOperation.WRITE.getStorageOperationId() == storageOperationId) {
				
				tapeDriveManager.setTapeHeadPositionForWriting(driveSNo); // FIXME - check on this
				logger.trace("TDM Tape Head positioned for writing");
				
				tapeJobProcessor.write(storageJob);
			}
			else if(StorageOperation.READ.getStorageOperationId() == storageOperationId) {
				tapeDriveManager.setTapeHeadPositionForReading(driveSNo, storageJob.getBlock());// FIXME - Need to offset???...
				logger.trace("TDM Tape Head positioned for reading");
				tapeJobProcessor.read(storageJob);
			}
		} catch (Throwable e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("Unable to copy" + storageJob.toString());
		}
	}	
}
