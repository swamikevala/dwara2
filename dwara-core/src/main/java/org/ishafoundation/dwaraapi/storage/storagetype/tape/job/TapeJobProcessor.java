package org.ishafoundation.dwaraapi.storage.storagetype.tape.job;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepositoryUtil;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.LabelManager;
import org.ishafoundation.dwaraapi.storage.storagetype.AbstractStoragetypeJobProcessor;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeDriveMapper;
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
	private JobDao jobDao; 
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@Autowired
	private TapeDriveManager tapeDriveManager;

	@Autowired
	private TapeDriveMapper tapeDriveMapper;

	@Autowired
	private LabelManager labelManager;
	
	@Autowired
	private ArtifactVolumeRepositoryUtil artifactVolumeRepositoryUtil;
	
	@Autowired
	private Configuration configuration;
	
	public StorageResponse map_tapedrives(SelectedStorageJob selectedStorageJob) throws Exception {
		String tapelibraryId = selectedStorageJob.getStorageJob().getJob().getRequest().getDetails().getAutoloaderId();
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		tapeDriveMapper.mapDrives(tapelibraryId, tapeJob.getAllDriveDetails());
		return new StorageResponse();
	}
	
	@Override
	protected void beforeInitialize(SelectedStorageJob selectedStorageJob) throws Exception {
		super.beforeInitialize(selectedStorageJob);
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();

		loadTape(selectedStorageJob);

		String dataTransferElementName = tapeJob.getDeviceWwnId();

		if(!tapeJob.getStorageJob().isForce()) { // if its not a forced format check if tape is blank and proceed
			if(!tapeDriveManager.isTapeBlank(dataTransferElementName)) // if tape is not blank throw error and dont continue...
				throw new Exception("Tape not blank to be formatted. If you still want to format use the \"force\" option...");
		}
		
		// validate on sequence no of tape - upfront validation
		// validate on group archive format vs member archiveformat. they have to be same...
		
		
		logger.trace("Now positioning tape head for formatting");
		tapeDriveManager.setTapeHeadPositionForInitializing(dataTransferElementName);
		logger.info("Tape Head positioned for formatting" + tapeLibraryName + ":" + dataTransferElementName + "(" + driveElementAddress + ")");
		
	}	
	
	@Override
	protected void beforeWrite(SelectedStorageJob selectedStorageJob) throws Exception {
		super.beforeWrite(selectedStorageJob);
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		
		loadTapeAndCheck(selectedStorageJob);
		
		Volume tapeToBeUsed = tapeJob.getStorageJob().getVolume();
		int lastArtifactOnVolumeEndVolumeBlock = artifactVolumeRepositoryUtil.getLastArtifactOnVolumeEndVolumeBlock(tapeJob.getStorageJob().getDomain(), tapeToBeUsed);
		
		logger.trace("Now positioning tape head for writing");
		tapeDriveManager.setTapeHeadPositionForWriting(tapeJob.getDeviceWwnId(), lastArtifactOnVolumeEndVolumeBlock + 3); 
		logger.info("Tape Head positioned for writing " + tapeLibraryName + ":" + tapeJob.getDeviceWwnId() + "(" + driveElementAddress + ")");
		
	}

	
	@Override
	protected void beforeVerify(SelectedStorageJob selectedStorageJob) throws Exception {
		super.beforeVerify(selectedStorageJob);
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		int blockNumberToSeek = tapeJob.getArtifactStartVolumeBlock();
		
		loadTapeAndCheck(selectedStorageJob);

		tapeDriveManager.setTapeHeadPositionForReading(tapeJob.getDeviceWwnId(), blockNumberToSeek);
		logger.info("Tape Head positioned for verifying "+ tapeLibraryName + ":" + tapeJob.getDeviceWwnId()+"("+driveElementAddress+")"  + ":" + blockNumberToSeek);
	}

	@Override
	protected void beforeRestore(SelectedStorageJob selectedStorageJob) throws Exception {
		super.beforeRestore(selectedStorageJob);
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		int blockNumberToSeek = tapeJob.getStorageJob().getVolumeBlock();
		
		loadTape(selectedStorageJob);
		
		tapeDriveManager.setTapeHeadPositionForReading(tapeJob.getDeviceWwnId(), blockNumberToSeek);
		logger.info("Tape Head positioned for reading "+ tapeLibraryName + ":" + tapeJob.getDeviceWwnId()+"("+driveElementAddress+")"  + ":" + blockNumberToSeek);
	}

	
	@Override
	protected void beforeFinalize(SelectedStorageJob selectedStorageJob) throws Exception {
		super.beforeFinalize(selectedStorageJob);
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();

		loadTapeAndCheck(selectedStorageJob);

		Volume tapeToBeUsed = tapeJob.getStorageJob().getVolume();
		int lastArtifactOnVolumeEndVolumeBlock = artifactVolumeRepositoryUtil.getLastArtifactOnVolumeEndVolumeBlock(tapeJob.getStorageJob().getDomain(), tapeToBeUsed);

		logger.trace("Now positioning tape head for finalizing " + tapeLibraryName + ":" + driveElementAddress);
		tapeDriveManager.setTapeHeadPositionForFinalizing(tapeJob.getDeviceWwnId(), lastArtifactOnVolumeEndVolumeBlock + 3);

	}
	
	private void loadTapeAndCheck(SelectedStorageJob selectedStorageJob) throws Exception {
		loadTape(selectedStorageJob);
		isRightTape(selectedStorageJob);
	}
	
	private boolean loadTape(SelectedStorageJob selectedStorageJob) throws Exception {
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
	
		Volume tapeToBeUsed = tapeJob.getStorageJob().getVolume();
		String tapeBarcode = tapeToBeUsed.getId();

		try {
			logger.info("Now locating and loading tape " + tapeBarcode + " on to drive " + driveElementAddress);
			tapeLibraryManager.locateAndLoadTapeOnToDrive(tapeBarcode, tapeLibraryName, driveElementAddress, tapeJob.getDeviceWwnId());
		} catch (Exception e) {
			logger.error("Unable to locate and load tape " + tapeBarcode + " on to drive " + driveElementAddress, e);
			String driveEmpty = "Data Transfer Element " + driveElementAddress + " is Empty"; // Happens during unload // Data Transfer Element 0 is Empty
			String driveFull = "Drive " + driveElementAddress + " Full"; // Happens during load // Drive 0 Full 
			
			if(e.getMessage().contains(driveEmpty) || e.getMessage().contains(driveFull)) { 
				createCorrectionJobs(selectedStorageJob);
			}
			throw e;
		}
		return true;
	}
	
	private void isRightTape(SelectedStorageJob selectedStorageJob) throws Exception {
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		Volume tapeToBeUsed = tapeJob.getStorageJob().getVolume();
		String tapeBarcode = tapeToBeUsed.getId();
		
		int lastArtifactOnVolumeEndVolumeBlock = artifactVolumeRepositoryUtil.getLastArtifactOnVolumeEndVolumeBlock(tapeJob.getStorageJob().getDomain(), tapeToBeUsed);
		
		if(lastArtifactOnVolumeEndVolumeBlock == 0)
			isRightTape(selectedStorageJob, tapeBarcode);
		else {
			tapeDriveManager.setTapeHeadPositionForReadingInterArtifactXml(tapeJob.getDeviceWwnId());
			
			boolean isRightTape = labelManager.isRightVolume(selectedStorageJob, false);
			if(!isRightTape)
				throw new Exception("Not the right tape loaded " + tapeBarcode + " Something fundamentally wrong. Please contact admin.");

		}
	}
	
	private void isRightTape(SelectedStorageJob selectedStorageJob, String tapeBarcode) throws Exception {
		// Verifying (if need be) if the tape is the right tape indeed.
		// last job on tape
		Job lastJobOnTape = jobDao.findTopByVolumeIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(tapeBarcode);
		
		LocalDateTime lastJobCompletionTime = LocalDateTime.now();
		boolean checkRightVolume = false;
		if(lastJobOnTape != null) {
			lastJobCompletionTime = lastJobOnTape.getCompletedAt();
			
			long intervalBetweenJobsInSeconds = ChronoUnit.SECONDS.between(lastJobCompletionTime, LocalDateTime.now());
			if(intervalBetweenJobsInSeconds > configuration.getRightVolumeCheckInterval()) {
				logger.trace("Past volume check threshold. Checking label...");
				checkRightVolume = true;
			}
		}else
			checkRightVolume = false;
			
		
		if(checkRightVolume) {
			TapeJob tapeJob = (TapeJob) selectedStorageJob;

			String tapeLibraryName = tapeJob.getTapeLibraryName();
			int driveElementAddress = tapeJob.getTapedriveNo();

			tapeDriveManager.setTapeHeadPositionForReadingLabel(tapeJob.getDeviceWwnId());
			logger.trace("Tape Head positioned for reading label "+ tapeLibraryName + ":" + tapeJob.getDeviceWwnId()+"("+driveElementAddress+")");
			
			boolean isRightTape = labelManager.isRightVolume(selectedStorageJob, true);
			if(!isRightTape)
				throw new Exception("Not the right tape loaded " + tapeBarcode + " Something fundamentally wrong. Please contact admin.");
		}else {
			logger.trace("Well inside volume check threshold. Not checking label");
		}
	}
	
	private void createCorrectionJobs(SelectedStorageJob selectedStorageJob) {
//		String errorMsg = null;
//		if(errorMsg.contains("No medium found")) { // 
//
//			
//			// initiate map drives job but check if its past the threshold
//			
//			// create a similar job as the current, so it gets picked up after the map drives job 
//		}
	}
}
