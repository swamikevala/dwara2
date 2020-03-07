package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetype.ArchiveJobsManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.executor.TapeTaskThreadPoolExecutor;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.task.TapeTask;
import org.ishafoundation.dwaraapi.tape.drive.DriveStatusDetails;
import org.ishafoundation.dwaraapi.tape.library.TapeLibraryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class TapeJobsManager extends ArchiveJobsManager{
	
	private static final Logger logger = LoggerFactory.getLogger(TapeJobsManager.class);
	
	@Autowired
	private ApplicationContext applicationContext;	
	
	@Autowired
	private TapeTaskThreadPoolExecutor tapeTaskThreadPoolExecutor;	
	
	@Autowired
	private TapeJobSelector tapeJobSelector;
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;	

	/**
	 * 
  		1) Get Available(Non busy) Drive list - We need to Dequeue as many jobs and Spawn as many threads For eg., If 2 free drives are available then we need to allocate 2 jobs to these drives on their own threads
  		2) Iterate the drive list
			a) choose a job(check for nonoptimsationjobs in the joblist and work on that job list first)
			b) launch a thread to process the job on a drive
			c) remove the job from the list so that the next iteration will not consider it...
	 * @param tapeStorageJobsList
	 */
	@Override
	public void manage(List<StorageJob> tapeStorageJobsList) {
		if(tapeStorageJobsList.size() <= 0) {
			logger.debug("No tape jobs in queue to be processed");
			return;
		}

		// STEP 1
		// TODO : Should we use the DB to get the drive list or from mtx
		// My bet is mtx as it would have the most latest status...
		// Should we validate it against DB...
		logger.trace("Getting Available Drives List");
		List<DriveStatusDetails> availableDrivesList = tapeLibraryManager.getAvailableDrivesList();
		if(availableDrivesList.size() > 0) { // means drive(s) available
			logger.trace("No. of drives available "+ availableDrivesList.size());
			// we need to allocate as many jobs for processing
			for (Iterator<DriveStatusDetails> driveStatusDetailsIterator = availableDrivesList.iterator(); driveStatusDetailsIterator.hasNext();) {
				DriveStatusDetails driveStatusDetails = (DriveStatusDetails) driveStatusDetailsIterator.next();
				logger.debug("Now selecting job for drive - " + driveStatusDetails.getDriveSNo());

				// STEP 2a
				StorageJob storageJob = tapeJobSelector.getJob(tapeStorageJobsList, driveStatusDetails, availableDrivesList);
				
				// STEP 2b
				if(storageJob == null) {
					logger.debug("No tape jobs in queue are eligible to be processed. So skipping the loop");
					break;
				}
				else if(storageJob != null) {
					logger.info("Job selected " + storageJob.getJob().getJobId() + " for " + driveStatusDetails.getDriveSNo());
					TapeTask tapeTask = applicationContext.getBean(TapeTask.class); 
					tapeTask.setStorageJob(storageJob);
					tapeTaskThreadPoolExecutor.getExecutor().execute(tapeTask);
					logger.trace("Job launched on a thread");
					
					// STEP 3 filter the job from the next iteration
					tapeStorageJobsList.remove(storageJob);
				}
				

				if(tapeStorageJobsList.size() <= 0) {
					logger.debug("No tape jobs in queue anymore. So skipping the loop");
					break;
				}
			}
		}
		else {
			logger.info("All drives busy");
		}
	}
}
