package org.ishafoundation.dwaraapi.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeDeviceUtil;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.utils.TapeUsageStatus;
import org.ishafoundation.dwaraapi.utils.VolumeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

//@Component
@RestController
public class ScheduledTapeUnloader {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduledTapeUnloader.class);

	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private TapeDeviceUtil tapeDeviceUtil;
	
	@Autowired
	private VolumeUtil volumeUtil;
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@Value("${scheduler.tapeUnloader.allowedTapeIdleSittingDuration}")
	private int allowedTapeIdleSittingDuration;

	@Scheduled(cron = "${scheduler.tapeUnloader.cronExpression}")
	@PostMapping("/unloadIdleTapesFromDrive")
	public void unloadIdleTapesFromDrive(){
		logger.info("***** Unloading idle tapes from Drive now *****");
		List<DriveDetails> availableDrivesDetails;
		try {
			availableDrivesDetails = tapeDeviceUtil.getAllAvailableDrivesDetails();
			for (DriveDetails nthAvailableDriveDetails : availableDrivesDetails) {
				String tapeBarcode = nthAvailableDriveDetails.getDte().getVolumeTag();
				if(tapeBarcode != null) { // means tape loaded in the drive
					logger.trace(tapeBarcode + " is loaded in " + nthAvailableDriveDetails.getDriveId());
					Job lastJobOnTape = jobDao.findTopByStoragetaskActionIdIsNotNullAndVolumeIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(tapeBarcode);
					
					LocalDateTime lastJobCompletionTime = LocalDateTime.now();
					boolean idleSittingTimePastThreshold = false;
					if(lastJobOnTape != null) {
						lastJobCompletionTime = lastJobOnTape.getCompletedAt();
						
						long durationSinceTapeLastUsedOnTheDrive = ChronoUnit.MINUTES.between(lastJobCompletionTime, LocalDateTime.now());
						if(durationSinceTapeLastUsedOnTheDrive > allowedTapeIdleSittingDuration) {
							logger.trace("Past configured idle sitting time threshold.");
							idleSittingTimePastThreshold = true;
						}
					}else
						idleSittingTimePastThreshold = false;
					
					if(idleSittingTimePastThreshold) {
						 TapeUsageStatus tapeUsageStatus = volumeUtil.getTapeUsageStatus(tapeBarcode);
						 if(tapeUsageStatus == TapeUsageStatus.no_job_queued) { // only when no queued or in progress job on the volume
							// TODO : check mtstatus and tactive device table one last time before pulling the plug...
							 logger.info("No jobs lined up for " + tapeBarcode + ". Will be unloading it");
							 tapeLibraryManager.unload(nthAvailableDriveDetails.getTapelibraryName(), nthAvailableDriveDetails.getDte().getsNo());
						 }
					}
				}
			}

		} catch (Exception e) {
			logger.error("Unable to unload idle tapes from drives", e);
		}
	}
}