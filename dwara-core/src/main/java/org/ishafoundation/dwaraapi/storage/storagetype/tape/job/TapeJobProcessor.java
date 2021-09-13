package org.ishafoundation.dwaraapi.storage.storagetype.tape.job;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.PfrConstants;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileEntityUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepositoryUtil;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.InterArtifactlabel;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.LabelManager;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.Volumelabel;
import org.ishafoundation.dwaraapi.storage.storagesubtype.AbstractStoragesubtype;
import org.ishafoundation.dwaraapi.storage.storagetype.AbstractStoragetypeJobProcessor;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeDriveMapper;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.status.MtxStatus;
import org.ishafoundation.dwaraapi.utils.VolumeUtil;
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
	private VolumeDao volumeDao; 
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@Autowired
	private TapeDriveManager tapeDriveManager;

	@Autowired
	private TapeDriveMapper tapeDriveMapper;

	@Autowired
	private LabelManager labelManager;
	
	@Autowired
    private FileEntityUtil fileEntityUtil;
	
	@Autowired
	private VolumeUtil volumeUtil;
	
	@Autowired
	private ArtifactVolumeRepositoryUtil artifactVolumeRepositoryUtil;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private Map<String, AbstractStoragesubtype> storagesubtypeMap;	
	
//	@Autowired
//	private AutoloaderService autoloaderService;
//	
//	@Autowired
//	private JobService jobService;
	
	public StorageResponse map_tapedrives(SelectedStorageJob selectedStorageJob) throws Exception {
		String tapelibraryId = selectedStorageJob.getStorageJob().getJob().getRequest().getDetails().getAutoloaderId();
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		tapeDriveMapper.mapDrives(tapelibraryId, tapeJob.getDriveDetails());
		return new StorageResponse();
	}
	
	@Override
	public StorageResponse initialize(SelectedStorageJob selectedStorageJob) throws Exception {
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		
		List<DriveDetails> preparedDriveDetailsList = tapeJob.getDriveDetails(); // gets prepared drive details...
		
		Volume volume = tapeJob.getStorageJob().getVolume();
//		
//		int volumeGeneration = storagesubtypeMap.get(volume.getStoragesubtype()).getGeneration();
//
//		DriveDetails selectedDriveDetails = null;
//		// select a drive that supports the volume
//		for (DriveDetails driveDetails : preparedDriveDetailsList) {
//			int[] driveSupportedWriteGenerations = storagesubtypeMap.get(driveDetails.getDriveStoragesubtype()).getWriteSupportedGenerations();
//			for (int i = 0; i < driveSupportedWriteGenerations.length; i++) {
//				if(driveSupportedWriteGenerations[i] == volumeGeneration) {
//					selectedDriveDetails = driveDetails;
//					break;
//				}
//			}
//			
//			if(selectedDriveDetails != null)
//				break;
//		}

		String tapeLibraryName = preparedDriveDetailsList.get(0).getTapelibraryName();// TODO - Supports only one library for now..
		MtxStatus mtxStatus = tapeLibraryManager.getMtxStatus(tapeLibraryName);
		int storageElementSNo = tapeLibraryManager.locateTape(volume.getId(), mtxStatus);
		List<DataTransferElement> dataTransferElementList = mtxStatus.getDteList();
		
		DataTransferElement usedDataTransferElement = null; 
		boolean status = false;
		for (DataTransferElement nthDataTransferElement : dataTransferElementList) {
			int dteSNo = nthDataTransferElement.getsNo();
			try {
				tapeLibraryManager.load(tapeLibraryName, storageElementSNo, dteSNo);
				usedDataTransferElement = nthDataTransferElement;
			}catch (Exception e) {
				logger.warn("Unable to load from slot " + storageElementSNo + " to drive " + dteSNo + ". Potentially an unsupported generation drive. Try with another one"); // if we try to load a LTO7 into LTO6 drive....
				continue;
			}
			
			DriveDetails usedDriveDetails = null;
			for (DriveDetails nthDriveDetails : preparedDriveDetailsList) {
				//String driveId = nthDriveDetails.getDriveId();
				String driveName = nthDriveDetails.getDriveName();
				logger.trace("Checking if " + driveName + " has got the tape loaded");
				DriveDetails driveStatusDetails = tapeDriveManager.getDriveDetails(driveName);
				if(driveStatusDetails.getMtStatus().isReady()){ // means drive is not empty and has the tape we loaded
					usedDriveDetails = nthDriveDetails;
					logger.trace("Tape loaded");
					break;
				}
			}
			usedDriveDetails.setDte(usedDataTransferElement);
			
			String dataTransferElementName = usedDriveDetails.getDriveName();

			if(!tapeJob.getStorageJob().isForce()) { // if its not a forced format check if tape is blank and proceed 
				logger.trace("Checking if tape is blank");
				if(!tapeDriveManager.isTapeBlank(dataTransferElementName)) // if tape is not blank throw error and dont continue...
					throw new Exception("Tape to be initialized " + volume.getId() + " is not blank. If you still want to initialize use the \"force\" option...");
			}
			
			logger.trace("Now positioning tape head for initialize");
			tapeDriveManager.setTapeHeadPositionForInitializing(dataTransferElementName);
			logger.info("Tape Head positioned for initialize " + tapeLibraryName + ":" + dataTransferElementName + "(" + usedDataTransferElement.getsNo() + ")");
			
			
			// TODO - Commenting the catch block as we dont know the specific error to check on write not supported gen drives and dont want to retry by keeping it open for other scenarios...
			// This usecase is probably not needed for now anyway as we use  LTO-7 drive and there isnt a need for us to write LTO-5 tapes... 
//			try {
				selectedStorageJob.setDeviceWwnId(dataTransferElementName); // Ideally we should be setting this detail upfront but just above we got this info so had to set it here...
				status = labelManager.writeVolumeLabel(selectedStorageJob);
				logger.info("Labelling success? - " + status);
				break;
//			}catch (Exception e) {
//				
//				tapeLibraryManager.unload(tapeLibraryName, storageElementSNo, dteSNo); // unload and continue...
//				
//				// TODO - Check for a specific error msg than keeping it open...
//				logger.debug("Potentially write not supported generation drive. Try with another one"); // LTO-7 cant write in LTO-5 tape 
//				count += 1;
//				continue;
//			}
		}

		if(status)
			afterInitialize(selectedStorageJob);
		else
			throw new Exception("Unable to initialise.");
		
		return new StorageResponse();
		
		// validate on sequence no of tape - upfront validation
		// validate on group archive format vs member archiveformat. they have to be same...
	}	
	
	@Override
	protected void beforeWrite(SelectedStorageJob selectedStorageJob) throws Exception {
		super.beforeWrite(selectedStorageJob);
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		
		Volume tapeToBeUsed = storageJob.getVolume();

		ArtifactVolume lastArtifactOnVolume = artifactVolumeRepositoryUtil.getLastArtifactOnVolume(storageJob.getDomain(), tapeToBeUsed);
		int lastArtifactOnVolumeEndVolumeBlock = 0;
		
		if(lastArtifactOnVolume != null) {
			selectedStorageJob.setLastWrittenArtifactName(lastArtifactOnVolume.getName());
			lastArtifactOnVolumeEndVolumeBlock = lastArtifactOnVolume.getDetails().getEndVolumeBlock();
		}
		/**
		 * No need to recheck here again as TapeJobSelector does the check on the artifact size and the selected Tape' size and excludes the job from selection, if size doesnt fit
		Volume actualTapeToBeUsed = volumeUtil.getToBeUsedPhysicalVolume(storageJob.getDomain(), tapeToBeUsed.getGroupRef().getId(), storageJob.getArtifactSize());
		if(!actualTapeToBeUsed.getId().equals(tapeToBeUsed.getId())) {
			// means at the time of building the storage job the volume selected had enough size 
			// but after it got selected 
			// an already running job updated the new volume size - which is probably making this jobs' artifatsize not fit in the selected tape
			// TODO : ???
		}
		**/
		
		loadTapeAndCheckLabel(selectedStorageJob);

		int blockNumberToBePositioned = 0;
		if(lastArtifactOnVolumeEndVolumeBlock == 0) // during BOT its just the volumelabel + tapemark
			blockNumberToBePositioned = 2;
		else
			blockNumberToBePositioned = lastArtifactOnVolumeEndVolumeBlock + 4;

		logger.trace("Now positioning tape head for writing");
		tapeDriveManager.setTapeHeadPositionForWriting(tapeJob.getDeviceWwnId(), blockNumberToBePositioned); 
		logger.info("Tape Head positioned for writing " + tapeLibraryName + ":" + tapeJob.getDeviceWwnId() + "(" + driveElementAddress + ")");
	}

	
//	@Override
//	protected void beforeVerify(SelectedStorageJob selectedStorageJob) throws Exception {
//		super.beforeVerify(selectedStorageJob);
//		TapeJob tapeJob = (TapeJob) selectedStorageJob;
//		String tapeLibraryName = tapeJob.getTapeLibraryName();
//		int driveElementAddress = tapeJob.getTapedriveNo();
//		int blockNumberToSeek = tapeJob.getArtifactStartVolumeBlock();
//		
//		loadTape(selectedStorageJob);
//
//		tapeDriveManager.setTapeHeadPositionForReading(tapeJob.getDeviceWwnId(), blockNumberToSeek);
//		logger.info("Tape Head positioned for verifying "+ tapeLibraryName + ":" + tapeJob.getDeviceWwnId()+"("+driveElementAddress+")"  + ":" + blockNumberToSeek);
//	}

	@Override
	protected void beforeRestore(SelectedStorageJob selectedStorageJob) throws Exception {
		super.beforeRestore(selectedStorageJob);
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		
		StorageJob storageJob = tapeJob.getStorageJob();
		int blockNumberToSeek = storageJob.getVolumeBlock();
		String timeCodeStart = storageJob.getTimecodeStart();
		if(timeCodeStart != null) { // pfr = true;
			// skip = ((archive_block + header_block) * archiveformatblocksize) + starttimecode's clusterpos - 1 byte[excluding the start byte]
			//skip = ((12 + 3) * 512) + 5840030 - 1 = 5847709
			org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = selectedStorageJob.getFile();
			Domain domain = storageJob.getDomain();
			String path = storageJob.getArtifact().getArtifactclass().getPath();
			logger.trace("path " + path);
			String filePathname = path + File.separator + file.getPathname();
			logger.trace("filePathname " + filePathname);
			String cuesFileEntries = FileUtils.readFileToString(new File(filePathname.replace(PfrConstants.MKV_EXTN, PfrConstants.INDEX_EXTN)));
			//CuesFileParser cfp = new CuesFileParser();
			 // TODO move this method to CuesFileParser
			
			FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, file.getId(), storageJob.getVolume().getId());
			int headerBlocks = fileVolume.getHeaderBlocks();
			logger.trace("archive_block " + storageJob.getArchiveBlock());
			logger.trace("archiveformatblocksize " + storageJob.getVolume().getArchiveformat().getBlocksize());
			logger.trace("starttimecode's clusterpos " + getClusterPosition(cuesFileEntries, timeCodeStart));
			long noOfBytesToBeSkipped = ((storageJob.getArchiveBlock() + headerBlocks) * storageJob.getVolume().getArchiveformat().getBlocksize()) + Integer.parseInt(getClusterPosition(cuesFileEntries, timeCodeStart)) - 1;
			logger.trace("noOfBytesToBeSkipped " + noOfBytesToBeSkipped);
			logger.trace("noOfVolBlocksToBeSkipped " + noOfBytesToBeSkipped/storageJob.getVolume().getDetails().getBlocksize());
			blockNumberToSeek =  blockNumberToSeek + (int) (noOfBytesToBeSkipped/storageJob.getVolume().getDetails().getBlocksize());
			logger.trace("blockNumberToSeek " + blockNumberToSeek);
		}
			
		
		loadTape(selectedStorageJob);
		
		tapeDriveManager.setTapeHeadPositionForReading(tapeJob.getDeviceWwnId(), blockNumberToSeek);
		logger.info("Tape Head positioned for reading "+ tapeLibraryName + ":" + tapeJob.getDeviceWwnId()+"("+driveElementAddress+")"  + ":" + blockNumberToSeek);
	}

	// TODO move this method to CuesFileParser - Aslo move it from TarArchiver tooo...
	private String getClusterPosition(String cuesFileEntries, String timestamp){
		String clusterPosition = null;
		Pattern timestampLineRegexPattern = Pattern.compile("timestamp=" + timestamp + ".000000000 duration=- cluster_position=([0-9]*) relative_position=([0-9]*)");
		
		
		Matcher timestampLineRegexMatcher = timestampLineRegexPattern.matcher(cuesFileEntries);
		if(timestampLineRegexMatcher.find()) {
			clusterPosition = timestampLineRegexMatcher.group(1);
		}

		return clusterPosition;
	}
	
	@Override
	protected void beforeFinalize(SelectedStorageJob selectedStorageJob) throws Exception {
		super.beforeFinalize(selectedStorageJob);
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		Volume tapeToBeUsed = tapeJob.getStorageJob().getVolume();

		ArtifactVolume lastArtifactOnVolume = artifactVolumeRepositoryUtil.getLastArtifactOnVolume(tapeJob.getStorageJob().getDomain(), tapeToBeUsed);
		selectedStorageJob.setLastWrittenArtifactName(lastArtifactOnVolume.getName());

		loadTapeAndCheckLabel(selectedStorageJob);
		
		int lastArtifactOnVolumeEndVolumeBlock = lastArtifactOnVolume.getDetails().getEndVolumeBlock();

		logger.trace("Now positioning tape head for finalizing " + tapeLibraryName + ":" + driveElementAddress);
		tapeDriveManager.setTapeHeadPositionForFinalizing(tapeJob.getDeviceWwnId(), lastArtifactOnVolumeEndVolumeBlock + 4);
		logger.info("Tape Head positioned for finalizing " + tapeLibraryName + ":" + driveElementAddress);
	}
	
	private void loadTapeAndCheckLabel(SelectedStorageJob selectedStorageJob) throws Exception {
		loadTape(selectedStorageJob);
		
		Label labelDetails = getLabelDetails(selectedStorageJob);
		
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		String volumeUuid = volume.getUuid();
		isRightVolume(labelDetails, volumeUuid);

		String lastArtifactNameOnVolume = selectedStorageJob.getLastWrittenArtifactName(); // Will be null for finalizing scenario
		if(lastArtifactNameOnVolume != null)
			isRightPosition(labelDetails, lastArtifactNameOnVolume);
	}
	
	private void loadTape(SelectedStorageJob selectedStorageJob) throws Exception {
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
			
//			if(e.getMessage().contains(driveEmpty) || e.getMessage().contains(driveFull)) { 
//				createCorrectionJobs(selectedStorageJob);
//			}
			throw e;
		}
	}
	
	private Label getLabelDetails(SelectedStorageJob selectedStorageJob) throws Exception {
		Label label = null;
	
		logger.info("Checking if its right tape by reading the label");
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		Volume tapeToBeUsed = tapeJob.getStorageJob().getVolume();
		String tapeBarcode = tapeToBeUsed.getId();
		
		// TODO - Try avoiding this call as 
		int lastArtifactOnVolumeEndVolumeBlock = artifactVolumeRepositoryUtil.getLastArtifactOnVolumeEndVolumeBlock(tapeJob.getStorageJob().getDomain(), tapeToBeUsed);
		
		if(lastArtifactOnVolumeEndVolumeBlock == 0) {
			logger.trace("Will be reading volume label - if need be");
			label = getLabelDetailsFromVolumeLabel(selectedStorageJob, tapeBarcode);
		}
		else {
			logger.trace("Will be reading artifact label");
//			boolean isRightTape = 
//			if(!isRightTape)
//				throw new Exception("Not the right tape loaded " + tapeBarcode + " Something fundamentally wrong. Please contact admin.");
			label = getLabelDetailsFromArtifactLabel(selectedStorageJob, lastArtifactOnVolumeEndVolumeBlock + 4);
		}
		return label;
	}
	

	
	private Label getLabelDetailsFromVolumeLabel(SelectedStorageJob selectedStorageJob, String tapeBarcode) throws Exception {
		// Verifying (if need be) if the tape is the right tape indeed.
		// last job on tape
//		Job lastJobOnTape = jobDao.findTopByVolumeIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(tapeBarcode);
//		
//		LocalDateTime lastJobCompletionTime = LocalDateTime.now();
//		boolean checkRightVolume = false;
//		if(lastJobOnTape != null) {
//			lastJobCompletionTime = lastJobOnTape.getCompletedAt();
//			
//			long intervalBetweenJobsInSeconds = ChronoUnit.SECONDS.between(lastJobCompletionTime, LocalDateTime.now());
//			if(intervalBetweenJobsInSeconds > configuration.getRightVolumeCheckInterval()) {
//				logger.trace("Past volume check threshold. Checking label...");
//				checkRightVolume = true;
//			}
//		}else
//			checkRightVolume = false;
//			
//		
//		if(checkRightVolume) {
			TapeJob tapeJob = (TapeJob) selectedStorageJob;

			String tapeLibraryName = tapeJob.getTapeLibraryName();
			int driveElementAddress = tapeJob.getTapedriveNo();

			tapeDriveManager.setTapeHeadPositionForReadingVolumeLabel(tapeJob.getDeviceWwnId());
			logger.trace("Tape Head positioned for reading label "+ tapeLibraryName + ":" + tapeJob.getDeviceWwnId()+"("+driveElementAddress+")");
			
			return getLabelDetails(selectedStorageJob, true);
//			boolean isRightTape = getLabelDetails(selectedStorageJob, true);
//			if(!isRightTape)
//				throw new Exception("Not the right tape loaded " + tapeBarcode + " Something fundamentally wrong. Please contact admin.");
//		}else {
//			logger.trace("Well inside volume check threshold. Not checking label");
//		}
	}

	private Label getLabelDetailsFromArtifactLabel(SelectedStorageJob selectedStorageJob, int expectedBlockNumberToBePositioned) throws Exception {
		TapeJob tapeJob = (TapeJob) selectedStorageJob;
		tapeDriveManager.setTapeHeadPositionForReadingInterArtifactXml(tapeJob.getDeviceWwnId(), expectedBlockNumberToBePositioned);
		
//		try {
//			
//		}catch (Exception e) {
//			StorageJob storageJob = selectedStorageJob.getStorageJob();
//			Volume volume = storageJob.getVolume();
//			volume.setSuspect(true);
//			volumeDao.save(volume);
//			// TODO - How do we differentiate when to fail the job and when to conitnue
		
//			tapeDriveManager.setTapeHeadPositionForReading(tapeJob.getDeviceWwnId(), lastArtifactOnVolumeEndVolumeBlock - 2);
//		}
		return getLabelDetails(selectedStorageJob, false);
	}

	private Label getLabelDetails(SelectedStorageJob selectedStorageJob, boolean fromVolumelabel) throws Exception {
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		Volume volume = storageJob.getVolume();
		VolumeDetails volumeDetails = volume.getDetails();
		int blocksize = volumeDetails.getBlocksize();

		String deviceName = selectedStorageJob.getDeviceWwnId();
		String volIdFromLabel = null;
		String artifactNameFromLabel = null; // Will be null for first write scenario
		if(fromVolumelabel){
			Volumelabel volumelabel = labelManager.readVolumeLabel(deviceName, blocksize);
			volIdFromLabel = volumelabel.getUuid();
		}
		else { // fromArtifactlabel
			InterArtifactlabel artifactlabel = labelManager.readArtifactLabel(deviceName, blocksize);
			volIdFromLabel = artifactlabel.getVolumeUuid();
			artifactNameFromLabel = artifactlabel.getArtifact();
		}

		Label labelDetails = new Label();
		labelDetails.setVolIdFromLabel(volIdFromLabel);
		labelDetails.setArtifactNameFromLabel(artifactNameFromLabel);
		
		return labelDetails;
	}
	
	private class Label { // TODO Rename this to LabelDetails...
		private String volIdFromLabel = null;
		private String artifactNameFromLabel = null;

		public String getVolIdFromLabel() {
			return volIdFromLabel;
		}
		public void setVolIdFromLabel(String volIdFromLabel) {
			this.volIdFromLabel = volIdFromLabel;
		}
		public String getArtifactNameFromLabel() {
			return artifactNameFromLabel;
		}
		public void setArtifactNameFromLabel(String artifactNameFromLabel) {
			this.artifactNameFromLabel = artifactNameFromLabel;
		}
	}
	
	private boolean isRightVolume(Label labelDetails, String volumeUuid) throws Exception {
		boolean isRightVolume = false;
		
		if(labelDetails.getVolIdFromLabel().equals(volumeUuid)) {
			isRightVolume = true;
			logger.trace("Right volume");
		}
		else {
			String errorMsg = "Loaded volume " + volumeUuid + " mismatches with volumeId on label " + labelDetails.getVolIdFromLabel() + ". Needs admin eyes";
			logger.error(errorMsg);
			throw new Exception(errorMsg);
		}
		return isRightVolume;
	}
	
	private boolean isRightPosition(Label labelDetails, String lastArtifactNameOnVolume) throws Exception {
		boolean isRightPosition = false;
		
		if(labelDetails.getArtifactNameFromLabel().equals(lastArtifactNameOnVolume)) {
			isRightPosition = true;
			logger.trace("Right position");
		}
		else {
			String errorMsg = "Positioned block is wrong. Has a mismatch on artifact Name. Expected " + lastArtifactNameOnVolume + " Actual " + labelDetails.getArtifactNameFromLabel() + ". Needs admin eyes";
			logger.error(errorMsg);
			throw new Exception(errorMsg);
		}
		return isRightPosition;
	}
	
	
	private void createCorrectionJobs(SelectedStorageJob selectedStorageJob) {
//		// last run mapdrive job completed should not be > 60 mts
//		
//		Job lastMapDriveJob = jobDao.findTopByStoragetaskActionIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(Action.map_tapedrives);
//		if(lastMapDriveJob != null) {
//			LocalDateTime lastMapdriveJobCompletionTime = lastMapDriveJob.getCompletedAt();
//				
//			long intervalBetweenJobsInHours = ChronoUnit.HOURS.between(lastMapdriveJobCompletionTime, LocalDateTime.now());
//			if(intervalBetweenJobsInHours > 1) { // TODO configure the hour...
//				logger.info("Past map drive check threshold. Initiating map drives job and requeing the current failed job...");
//				TapeJob tapeJob = (TapeJob) selectedStorageJob;
//				String tapeLibraryName = tapeJob.getTapeLibraryName();
//				
//				autoloaderService.mapDrives(tapeLibraryName);
//				
//				jobService.requeueJob(tapeJob.getStorageJob().getJob().getId());
//			}
//			
//		}
	}
}
