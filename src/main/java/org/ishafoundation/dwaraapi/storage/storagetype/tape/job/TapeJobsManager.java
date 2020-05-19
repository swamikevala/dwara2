package org.ishafoundation.dwaraapi.storage.storagetype.tape.job;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ishafoundation.dwaraapi.db.dao.master.TapedriveDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.master.Tapelibrary;
import org.ishafoundation.dwaraapi.enumreferences.TapedriveStatus;
import org.ishafoundation.dwaraapi.storage.constants.StorageOperation;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetype.ArchiveJobsManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveStatusDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeDrivePreparer;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.executor.TapeTaskThreadPoolExecutor;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.task.TapeTask;
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
	private TapedriveDao tapedriveDao;	
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;	

	@Autowired
	private TapeDrivePreparer tapeDrivePreparer;
	/**
	 * 
  		1) Get Available(Non busy) Drive list - We need to Dequeue as many jobs and Spawn as many threads For eg., If 2 free drives are available then we need to allocate 2 jobs to these drives on their own threads
  		2) Iterate the drive list
			a) choose a job(check for nonoptimisationjobs in the joblist and work on that job list first)
			b) launch a thread to process the job on a drive
			c) remove the job from the list so that the next iteration will not consider it...
	 * @param tapeStorageJobsList
	 */
	@Override
	public void manage(List<StorageJob> tapeStorageJobsList) {
		if(tapeStorageJobsList.size() <= 0) { // Double verifying if there are any tape jobs or not
			logger.debug("No tape jobs in queue to be processed");
			return;
		}
		
		// if there is a label job only one job will be in the list coming from JobManager... so we are skipping job selection...
		if(tapeStorageJobsList.get(0).getStorageOperation() == StorageOperation.FORMAT) {
			StorageJob formatJob = tapeStorageJobsList.get(0);	
			logger.info("Format job " + formatJob.getJob().getId() + " dequeued");
			Tape tape = formatJob.getVolume().getTape();
			String barcode = tape.getBarcode();
			HashMap<Tapelibrary, List<DataTransferElement>> prepared_tapelibrary_dteList_map = tapeDrivePreparer.prepareAllTapeDrivesForBlockingJobs();
			Set<Tapelibrary> tapelibrarySet = prepared_tapelibrary_dteList_map.keySet();
			
			
			for (Iterator<Tapelibrary> iterator = tapelibrarySet.iterator(); iterator.hasNext();) {
				Tapelibrary tapelibrary = (Tapelibrary) iterator.next();
				
				String tapelibraryName = tapelibrary.getName();
				logger.debug("Now finding the library the tape is in");
				List<String> loadedTapeList = tapeLibraryManager.getAllLoadedTapesInTheLibrary(tapelibraryName);
				
				if(!loadedTapeList.contains(barcode)) {
					logger.debug(barcode + " not in " + tapelibraryName);
					continue;	
				}else {
					logger.debug(barcode + " in " + tapelibraryName);
				}
					
				List<DataTransferElement> allDrives = prepared_tapelibrary_dteList_map.get(tapelibrary);
				int driveSNo = allDrives.get(0).getsNo(); // any drive is fine...// TODO : Generation specific drive need to be chosen here...
				
				Tapedrive tapedrive = tapedriveDao.findByTapelibraryNameAndElementAddress(tapelibraryName, driveSNo);
				tapedrive.setStatus(TapedriveStatus.BUSY.toString());
				//tapedrive.setTape(tape); only format completed tape should be added into our systems...
				tapedrive.setJob(formatJob.getJob());
				logger.debug("DB Tapedrive Updation " + tapedrive.getId() + ":" + tapedrive.getStatus() + "-" + tapedrive.getJob().getId());   
				tapedriveDao.save(tapedrive);
				logger.debug("DB Tapedrive Updation - Success");
				
				formatJob.setTapeLibraryName(tapelibraryName);
				formatJob.setDriveNo(driveSNo);
				formatJob.setDeviceWwid(tapedrive.getDeviceWwid());

				logger.info("Job " + formatJob.getJob().getId() + " selected for " + driveSNo);
				TapeTask tapeTask = applicationContext.getBean(TapeTask.class); 
				tapeTask.setStorageJob(formatJob);
				tapeTaskThreadPoolExecutor.getExecutor().execute(tapeTask);
				logger.trace("Job launched on a thread");
				
				break;
			}
		}else {
			// STEP 1
			// TODO : Should we use the DB to get the drive list or from mtx
			// My bet is mtx as it would have the most latest status...
			// Should we validate it against DB...
			logger.trace("Getting Available Drives List");
			List<DriveStatusDetails> availableDrivesList = tapeLibraryManager.getAvailableDrivesList();
			if(availableDrivesList.size() > 0) { // means drive(s) available
				logger.trace("No. of drives available "+ availableDrivesList.size());
	
				// TODO - To load balance across drives based on their usage. The usage parameters is not retrieved...
	//			Map<Integer, DriveStatusDetails> usage_driveStatusDetails = new TreeMap<Integer, DriveStatusDetails>(); 
	//			for (Iterator<DriveStatusDetails> driveStatusDetailsIterator = availableDrivesList.iterator(); driveStatusDetailsIterator.hasNext();) {
	//				DriveStatusDetails driveStatusDetails = (DriveStatusDetails) driveStatusDetailsIterator.next();
	//				usage_driveStatusDetails.put(driveStatusDetails.getTotalUsageInHours(), driveStatusDetails); // TODO Need to decide based on what parameter the load has to be balanced...
	//			}
	//			Set<Integer> treeSet = new TreeSet<Integer>();
	//			treeSet.addAll(usage_driveStatusDetails.keySet());
	//			for (Integer usageHours : treeSet) {
	//				DriveStatusDetails driveStatusDetails = usage_driveStatusDetails.get(usageHours);
	//				// code goes here
	//			}
	
				// we need to allocate as many jobs for processing
				for (Iterator<DriveStatusDetails> driveStatusDetailsIterator = availableDrivesList.iterator(); driveStatusDetailsIterator.hasNext();) {
					DriveStatusDetails driveStatusDetails = (DriveStatusDetails) driveStatusDetailsIterator.next();
					logger.debug("Now selecting job for drive - " + driveStatusDetails.getDriveSNo());
	
					// STEP 2a
					StorageJob storageJob = tapeJobSelector.getJob(tapeStorageJobsList, driveStatusDetails);
					
					// STEP 2b
					if(storageJob == null) {
						logger.debug("No tape jobs in queue are eligible to be processed for the drive");
						//break;
					}
					else if(storageJob != null) {
						logger.info("Job " + storageJob.getJob().getId() + " selected for " + driveStatusDetails.getDriveSNo());
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
}
