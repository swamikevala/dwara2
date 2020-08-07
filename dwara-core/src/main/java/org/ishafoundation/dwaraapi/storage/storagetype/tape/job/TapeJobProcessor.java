package org.ishafoundation.dwaraapi.storage.storagetype.tape.job;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
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
    
	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@Autowired
	private TapeDriveManager tapeDriveManager;
	
	public StorageResponse map_tapedrives(SelectedStorageJob selectedStorageJob) {
		logger.trace("Mapping invoked from processor");
		return new StorageResponse();
	}
	
	@Override
	protected void beforeFormat(SelectedStorageJob selectedStorageJob) throws Exception {
		super.beforeFormat(selectedStorageJob);
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();

		loadTape(selectedStorageJob, true);

		// TODO : Wher shoudld is Blank check go? and force option...
		// validate on sequence no of tape - upfront validation
		// validate on group archive format vs member archiveformat. they have to be same...
		
		
		logger.trace("Now positioning tape head for formatting " + tapeLibraryName + ":" + tapeJob.getDeviceWwnId()+"("+driveElementAddress+")" );
		tapeDriveManager.setTapeHeadPositionForFormatting(tapeJob.getDeviceWwnId());
		logger.trace("Tape Head positioned for formatting");
		
	}	
	
	@Override
	protected void beforeWrite(SelectedStorageJob selectedStorageJob) throws Exception {
		super.beforeWrite(selectedStorageJob);
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		int fileNumberToBePositioned = tapeJob.getArtifactVolumeCount() == 0 ? 1 : tapeJob.getArtifactVolumeCount() + 1; // +1 because of label...
		loadTape(selectedStorageJob);
		
		logger.trace("Now positioning tape head for writing " + tapeLibraryName + ":" + tapeJob.getDeviceWwnId()+"("+driveElementAddress+")" );
		tapeDriveManager.setTapeHeadPositionForWriting(tapeJob.getDeviceWwnId(), fileNumberToBePositioned); // FIXME - check on this, using eod, bsf 1 and fsf 1
		logger.trace("Tape Head positioned for writing");
		
	}
	
	@Override
	protected void beforeVerify(SelectedStorageJob selectedStorageJob) throws Exception {
		super.beforeVerify(selectedStorageJob);
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		int blockNumberToSeek = tapeJob.getArtifactStartVolumeBlock();
		
		loadTape(selectedStorageJob);
		
		tapeDriveManager.setTapeHeadPositionForReading(tapeJob.getDeviceWwnId(), blockNumberToSeek);
		logger.trace("Tape Head positioned for verifying "+ tapeLibraryName + ":" + tapeJob.getDeviceWwnId()+"("+driveElementAddress+")"  + ":" + blockNumberToSeek);
	}

//	@Override
//	protected void afterWrite(StoragetypeJob storageJob, ArchiveResponse ar) {
//		// TODO Auto-generated method stub
//		logger.debug("after write hook on TapeJP");
//	}

	@Override
	protected void beforeRestore(SelectedStorageJob selectedStorageJob) throws Exception {
		super.beforeRestore(selectedStorageJob);
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		int blockNumberToSeek = tapeJob.getStorageJob().getVolumeBlock();
		
		loadTape(selectedStorageJob);
		
		tapeDriveManager.setTapeHeadPositionForReading(tapeJob.getDeviceWwnId(), blockNumberToSeek);
		logger.trace("Tape Head positioned for reading "+ tapeLibraryName + ":" + tapeJob.getDeviceWwnId()+"("+driveElementAddress+")"  + ":" + blockNumberToSeek);
	}

	private boolean loadTape(SelectedStorageJob selectedStorageJob) throws Exception {
		return loadTape(selectedStorageJob, false);
	}
	
	private boolean loadTape(SelectedStorageJob selectedStorageJob, boolean skipRightTapeCheck) throws Exception {
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
	
		Volume tapeToBeUsed = tapeJob.getStorageJob().getVolume();
//		logger.trace("Checking if drive " + driveElementAddress + " is already loaded with the needed tape");
//		
//		if(tapeJob.isTapedriveAlreadyLoadedWithNeededTape()) {
//			logger.trace("Tape " + tapeToBeUsed.getId() + " is already loaded on to drive " + driveElementAddress);
//		}
//		else {
			try {
				logger.trace("Now locating and loading tape " + tapeToBeUsed.getId() + " on to drive " + driveElementAddress);
				tapeLibraryManager.locateAndLoadTapeOnToDrive(tapeToBeUsed.getId(), tapeLibraryName, driveElementAddress, tapeJob.getDeviceWwnId());
			} catch (Exception e) {
				logger.error("Unable to locate and load tape " + tapeToBeUsed.getId() + " on to drive " + driveElementAddress, e);
				throw e;
			}
//		}
		
		if(!skipRightTapeCheck) {
			logger.trace("Now checking if " + tapeLibraryName + ":" + driveElementAddress + " indeed have the tape " + tapeToBeUsed.getId());
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
	@Override
	protected void beforeFinalize(SelectedStorageJob selectedStorageJob) throws Exception {
		super.beforeFinalize(selectedStorageJob);
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();

		loadTape(selectedStorageJob, true);

		logger.trace("Now positioning tape head for finalizing " + tapeLibraryName + ":" + driveElementAddress);

	}
}
