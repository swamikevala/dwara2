package org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.task;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.constants.TapedriveStatus;
import org.ishafoundation.dwaraapi.db.dao.master.TapeDao;
import org.ishafoundation.dwaraapi.db.dao.master.TapedriveDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.storage.constants.StorageOperation;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeJobProcessor;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.TapeDriveManager;
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
			job.setStartedAt(LocalDateTime.now());
			job.setStatus(status);
			logger.debug("DB Job Updation " + status);
			jobDao.save(job);
			logger.debug("DB Job Updation - Success");
			
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

			if(StorageOperation.WRITE == storageOperation) {
				logger.trace("Now positioning tape head for writing");
				tapeDriveManager.setTapeHeadPositionForWriting(tapeLibraryName, driveElementAddress); // FIXME - check on this, using eod, bsf 1 and fsf 1
				logger.trace("Tape Head positioned for writing");
				ArchiveResponse archiveResponse = tapeJobProcessor.write(storageJob);
				
				logger.trace("Now verifying written content by reading again to disk and compare CRCs");
				int blockNumberToSeek = archiveResponse.getLibraryBlockNumber();
				logger.trace("Now positioning tape head for reading to verify written content");
				tapeDriveManager.setTapeHeadPositionForReading(tapeLibraryName, driveElementAddress, blockNumberToSeek);// FIXME - Need offset???...Not needed for BRU...
				logger.trace("Tape Head positioned for reading");

				StorageJob preparedStorageJob = new StorageJob();
				preparedStorageJob.setJob(storageJob.getJob());

				preparedStorageJob.setStorageformat(storageJob.getStorageformat());
				preparedStorageJob.setStorageOperation(StorageOperation.READ);
				
				preparedStorageJob.setDeviceWwid(storageJob.getDeviceWwid());
				preparedStorageJob.setFilePathname(archiveResponse.getLibraryName());
				preparedStorageJob.setDestinationPath(writeVerifyReadTmpLocation);
				preparedStorageJob.setVolume(storageJob.getVolume());

				tapeJobProcessor.read(preparedStorageJob);
				logger.trace("Written content read to disk and ready for comparison");
				
				//verify the crc
				List<org.ishafoundation.dwaraapi.db.model.transactional.File> libraryFileList = fileDao.findAllByLibraryId(storageJob.getLibrary().getId());
				
				for (Iterator<org.ishafoundation.dwaraapi.db.model.transactional.File> iterator = libraryFileList.iterator(); iterator.hasNext();) {
					org.ishafoundation.dwaraapi.db.model.transactional.File nthFile = iterator.next();
					Long crcOfSource = nthFile.getCrc();
					if(crcOfSource != null) { // For directories crc is null
						String filePathname = nthFile.getPathname();
						File archivedFile = new File(writeVerifyReadTmpLocation + File.separator + filePathname);
						Long archivedFileCrc = CRCUtil.getCrc(archivedFile);
						if(archivedFileCrc.longValue() == crcOfSource.longValue())
							logger.trace("Source and archive CRC Matches");
						else {
							String errorMsg = "Source and archived file CRC Mismatches " + filePathname;
							logger.error(errorMsg);
							throw new Exception(errorMsg);
						}
					}
				}

				status = Status.completed;
			}
			else if(StorageOperation.READ == storageOperation) {
				logger.trace("Now positioning tape head for reading");
				tapeDriveManager.setTapeHeadPositionForReading(tapeLibraryName, driveElementAddress, storageJob.getBlock());// FIXME - Need offset???...Not needed for BRU...
				logger.trace("Tape Head positioned for reading");
				tapeJobProcessor.read(storageJob);
				status = Status.completed;
			}
			else if(StorageOperation.FORMAT == storageOperation) {
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
					
					job.setTape(tape);
					status = Status.completed;
				}
				else { // TODO Force option still pending
					String errMsg = "Tape not blank. Not proceeding with format";
					logger.error(errMsg);
					throw new Exception(errMsg);
				}
			}
		} catch (Throwable e) {
			logger.error("Unable to complete tape task " + storageJob.toString(), e);
			status = Status.failed;
		}finally {
			job.setStatus(status);
			job.setCompletedAt(LocalDateTime.now());
			logger.debug("DB Job Updation " + status);
			jobDao.save(job);
			logger.debug("DB Job Updation - Success");
			
			Tapedrive tapedrive = tapedriveDao.findByTapelibraryNameAndElementAddress(storageJob.getTapeLibraryName(), storageJob.getDriveNo());
			tapedrive.setStatus(TapedriveStatus.AVAILABLE.toString());
			tapedrive.setTape(null);
			tapedrive.setJob(null);
			logger.debug("DB Tapedrive Updation " + tapedrive.getStatus());
			tapedriveDao.save(tapedrive);
			logger.debug("DB Tapedrive Updation - Success");
		}
	}	
}
