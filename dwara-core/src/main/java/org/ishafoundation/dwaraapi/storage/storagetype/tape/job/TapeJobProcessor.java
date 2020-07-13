package org.ishafoundation.dwaraapi.storage.storagetype.tape.job;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.ishafoundation.dwaraapi.storage.storagetype.AbstractStoragetypeJobProcessor;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component("tapeJobProcessor")
@Component("tape" + DwaraConstants.STORAGETYPE_JOBPROCESSOR_SUFFIX)
//@Profile({ "!dev & !stage" })
public class TapeJobProcessor extends AbstractStoragetypeJobProcessor {

	private static final Logger logger = LoggerFactory.getLogger(TapeJobProcessor.class);
//
//    static {
//    	StoragetypeJobProcessorFactory.register(Storagetype.tape.name(), TapeJobProcessor.class);
//    }
    
	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@Autowired
	private TapeDriveManager tapeDriveManager;
	

	
	public StorageResponse map_tapedrives(StoragetypeJob storagetypeJob) {
		logger.trace("Mapping invoked from processor");
		return new StorageResponse();
	}
	@Override
	protected void beforeFormat(StoragetypeJob storagetypeJob) throws Exception {
		TapeJob tapeJob = (TapeJob) storagetypeJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();

		loadTape(storagetypeJob, true);

		logger.trace("Now positioning tape head for formatting " + tapeLibraryName + ":" + driveElementAddress);
	}	
	
	@Override
	protected void beforeWrite(StoragetypeJob storagetypeJob) throws Exception {
		TapeJob tapeJob = (TapeJob) storagetypeJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();

		loadTape(storagetypeJob);

		
		logger.trace("Now positioning tape head for writing" + tapeLibraryName + ":" + driveElementAddress);
		tapeDriveManager.setTapeHeadPositionForWriting(tapeJob.getDeviceUid()); // FIXME - check on this, using eod, bsf 1 and fsf 1
		logger.trace("Tape Head positioned for writing");
		
	}
	
	@Override
	protected void beforeVerify(StoragetypeJob storagetypeJob) throws Exception {
		super.beforeVerify(storagetypeJob);
		TapeJob tapeJob = (TapeJob) storagetypeJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		int blockNumberToSeek = tapeJob.getStorageJob().getArtifactStartVolumeBlock();
		
		loadTape(storagetypeJob);
		
//		tapeDriveManager.setTapeHeadPositionForReading(tapeLibraryName, driveElementAddress, blockNumberToSeek); // FIXME - check on this, using eod, bsf 1 and fsf 1
		logger.trace("Tape Head positioned for verifying"+ tapeLibraryName + ":" + driveElementAddress  + ":" + blockNumberToSeek);
	}

//	@Override
//	protected void afterWrite(StoragetypeJob storageJob, ArchiveResponse ar) {
//		// TODO Auto-generated method stub
//		logger.debug("after write hook on TapeJP");
//	}

	@Override
	protected void beforeRestore(StoragetypeJob storagetypeJob) throws Exception {
		TapeJob tapeJob = (TapeJob) storagetypeJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		int blockNumberToSeek = tapeJob.getStorageJob().getVolumeBlock();
		
		loadTape(storagetypeJob);
//		tapeDriveManager.setTapeHeadPositionForReading(tapeLibraryName, driveElementAddress, blockNumberToSeek); // FIXME - check on this, using eod, bsf 1 and fsf 1
		logger.trace("Tape Head positioned for reading"+ tapeLibraryName + ":" + driveElementAddress  + ":" + blockNumberToSeek);
	}

	private boolean loadTape(StoragetypeJob storagetypeJob) throws Exception {
		return loadTape(storagetypeJob, false);
	}
	
	private boolean loadTape(StoragetypeJob storagetypeJob, boolean skipRightTapeCheck) throws Exception {
		TapeJob tapeJob = (TapeJob) storagetypeJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
	
		Volume tapeToBeUsed = tapeJob.getStorageJob().getVolume();
		logger.trace("Checking if drive " + driveElementAddress + " is already loaded with the needed tape");
		
		if(tapeJob.isTapedriveAlreadyLoadedWithNeededTape()) {
			logger.trace("Tape " + tapeToBeUsed.getUid() + " is already loaded on to drive " + driveElementAddress);
		}
		else {
			try {
				logger.trace("Now loading tape " + tapeToBeUsed.getUid() + " on to drive " + driveElementAddress);
				tapeLibraryManager.locateAndLoadTapeOnToDrive(tapeToBeUsed.getUid(), tapeLibraryName, driveElementAddress, tapeJob.getDeviceUid());
			} catch (Exception e) {
				logger.error("Unable to load tape " + tapeToBeUsed.getUid() + " on to drive " + driveElementAddress, e);
				throw e;
			}
		}
		
		if(!skipRightTapeCheck) {
			logger.trace("Now checking if " + tapeLibraryName + ":" + driveElementAddress + " indeed have the tape " + tapeToBeUsed.getUid());
			//isRightVolume
		}
		return true;
	}

//	@Override
//	protected void afterRestore(StorageTypeJob storageJob) {
//		// TODO Auto-generated method stub
//		
//	}
//
	protected void beforeFinalize(StoragetypeJob storagetypeJob) throws Exception {
		TapeJob tapeJob = (TapeJob) storagetypeJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();

		loadTape(storagetypeJob, true);

		logger.trace("Now positioning tape head for finalizing " + tapeLibraryName + ":" + driveElementAddress);

	}
}
