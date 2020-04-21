package org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.task;

import org.ishafoundation.dwaraapi.db.dao.master.TapelibraryDao;
import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.master.Tapelibrary;
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
	
	@Autowired
	private TapelibraryDao tapelibraryDao;
	
	private StorageJob storageJob;


	public StorageJob getStorageJob() {
		return storageJob;
	}

	public void setStorageJob(StorageJob storageJob) {
		this.storageJob = storageJob;
	}

	private int getTapeLibraryId(String tapeLibraryName) {
		Tapelibrary tapelibrary = tapelibraryDao.findByName(tapeLibraryName);
		return tapelibrary.getId();
	}
	
	@Override
	public void run() {
		String tapeLibraryName = storageJob.getTapeLibraryName();
		int tapeLibraryId = getTapeLibraryId(tapeLibraryName);
		int driveElementAddress = storageJob.getDriveNo();
		logger.trace("Now running storagejob " + storageJob.getJob().getId() + " on drive " + driveElementAddress);
		Tape tapeToBeUsed = storageJob.getVolume().getTape();
		logger.trace("Checking if drive " + driveElementAddress + " is already loaded with any tape");
		if(storageJob.isDriveAlreadyLoadedWithTape()) {
			logger.trace("Tape " + tapeToBeUsed.getBarcode() + " is already loaded on to drive " + driveElementAddress);
		}
		else {
			try {
				logger.trace("Now loading tape " + tapeToBeUsed + " on to drive " + driveElementAddress);
				tapeLibraryManager.locateAndLoadTapeOnToDrive(tapeToBeUsed.getBarcode(), tapeLibraryName, driveElementAddress);
			} catch (Exception e) {
				logger.error("Unable to load tape " + tapeToBeUsed + " on to drive " + driveElementAddress, e);
				return;
			}
		}
		
		int storageOperationId = storageJob.getStorageOperation().getStorageOperationId();
		try {
			if(StorageOperation.WRITE.getStorageOperationId() == storageOperationId) {
				logger.trace("Now positioning tape head for writing");
				tapeDriveManager.setTapeHeadPositionForWriting(tapeLibraryId, driveElementAddress); // FIXME - check on this, using eod, bsf 1 and fsf 1
				logger.trace("Tape Head positioned for writing");
				tapeJobProcessor.write(storageJob);
			}
			else if(StorageOperation.READ.getStorageOperationId() == storageOperationId) {
				logger.trace("Now positioning tape head for reading");
				tapeDriveManager.setTapeHeadPositionForReading(tapeLibraryId, driveElementAddress, storageJob.getBlock());// FIXME - Need offset???...
				logger.trace("Tape Head positioned for reading");
				tapeJobProcessor.read(storageJob);
			}
		} catch (Throwable e) {
			logger.error("Unable to complete tape task " + storageJob.toString(), e);
		}
	}	
}
