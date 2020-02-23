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
		// STEP 1
		// TODO : Should we use the DB to get the drive list or from mtx
		// My bet is mtx as it would have the most latest status...
		List<DriveStatusDetails> driveList = TapeLibraryManager.getAvailableDrivesList();
		if(driveList.size() > 0) { // means drive(s) available
			logger.trace("no. of drives available "+ driveList.size());
			// we need to allocate as many jobs for processing
			for (Iterator<DriveStatusDetails> driveStatusDetailsIterator = driveList.iterator(); driveStatusDetailsIterator.hasNext();) {
				if(tapeStorageJobsList.size() > 0) {
					DriveStatusDetails driveStatusDetails = (DriveStatusDetails) driveStatusDetailsIterator.next();
					
					// STEP 2a
					StorageJob storageJob = tapeJobSelector.getJob(tapeStorageJobsList, driveStatusDetails);
					logger.trace("job selected");
					
					// STEP 2b
					if(storageJob != null) {
						TapeTask tapeTask = applicationContext.getBean(TapeTask.class); 
						tapeTask.setArchiveJob(storageJob);
						tapeTaskThreadPoolExecutor.getExecutor().execute(tapeTask);
						logger.trace("job launched on a thread");
					}
					
					// STEP 3 filter the job from the next iteration
					tapeStorageJobsList.remove(storageJob);
				}
			}
		}
		else 
			System.out.println("do nothing");
	}
}
