package org.ishafoundation.dwaraapi.storage.storagetype.tape.thread;

import java.util.Map;

import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.StoragetaskAttributeConverter;
import org.ishafoundation.dwaraapi.enumreferences.Storagetask;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetask;
import org.ishafoundation.dwaraapi.storage.storagetask.StoragetaskFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class TapeTask implements Runnable{
	
//	private static final Logger logger = LoggerFactory.getLogger(TapeTask.class);
//	
//	@Value("${tape.readlabel.intervalbetweenjobsinseconds}")
//	int intervalBetweenJobsInSecondsThreshold;
//	
//	@Value("${tape.writeverify.readtmplocation}")
//	String writeVerifyReadTmpLocation;
//
//	@Autowired
//	private VolumeDao volumeDao;	
//
//	@Autowired
//	private JobDao jobDao;
//	
//    @Autowired
//    private FileDao fileDao;	
//	
//	@Autowired
//	private TapedriveDao tapedriveDao;
//	
//	@Autowired
//	private TapeJobProcessor tapeJobProcessor;	
//	
//	@Autowired
//	private TapeLibraryManager tapeLibraryManager;
//	
//	@Autowired
//	private TapeDriveManager tapeDriveManager;
//	
//	@Autowired
//	private TapeLabelManager tapeLabelManager;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private StoragetaskAttributeConverter storagetaskAttributeConverter;
	
	@Autowired
	private Map<String, AbstractStoragetask> storagetaskMap;
	
	private TapeJob tapeJob;
	
	public TapeJob getTapeJob() {
		return tapeJob;
	}

	public void setTapeJob(TapeJob tapeJob) {
		this.tapeJob = tapeJob;
	}

	@Override
	public void run() {
		System.out.println(this.getClass().getName() + " prototype scoped tape task");
//		Status status = Status.in_progress;
//		Job job = (Job) storageJob.getJob();
//		try {
//			updateJobInProgress(job);
//			
//			String tapeLibraryName = storageJob.getTapeLibraryName();
//			int driveElementAddress = storageJob.getDriveNo();
//			logger.trace("Now running storagejob " + storageJob.getJob().getId() + " on drive " + driveElementAddress);
//			Volume tapeToBeUsed = storageJob.getVolume();
//			logger.trace("Checking if drive " + driveElementAddress + " is already loaded with any tape");
//			if(storageJob.isDriveAlreadyLoadedWithTape()) {
//				logger.trace("Tape " + tapeToBeUsed.getBarcode() + " is already loaded on to drive " + driveElementAddress);
//			}
//			else {
//				try {
//					logger.trace("Now loading tape " + tapeToBeUsed.getBarcode() + " on to drive " + driveElementAddress);
//					tapeLibraryManager.locateAndLoadTapeOnToDrive(tapeToBeUsed.getBarcode(), tapeLibraryName, driveElementAddress);
//				} catch (Exception e) {
//					logger.error("Unable to load tape " + tapeToBeUsed.getBarcode() + " on to drive " + driveElementAddress, e);
//					throw e;
//				}
//			}
//
//			
//			
//			Storagetask storagetask = storageJob.getStoragetask();
//			
//			isRightTape(storagetask, tapeToBeUsed, tapeLibraryName, driveElementAddress);
			Integer dbData = tapeJob.getStorageJob().getJob().getStoragetaskId();
			Storagetask storagetask = storagetaskAttributeConverter.convertToEntityAttribute(dbData);
			org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetask storagetaskImpl = StoragetaskFactory.getInstance(applicationContext, storagetask.name());
			
			storagetaskImpl = storagetaskMap.get(storagetask.name());
			
			try {
				System.out.println("\t\tcalling storage task impl " + storagetask.name());
				storagetaskImpl.process(tapeJob);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			status = Status.completed;
//
////			if(Storagetask.WRITE == storagetask) {
////				archive(tapeLibraryName, driveElementAddress);
////				status = Status.completed;
////			}
////			else if(Storagetask.READ == storagetask) {
////				restore(storageJob, tapeLibraryName, driveElementAddress, storageJob.getBlock());
////				status = Status.completed;
////			}
////			else if(Storagetask.FORMAT == storagetask) {
////				format(job, tapeLibraryName, driveElementAddress);
////				status = Status.completed;
////			}
//		} catch (Throwable e) {
//			logger.error("Unable to complete tape task " + storageJob.toString(), e);
//			status = Status.failed;
//		}finally {
//			updateJobStatus(job, status);
//			
//			Tapedrive tapedrive = tapedriveDao.findByTapelibraryNameAndElementAddress(storageJob.getTapeLibraryName(), storageJob.getDriveNo());
//			tapedrive.setStatus(DeviceStatus.AVAILABLE.toString());
//			tapedrive.setTape(null);
//			tapedrive.setJob(null);
//			logger.debug("DB Tapedrive Updation " + tapedrive.getStatus());
//			tapedriveDao.save(tapedrive);
//			logger.debug("DB Tapedrive Updation - Success");
//			
//			//finalizeTapeIfNeedBe(storageJob.getVolume())
//		}
	}	
	
//	private void isRightTape(Storagetask storagetask, Volume tapeToBeUsed, String tapeLibraryName, int driveElementAddress) throws Exception {
//		if(Storagetask.format != storagetask) {
//			// Verifying (if need be) if the tape is the right tape indeed.
//			// last job on tape
//			Job lastJobOnTape = jobDao.findTopByTapeBarcodeOrderByIdDesc(tapeToBeUsed.getBarcode());
//			
//			LocalDateTime lastJobCompletionTime = LocalDateTime.now();
//			if(lastJobOnTape != null)
//				lastJobCompletionTime = lastJobOnTape.getCompletedAt();
//			
//			long intervalBetweenJobsInSeconds = ChronoUnit.SECONDS.between(lastJobCompletionTime, LocalDateTime.now());
//			
//			if(intervalBetweenJobsInSeconds > intervalBetweenJobsInSecondsThreshold) {
//				String driveWwid = tapeDriveManager.getDriveWwid(tapeLibraryName, driveElementAddress);
//
//				boolean isRightTape = tapeLabelManager.isRightTape(driveWwid, tapeToBeUsed.getBarcode());
//				if(!isRightTape)
//					throw new Exception("Not the right tape loaded " + tapeToBeUsed.getBarcode() + " Something fundamentally wrong. Please contact admin.");
//			}
//		}
//	}
	

//	private void format(Job job, String tapeLibraryName, int driveElementAddress) throws Throwable {
//		logger.trace("Now checking if tape is indeed blank");
//		boolean isTapeBlank = tapeDriveManager.isTapeBlank(tapeLibraryName, driveElementAddress);
//		if(isTapeBlank) {
//			logger.trace("Tape blank proceeding with formatting");
//			tapeDriveManager.setTapeHeadPositionForFormatting(tapeLibraryName, driveElementAddress);
//			tapeJobProcessor.format(storageJob);
//			Tape tape = storageJob.getVolume().getTape();
//			
//			logger.debug("DB Tape Insertion " + tape.getBarcode());   
//			tape = volumeDao.save(tape);
//			logger.debug("DB Tape Insertion - Success");
//			
//			job.setTape(tape); // TODO what is this for...
//		}
//		else { // TODO Force option still pending
//			String errMsg = "Tape not blank. Not proceeding with format";
//			logger.error(errMsg);
//			throw new Exception(errMsg);
//		}
//	}

}
