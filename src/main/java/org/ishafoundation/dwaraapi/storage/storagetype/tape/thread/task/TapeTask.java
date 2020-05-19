package org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.task;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.TapeDao;
import org.ishafoundation.dwaraapi.db.dao.master.TapedriveDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.TapedriveStatus;
import org.ishafoundation.dwaraapi.storage.constants.StorageOperation;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.job.TapeJobProcessor;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.label.TapeLabelManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.utils.CRCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class TapeTask implements Runnable{
	
	private static final Logger logger = LoggerFactory.getLogger(TapeTask.class);
	
	@Value("${tape.readlabel.intervalbetweenjobsinseconds}")
	int intervalBetweenJobsInSecondsThreshold;
	
	@Value("${tape.writeverify.readtmplocation}")
	String writeVerifyReadTmpLocation;

	@Autowired
	private TapeDao tapeDao;	

	@Autowired
	private JobDao jobDao;
	
    @Autowired
    private FileDao fileDao;	
	
	@Autowired
	private TapedriveDao tapedriveDao;
	
	@Autowired
	private TapeJobProcessor tapeJobProcessor;	
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@Autowired
	private TapeDriveManager tapeDriveManager;
	
	@Autowired
	private TapeLabelManager tapeLabelManager;
	
	
	private StorageJob storageJob;

	
	public StorageJob getStorageJob() {
		return storageJob;
	}

	public void setStorageJob(StorageJob storageJob) {
		this.storageJob = storageJob;
	}
	
	@Override
	public void run() {
		Status status = Status.in_progress;
		Job job = (Job) storageJob.getJob();
		try {
			updateJobInProgress(job);
			
			String tapeLibraryName = storageJob.getTapeLibraryName();
			int driveElementAddress = storageJob.getDriveNo();
			logger.trace("Now running storagejob " + storageJob.getJob().getId() + " on drive " + driveElementAddress);
			Tape tapeToBeUsed = storageJob.getVolume().getTape();
			logger.trace("Checking if drive " + driveElementAddress + " is already loaded with any tape");
			if(storageJob.isDriveAlreadyLoadedWithTape()) {
				logger.trace("Tape " + tapeToBeUsed.getBarcode() + " is already loaded on to drive " + driveElementAddress);
			}
			else {
				try {
					logger.trace("Now loading tape " + tapeToBeUsed.getBarcode() + " on to drive " + driveElementAddress);
					tapeLibraryManager.locateAndLoadTapeOnToDrive(tapeToBeUsed.getBarcode(), tapeLibraryName, driveElementAddress);
				} catch (Exception e) {
					logger.error("Unable to load tape " + tapeToBeUsed.getBarcode() + " on to drive " + driveElementAddress, e);
					throw e;
				}
			}

			StorageOperation storageOperation = storageJob.getStorageOperation();
			
			isRightTape(storageOperation, tapeToBeUsed, tapeLibraryName, driveElementAddress);

			if(StorageOperation.WRITE == storageOperation) {
				archive(tapeLibraryName, driveElementAddress);
				status = Status.completed;
			}
			else if(StorageOperation.READ == storageOperation) {
				restore(storageJob, tapeLibraryName, driveElementAddress, storageJob.getBlock());
				status = Status.completed;
			}
			else if(StorageOperation.FORMAT == storageOperation) {
				format(job, tapeLibraryName, driveElementAddress);
				status = Status.completed;
			}
		} catch (Throwable e) {
			logger.error("Unable to complete tape task " + storageJob.toString(), e);
			status = Status.failed;
		}finally {
			updateJobStatus(job, status);
			
			Tapedrive tapedrive = tapedriveDao.findByTapelibraryNameAndElementAddress(storageJob.getTapeLibraryName(), storageJob.getDriveNo());
			tapedrive.setStatus(TapedriveStatus.AVAILABLE.toString());
			tapedrive.setTape(null);
			tapedrive.setJob(null);
			logger.debug("DB Tapedrive Updation " + tapedrive.getStatus());
			tapedriveDao.save(tapedrive);
			logger.debug("DB Tapedrive Updation - Success");
			
			//finalizeTapeIfNeedBe(storageJob.getVolume())
		}
	}	
	
	private void isRightTape(StorageOperation storageOperation, Tape tapeToBeUsed, String tapeLibraryName, int driveElementAddress) throws Exception {
		if(StorageOperation.FORMAT != storageOperation) {
			// Verifying (if need be) if the tape is the right tape indeed.
			// last job on tape
			Job lastJobOnTape = jobDao.findTopByTapeBarcodeOrderByIdDesc(tapeToBeUsed.getBarcode());
			
			LocalDateTime lastJobCompletionTime = LocalDateTime.now();
			if(lastJobOnTape != null)
				lastJobCompletionTime = lastJobOnTape.getCompletedAt();
			
			long intervalBetweenJobsInSeconds = ChronoUnit.SECONDS.between(lastJobCompletionTime, LocalDateTime.now());
			
			if(intervalBetweenJobsInSeconds > intervalBetweenJobsInSecondsThreshold) {
				String driveWwid = tapeDriveManager.getDriveWwid(tapeLibraryName, driveElementAddress);

				boolean isRightTape = tapeLabelManager.isRightTape(driveWwid, tapeToBeUsed.getBarcode());
				if(!isRightTape)
					throw new Exception("Not the right tape loaded " + tapeToBeUsed.getBarcode() + " Something fundamentally wrong. Please contact admin.");
			}
		}
	}
	

	
	
	private void archive(String tapeLibraryName, int driveElementAddress) throws Throwable {
		Job archiveJob = storageJob.getJob();
		
		// get the write job here based on archivejob // TODO
		Job writeJob = null;
		ArchiveResponse archiveResponse = null;
		try {
			updateJobInProgress(writeJob);
			
			archiveResponse = write(tapeLibraryName, driveElementAddress);
			
			updateJobCompleted(writeJob);
		}catch (Exception e) {
			updateJobFailed(writeJob);
			throw e;
		}
		
		// get the verify job here // TODO
		Job verifyJob = null;
		try {
			updateJobInProgress(verifyJob);
			
			verify(verifyJob, tapeLibraryName, driveElementAddress, archiveResponse);
			
			updateJobCompleted(verifyJob);
		}catch (Exception e) {
			updateJobFailed(verifyJob);
			throw e;
		}
	}

	private ArchiveResponse write(String tapeLibraryName, int driveElementAddress) throws Throwable {
		logger.trace("Now positioning tape head for writing");
		tapeDriveManager.setTapeHeadPositionForWriting(tapeLibraryName, driveElementAddress); // FIXME - check on this, using eod, bsf 1 and fsf 1
		logger.trace("Tape Head positioned for writing");
		return tapeJobProcessor.write(storageJob);
	}
	
	

	private void verify(Job verifyJob, String tapeLibraryName, int driveElementAddress, ArchiveResponse archiveResponse) throws Throwable {
		logger.trace("Now verifying written content by reading again to disk and compare CRCs");

		// get the restore job here // TODO
		Job restoreJob = null;
		try {
			updateJobInProgress(restoreJob);

			int blockNumberToSeek = archiveResponse.getLibraryBlockNumber();
			StorageJob preparedStorageJob = buildStorageJobForReadingWrittenContent(restoreJob, archiveResponse);
			logger.trace("Now positioning tape head for reading to verify written content");
			restore(preparedStorageJob, tapeLibraryName, driveElementAddress, blockNumberToSeek);
			logger.trace("Written content read to disk and ready for comparison");

			updateJobCompleted(restoreJob);
		}catch (Exception e) {
			updateJobFailed(restoreJob);
			throw e;
		}
		
		// get the verifyCrc job here // TODO
		Job verifyCrc = null;
		try {
			updateJobInProgress(verifyCrc);
			
			verifyCrc(storageJob.getLibrary().getId());
			
			updateJobCompleted(verifyCrc);
		}catch (Exception e) {
			updateJobFailed(verifyCrc);
			throw e;
		}			

	}

	
	private StorageJob buildStorageJobForReadingWrittenContent(Job restoreJob, ArchiveResponse archiveResponse) {
		StorageJob preparedStorageJob = new StorageJob();
		preparedStorageJob.setJob(restoreJob);

		preparedStorageJob.setStorageformat(storageJob.getStorageformat());
		preparedStorageJob.setStorageOperation(StorageOperation.READ);
		
		preparedStorageJob.setDeviceWwid(storageJob.getDeviceWwid());
		preparedStorageJob.setFilePathname(archiveResponse.getLibraryName());
		preparedStorageJob.setDestinationPath(writeVerifyReadTmpLocation);
		preparedStorageJob.setVolume(storageJob.getVolume());
		return preparedStorageJob;
	}
	
	private void restore(StorageJob storageJob, String tapeLibraryName, int driveElementAddress, int blockNumberToSeek) throws Throwable {
		
		logger.trace("Now positioning tape head for reading");
		tapeDriveManager.setTapeHeadPositionForReading(tapeLibraryName, driveElementAddress, blockNumberToSeek);// FIXME - Need offset???...Not needed for BRU...
		logger.trace("Tape Head positioned for reading");
		tapeJobProcessor.read(storageJob);
		
	}
	
	private void verifyCrc(int libraryId) throws Exception {
		logger.trace("Source file and archive file CRC Matches");
		List<org.ishafoundation.dwaraapi.db.model.transactional.File> libraryFileList = fileDao.findAllByLibraryId(libraryId);
		
		for (Iterator<org.ishafoundation.dwaraapi.db.model.transactional.File> iterator = libraryFileList.iterator(); iterator.hasNext();) {
			org.ishafoundation.dwaraapi.db.model.transactional.File nthFile = iterator.next();
			Long crcOfSource = nthFile.getCrc();
			if(crcOfSource != null) { // For directories crc is null
				String filePathname = nthFile.getPathname();
				File archivedFile = new File(writeVerifyReadTmpLocation + File.separator + filePathname);
				Long archivedFileCrc = CRCUtil.getCrc(archivedFile);
				if(archivedFileCrc.longValue() == crcOfSource.longValue()) {
					//logger.trace("Source file and archive file CRC Matches");
				}
				else {
					String errorMsg = "Source and archived file CRC Mismatches " + filePathname;
					logger.error(errorMsg);
					throw new Exception(errorMsg);
				}
			}
		}
		logger.trace("Source file and archive file CRC Matches");
	}

	private void format(Job job, String tapeLibraryName, int driveElementAddress) throws Throwable {
		logger.trace("Now checking if tape is indeed blank");
		boolean isTapeBlank = tapeDriveManager.isTapeBlank(tapeLibraryName, driveElementAddress);
		if(isTapeBlank) {
			logger.trace("Tape blank proceeding with formatting");
			tapeDriveManager.setTapeHeadPositionForFormatting(tapeLibraryName, driveElementAddress);
			tapeJobProcessor.format(storageJob);
			Tape tape = storageJob.getVolume().getTape();
			
			logger.debug("DB Tape Insertion " + tape.getBarcode());   
			tape = tapeDao.save(tape);
			logger.debug("DB Tape Insertion - Success");
			
			job.setTape(tape); // TODO what is this for...
		}
		else { // TODO Force option still pending
			String errMsg = "Tape not blank. Not proceeding with format";
			logger.error(errMsg);
			throw new Exception(errMsg);
		}
	}
	private void updateJobInProgress(Job job) {
		job.setStartedAt(LocalDateTime.now());
		updateJobStatus(job, Status.in_progress);
	}
	
	private void updateJobCompleted(Job job) {
		job.setCompletedAt(LocalDateTime.now());
		updateJobStatus(job, Status.completed);
	}
	
	private void updateJobFailed(Job job) {
		updateJobStatus(job, Status.failed);
	}
	
	private void updateJobStatus(Job job, Status status) {
		job.setStatus(status);
		logger.debug("DB Job Updation " + status);
		jobDao.save(job);
		logger.debug("DB Job Updation - Success");
	}
}
