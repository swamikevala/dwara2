package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.constants.TapedriveStatus;
import org.ishafoundation.dwaraapi.db.dao.TapedriveDao;
import org.ishafoundation.dwaraapi.db.dao.master.common.RequesttypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.storage.TapeDao;
import org.ishafoundation.dwaraapi.db.dao.master.workflow.CopyDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.LibraryDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.db.model.master.common.Requesttype;
import org.ishafoundation.dwaraapi.db.model.master.storage.Tape;
import org.ishafoundation.dwaraapi.db.model.master.workflow.Copy;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.model.Volume;
import org.ishafoundation.dwaraapi.storage.constants.StorageOperation;
import org.ishafoundation.dwaraapi.storage.model.GroupedJobsCollection;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.tape.drive.DriveStatusDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeJobSelector {
	
	private static final Logger logger = LoggerFactory.getLogger(TapeJobSelector.class);
	
	@Autowired
	private TapedriveDao tapedriveDao;	
	
	@Autowired
	private LibraryDao libraryDao;		
	
	@Autowired
	private RequesttypeDao requesttypeDao;
	
	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private SubrequestDao subrequestDao;
	
	@Autowired
	private CopyDao copyDao;
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private TapeDao tapeDao;

	/**
	 * Method responsible for getting a job for the drive
	 * 
	 * First check if there are ignoreVolumeOptimasation Jobs and choose a job
	 * else choose a job from the exhaustive list 
	 * 
	 * @param tapeJobsList
	 * @param driveStatusDetails
	 * @return
	 */
	
	public StorageJob getJob(List<StorageJob> tapeJobsList, DriveStatusDetails driveStatusDetails) {
		
		List<StorageJob> ignoreOptimisationTapeJobsList = getIgnoreOptimisationTapeJobsList(tapeJobsList);
		
		// TODO For now defaulting it to the first one...
		//StorageJob archiveJob = tapeStorageJobsList.get(0);
		StorageJob tapeJob = null;
		if(ignoreOptimisationTapeJobsList.size() > 0) { // are there IgnoreVolumeOptimisation jobs
			logger.debug("contains Ignore Tape Optimisation jobs");
			// if yes then get a job that is optimised - Yes we need optimisation even on the IgnoreVolumeOptimasation job list...
			tapeJob = chooseAJobForTheDrive(ignoreOptimisationTapeJobsList, driveStatusDetails);
		} else {
			logger.debug("doesnt contain any Ignore Tape Optimisation jobs");// Optimise Tape Access - true");
			tapeJob = chooseAJobForTheDrive(tapeJobsList, driveStatusDetails);
		}
		
		// FIXME : for now updating the Tapedrive table here...
		// Why because
		// Lets say a library is ingested, so all are 3 copy jobs are pending processing
		// so when copy 1 is picked up for processing on a separate thread even before we call the TapeLibrary Utils and use the drive and update the db,
		// getTheCurrentlyRunningArchiveJobs is being called by the second copy job thus the method not having any tape drive busy and so giving the same tape drive given for copy 1...
		if(tapeJob != null) {
			Tapedrive tapedrive = tapedriveDao.findByElementAddress(driveStatusDetails.getDriveSNo());
			tapedrive.setStatus(TapedriveStatus.BUSY.toString());
			tapedrive.setTapeId(tapeJob.getVolume().getTape().getTapeId());
			tapedrive.setJobId(tapeJob.getJob().getJobId());
			logger.debug("DB Tapedrive Updation " + tapedrive.getStatus() + ":" + tapedrive.getTapeId() + ":" + tapedrive.getJobId());   
			tapedriveDao.save(tapedrive);
			logger.debug("DB Tapedrive Updation - Success");
			
			tapeJob.setDriveNo(driveStatusDetails.getDriveSNo());
			tapeJob.setDeviceWwid(tapedrive.getDeviceWwid());
		}
		return tapeJob;
	}
	
	private List<StorageJob> getIgnoreOptimisationTapeJobsList(List<StorageJob> tapeJobsList){
		logger.trace("getting ignore optimisation list");
		List<StorageJob> ignoreOptimisationTapeJobsList = new ArrayList<StorageJob>();
		for (Iterator<StorageJob> iterator = tapeJobsList.iterator(); iterator.hasNext();) {
			StorageJob nthTapeJob = (StorageJob) iterator.next();
			
			if(!nthTapeJob.isOptimizeTapeAccess()) {
				ignoreOptimisationTapeJobsList.add(nthTapeJob);
			}
		}
		return ignoreOptimisationTapeJobsList;
	}
	
	
	/*
	checks if drive has a tape
	 	yes
			check if any job in the job list need to use the same tape
				yes
					group the job list based on the same tape
					order the job list within a tape 
					choose a job
				no
					goto !~!
		no!~!
			group the job list based on tapes
			order the job list within a tape 
			choose a job

	@returns JobToBeProcessed
	 */
	private StorageJob chooseAJobForTheDrive(List<StorageJob> tapeJobsList, DriveStatusDetails driveStatusDetails){
		logger.debug("choosing a job for the drive " + driveStatusDetails.getDriveSNo());
		StorageJob chosenTapeJob = null;
		String volumeTag = driveStatusDetails.getDte().getVolumeTag();

		if(StringUtils.isNotBlank(volumeTag)) { // means the drive has a tape already loaded
			logger.debug("already has the tape " + volumeTag + " loaded. checking if the tape is needed by any of the jobs");
			for (Iterator<StorageJob> tapeJobsIterator = tapeJobsList.iterator(); tapeJobsIterator.hasNext();) {
				StorageJob tapeJob = (StorageJob) tapeJobsIterator.next();
				Volume toBeUsedVolume = tapeJob.getVolume();
				String toBeUsedVolumeCode = toBeUsedVolume.getTape().getBarcode();
				// checking if the tape is needed by any of the jobs
				if(toBeUsedVolumeCode.equals(volumeTag)) { 
					// if yes group and order the jobs...
					List<StorageJob> groupedAndOrderedJobsOnVolumeTagList = groupAndOrderJobsBasedOnVolumeTag(tapeJobsList, toBeUsedVolumeCode);
					chosenTapeJob = chooseAJob(groupedAndOrderedJobsOnVolumeTagList); // pick a job by checking if another job is not using the tape and also verify if there is not a concurrent overlap
					chosenTapeJob.setDriveAlreadyLoadedWithTape(true);
					return chosenTapeJob;
				}
				// else continue checking with the next job...
			}
		}


		if(chosenTapeJob == null) { // means either Drive not already loaded with a Tape or Tape already loaded but not usable as none of the jobs from the list match the same tape 
			if(StringUtils.isNotBlank(volumeTag))
				logger.debug("none of the jobs from the list match the tape");
			else
				logger.debug("Drive not already loaded with a Tape");
			// TODO : Check if we have to groupAndOrderJobs on Priority and volumetag... 
			List<StorageJob> groupedAndOrderedJobsList = groupAndOrderJobs(tapeJobsList);
			chosenTapeJob = chooseAJob(groupedAndOrderedJobsList);
		}

		return chosenTapeJob;
	}

	/*
  	Picking up a job in an optimised way needs focus on 2 levels
		1) Selection of tape - optmised loading and unloading tapes - 
			Picking a job which has already got its tape loaded in the drive
			grouping of jobs based on tapes and selecting one
		2) optimised winding or seeking position - ordering of jobs within a selected tape

	Step 1 
		groups the jobs based on 
			priority
			volumeTag
	Step 2
		order the jobs 
			if its a read - orders the jobs on read blocks position...
			write - orders on seq code..

	Step 1 
		E.g., to showcase job lists for the following and its consequence
		same priority - different volumes
			1, V5A001
			1, V5A005
		varied priorities - same volume
			1, V5A001
			50, V5A001
		NOTE : priority grouping on volumetag wins

		for eg., for the job list like below
			Job1 = Priority 1, Volume Tag V5A001
			Job2 = Priority 1, Volume Tag V5A005
			Job3 = Priority 1, Volume Tag V5A001
			Job4 = Priority 50, Volume Tag V5A001
			Job5 = Priority 2, Volume Tag V5A003

		THE RESULT would be (note the Job4 with 50 as priority winning over Job5 even though with greater priority 2) 
			Job1 = Priority 1, Volume Tag V5A001
			Job3 = Priority 1, Volume Tag V5A001
			Job4 = Priority 50, Volume Tag V5A001
			Job2 = Priority 1, Volume Tag V5A005
			Job5 = Priority 2, Volume Tag V5A003

	Step 2
		To develop on the above resultset after grouping on priority and volumeTag, we need to order the jobs that use the same volume...
		lets Showcase 
			ordering of jobs within a volumetag
				reads - based on blocks within a tape
				writes - based on seqCode
			and one tape needing both read and write

			Job1 = Priority 1, Volume Tag V5A001, Read, Block 77
			Job3 = Priority 1, Volume Tag V5A001, Read, Block 99
			Job4 = Priority 50, Volume Tag V5A001, Read, Block 11
			Job2 = Priority 1, Volume Tag V5A005, Read, Block 55
			Job5 = Priority 2, Volume Tag V5A003, Read, Block 66
			Job6 = Priority 100, Volume Tag V5A005, Write, 12345_SadhguruOnNationBuilding_Farmers
			Job7 = Priority 100, Volume Tag V5A005, Write, 12346_SadhguruOnNationBuilding_FocusingOnPeople

		THE RESULT would be (note Job4 with block 11 is the frontrunner)
			Job4 = Priority 50, Volume Tag V5A001, Read, Block 11
			Job1 = Priority 1, Volume Tag V5A001, Read, Block 77
			Job3 = Priority 1, Volume Tag V5A001, Read, Block 99
			Job2 = Priority 1, Volume Tag V5A005, Read, Block 55
			Job6 = Priority 100, Volume Tag V5A005, Write, 12345_SadhguruOnNationBuilding_Farmers
			Job7 = Priority 100, Volume Tag V5A005, Write, 12346_SadhguruOnNationBuilding_FocusingOnPeople
			Job5 = Priority 2, Volume Tag V5A003, Read, Block 66
	 */	
	private List<StorageJob> groupAndOrderJobsBasedOnVolumeTag(List<StorageJob> tapeJobsList, String toBeUsedVolumeCode){
		logger.debug("grouping And Ordering Jobs Based On VolumeTag" + toBeUsedVolumeCode);
		List<StorageJob> groupedAndOrderedJobsList = new ArrayList<StorageJob>();

		// To group and order the jobs
		// STEP 1 - Grouping Jobs based on  volumeTags
		GroupedJobsCollection gjc = groupJobsOnVolumeTag(tapeJobsList);
		Map<String, List<StorageJob>> volumeTag_volumeTagGroupedJobs = gjc.getVolumeTag_volumeTagGroupedJobs();
		List<StorageJob> groupedOnVolumeTagJobsList = volumeTag_volumeTagGroupedJobs.get(toBeUsedVolumeCode);
		if(groupedOnVolumeTagJobsList != null) { // if its not already part of other priority
			// STEP 2 - Ordering Jobs within a volume - 
			// say 
			// V5A001 - [Job1, Job3, Job4] becomes [Job4, Job1, Job3]
			// V5A005 - [Job2, Job6, Job7] becomes [Job2, Job6, Job7]
			List<StorageJob> orderedJobsList = orderTheJobsForTheTape(groupedOnVolumeTagJobsList);
			groupedAndOrderedJobsList.addAll(orderedJobsList);
		}
		/* will return 
		Job4 = Priority 50, Volume Tag V5A001, Read, Block 11
		Job1 = Priority 1, Volume Tag V5A001, Read, Block 77
		Job3 = Priority 1, Volume Tag V5A001, Read, Block 99
		 */
		return groupedAndOrderedJobsList;
	}	
	
	private List<StorageJob> groupAndOrderJobs(List<StorageJob> tapeJobsList){
		logger.debug("grouping jobs on tape And Ordering them");
		List<StorageJob> groupedAndOrderedJobsList = new ArrayList<StorageJob>();

		// To group and order the jobs
		// STEP 1 - Grouping Jobs based on priority and volumeTags
		GroupedJobsCollection gjc = groupJobsOnVolumeTag(tapeJobsList);
		Set<Integer> priorityOrder = gjc.getPriorityOrder();
		Map<String, List<StorageJob>> volumeTag_volumeTagGroupedJobs = gjc.getVolumeTag_volumeTagGroupedJobs();

		// Now iterate through on the sorted jobs priority and get the first job matching the priority.  
		// then use the grouped jobs on the volume of the picked job
		// now order the jobs among the volume - so that the job in the order can be used on conditions...
		for (Iterator<Integer> iterator = priorityOrder.iterator(); iterator.hasNext();) {
			Integer priority = (Integer) iterator.next();
			for (Iterator<StorageJob> tapeJobIterator = tapeJobsList.iterator(); tapeJobIterator.hasNext();) {
				StorageJob tapeJob = (StorageJob) tapeJobIterator.next();
				if(tapeJob.getPriority() == priority) {
					String toBeUsedVolumeCode = tapeJob.getVolume().getTape().getBarcode();

					List<StorageJob> groupedOnVolumeTagJobsList = volumeTag_volumeTagGroupedJobs.get(toBeUsedVolumeCode);
					if(groupedOnVolumeTagJobsList != null) { // if its not already part of other priority
						// STEP 2 - Ordering Jobs within a volume - 
						// say 
						// V5A001 - [Job1, Job3, Job4] becomes [Job4, Job1, Job3]
						// V5A005 - [Job2, Job6, Job7] becomes [Job2, Job6, Job7]
						List<StorageJob> orderedJobsList = orderTheJobsForTheTape(groupedOnVolumeTagJobsList);
						groupedAndOrderedJobsList.addAll(orderedJobsList);
						volumeTag_volumeTagGroupedJobs.remove(toBeUsedVolumeCode);
					}
				}
			}
		}
		/* will return 
				Job4 = Priority 50, Volume Tag V5A001, Read, Block 11
			Job1 = Priority 1, Volume Tag V5A001, Read, Block 77
			Job3 = Priority 1, Volume Tag V5A001, Read, Block 99
			Job2 = Priority 1, Volume Tag V5A005, Read, Block 55
			Job6 = Priority 100, Volume Tag V5A005, Write, 12345_SadhguruOnNationBuilding_Farmers
			Job7 = Priority 100, Volume Tag V5A005, Write, 12346_SadhguruOnNationBuilding_FocusingOnPeople
			Job5 = Priority 2, Volume Tag V5A003, Read, Block 66
		 */
		return groupedAndOrderedJobsList;
	}


	private GroupedJobsCollection groupJobsOnVolumeTag(List<StorageJob> tapeJobsList){
		logger.debug("grouping the Jobs related to tapes");
		Set<Integer> priorityOrder = new TreeSet<Integer>();
		/*
		 * 
		 * After executing the below snippet volumeTag_volumeTagGroupedJobs map will hold something like the below key value pairs
			V5A001 - [Job1, Job3, Job4]
		V5A005 - [Job2, Job6, Job7]
		V5A003 - [Job5]
		 */
		Map<String, List<StorageJob>> volumeTag_volumeTagGroupedJobs = new HashMap<String, List<StorageJob>>();
		for (Iterator<StorageJob> iterator = tapeJobsList.iterator(); iterator.hasNext();) {
			StorageJob tapeJob = (StorageJob) iterator.next();
			int priority = tapeJob.getPriority();
			priorityOrder.add(priority); // TODO test with priority zero

			// STEP 1a - Grouping Jobs based on volumeTags
			String toBeUsedVolumeCode = tapeJob.getVolume().getTape().getBarcode();
			if(volumeTag_volumeTagGroupedJobs.containsKey(toBeUsedVolumeCode)) { // if map already contains volume just append the job to the list for the volume
				List<StorageJob> groupedOnVolumeTagJobsList = volumeTag_volumeTagGroupedJobs.get(toBeUsedVolumeCode);
				groupedOnVolumeTagJobsList.add(tapeJob);
				volumeTag_volumeTagGroupedJobs.put(toBeUsedVolumeCode, groupedOnVolumeTagJobsList);
			}
			else { // if map doesnt contain the volume add the volume to the map
				List<StorageJob> groupedOnVolumeTagJobsList = new ArrayList<StorageJob>();
				groupedOnVolumeTagJobsList.add(tapeJob);
				volumeTag_volumeTagGroupedJobs.put(toBeUsedVolumeCode, groupedOnVolumeTagJobsList);
			}
		}	

		GroupedJobsCollection gjc = new GroupedJobsCollection();
		gjc.setPriorityOrder(priorityOrder);
		gjc.setVolumeTag_volumeTagGroupedJobs(volumeTag_volumeTagGroupedJobs);

		return gjc;
	}
	
	/*
 	Orders the list of jobs waiting to be executed on the candidate tape

	read jobs had to be ordered based on blocks
	write jobs need to be ordered based on sequence id of the library names
	 */
	private List<StorageJob> orderTheJobsForTheTape(List<StorageJob> groupedOnVolumeTagJobsList) {
		logger.debug("ordering the Jobs within tapes");
		List<StorageJob> orderedJobsList = new ArrayList<StorageJob>();

		Map<Integer, List<StorageJob>> storageOperationId_storageOperationIdGroupedJobs = new HashMap<Integer, List<StorageJob>>();

		// First group all read and write jobs...
		// say for V5A001 - readjobs = [Job1, Job3, Job4]
		// and for V5A005 - readjobs = [Job2] and writejobs = [Job6, Job7]
		for (Iterator<StorageJob> iterator = groupedOnVolumeTagJobsList.iterator(); iterator.hasNext();) {
			StorageJob tapeJob = (StorageJob) iterator.next();
			int storageOperationId = tapeJob.getStorageOperation().getStorageOperationId();
			if(storageOperationId_storageOperationIdGroupedJobs.containsKey(storageOperationId)) {
				List<StorageJob> groupedOnArchiveFunctionJobsList = storageOperationId_storageOperationIdGroupedJobs.get(storageOperationId);
				groupedOnArchiveFunctionJobsList.add(tapeJob);
				storageOperationId_storageOperationIdGroupedJobs.put(storageOperationId, groupedOnArchiveFunctionJobsList);
			}
			else {
				List<StorageJob> groupedOnArchiveFunctionJobsList = new ArrayList<StorageJob>();
				groupedOnArchiveFunctionJobsList.add(tapeJob);
				storageOperationId_storageOperationIdGroupedJobs.put(storageOperationId, groupedOnArchiveFunctionJobsList);
			}			
		}


		Set<Integer> storageOperationIdGroupedJobsKeySet = storageOperationId_storageOperationIdGroupedJobs.keySet();
		for (Iterator<Integer> iterator = storageOperationIdGroupedJobsKeySet.iterator(); iterator.hasNext();) {
			Integer storageOperationId = (Integer) iterator.next();
			// READ/RESTORE - ordered based on seqBlocks
			// V5A001 - readjobs = [Job1, Job3, Job4] becomes [Job4, Job1, Job3]
			if(storageOperationId == StorageOperation.READ.getStorageOperationId()) {
				List<StorageJob> readJobs = storageOperationId_storageOperationIdGroupedJobs.get(storageOperationId);
				Set<Integer> seqBlockOrderSortedSet = new TreeSet<Integer>();
				// In a single block there could be multiple files so need the List<StorageJob>...
				Map<Integer, List<StorageJob>> seqBlock_seqBlockGroupedJobs = new HashMap<Integer, List<StorageJob>>();
				Map<Integer, StorageJob> seqOffSet_ArchiveJob = new HashMap<Integer, StorageJob>();
				for (Iterator<StorageJob> iterator2 = readJobs.iterator(); iterator2.hasNext();) {
					StorageJob tapeJob = (StorageJob) iterator2.next();
					
					int seqBlock = tapeJob.getBlock();
					// TODO - Assumes the seqBlock is from the start of the Tape and not the start of the archive...
					// just ordering by block should be good enough - long seqOffset = fileArchive.getSeqOffset();
					seqBlockOrderSortedSet.add(seqBlock);
					if(seqBlock_seqBlockGroupedJobs.containsKey(seqBlock)) {
						List<StorageJob> groupedOnSeqBlockJobsList = seqBlock_seqBlockGroupedJobs.get(seqBlock);
						groupedOnSeqBlockJobsList.add(tapeJob);
						seqBlock_seqBlockGroupedJobs.put(seqBlock, groupedOnSeqBlockJobsList);
					}
					else {
						List<StorageJob> groupedOnSeqBlockJobsList = new ArrayList<StorageJob>();
						groupedOnSeqBlockJobsList.add(tapeJob);
						seqBlock_seqBlockGroupedJobs.put(seqBlock, groupedOnSeqBlockJobsList);
					}
				}
				// running through each seq Block in the order and adding their respective jobs to the orderedJob collection
				for (Iterator<Integer> iterator2 = seqBlockOrderSortedSet.iterator(); iterator2.hasNext();) {
					Integer seqBlock = (Integer) iterator2.next();
					List<StorageJob> seqBlockGroupedJobsList = seqBlock_seqBlockGroupedJobs.get(seqBlock);
					orderedJobsList.addAll(seqBlockGroupedJobsList);
				}
			}
			else if(storageOperationId == StorageOperation.WRITE.getStorageOperationId()) { // WRITE/INGEST - ordered based on seqId of the library...
				// V5A005 - readjobs = [Job2] and writejobs = [Job6, Job7], retains the same order
				List<StorageJob> writeJobs = storageOperationId_storageOperationIdGroupedJobs.get(storageOperationId);
				Set<String> libraryNameOrderedSortedSet = new TreeSet<String>();
				Map<String, StorageJob> libraryName_TapeJob = new HashMap<String, StorageJob>();

				for (Iterator<StorageJob> iterator2 = writeJobs.iterator(); iterator2.hasNext();) {
					StorageJob tapeJob = (StorageJob) iterator2.next();
					Library library = libraryDao.findById(tapeJob.getLibraryId()).get();
					String libraryName = library.getName();
					libraryNameOrderedSortedSet.add(libraryName);
					libraryName_TapeJob.put(libraryName, tapeJob);
				}
				// now running through each library Name in the order and adding their respective jobs to the orderedJob collection
				for (Iterator<String> iterator2 = libraryNameOrderedSortedSet.iterator(); iterator2.hasNext();) {
					String libraryName = (String) iterator2.next();
					StorageJob tapeJob = libraryName_TapeJob.get(libraryName);
					orderedJobsList.add(tapeJob);
				}				
			}
		}
		return orderedJobsList;
	}


	/*
	chooses a job by ensuring
		the candidate job is not needing a tape thats already been used by another running job
		the candidate job is not overlapping on concurrent writes with an already running job

		Define non concurrent writes
			1) say libraryclass pub-video is already writing copy 1(it could be any of the non-finalised tapes and need not be a similar copy. For simplicity we will assume its one of similar copy tape)
			2) then jobs for libraryclass pub-video to write copy 2 and 3 should not be allowed... 

	 */
	private StorageJob chooseAJob(List<StorageJob> tapeJobsList) {
		logger.trace("choosing a job");
		StorageJob chosenJob = null;
		List<StorageJob> currentlyRunningTapeJobsList = getTheCurrentlyRunningTapeJobs();
		List<StorageJob> currentlyRunningWriteJobsList = new ArrayList<StorageJob>();
		if(currentlyRunningTapeJobsList == null || currentlyRunningTapeJobsList.size() == 0) {
			// no jobs currently running and hence need not worry about concurrency checks or same tape being used in another job
			logger.trace("no jobs currently running. returning the first job");
			return tapeJobsList.get(0); // return the first job
		}
		else {
			// means there are jobs running...
			// check if there are any writing jobs currently ON...
			logger.trace("there are jobs running. checking if any writing jobs ON");
			boolean isWriteJobsOn = false;

			GroupedJobsCollection gjc = groupJobsOnVolumeTag(tapeJobsList);
			Map<String, List<StorageJob>> volumeTag_volumeTagGroupedJobs = gjc.getVolumeTag_volumeTagGroupedJobs();

			// checking if there are jobs that all need to use the same tape as a currently running job... if yes remove them from the candidate list...
			for (Iterator<StorageJob> iterator = currentlyRunningTapeJobsList.iterator(); iterator.hasNext();) {
				StorageJob runningTapeJob = (StorageJob) iterator.next();
				String alreadyInUseTapeBarCode = runningTapeJob.getVolume().getTape().getBarcode();
				logger.trace("tape " + alreadyInUseTapeBarCode + " is already in use by " + runningTapeJob.getJob().getJobId() + ". checking if any other jobs in the list use the same tape and so can be removed from processing.");
				int storageOperationId= runningTapeJob.getStorageOperation().getStorageOperationId();
				if(storageOperationId == StorageOperation.WRITE.getStorageOperationId()) {
					isWriteJobsOn = true;
					currentlyRunningWriteJobsList.add(runningTapeJob);
				}
				
				// The jobs needing a tape thats already been used by another running job are removed as we cant run 2 jobs on a same tape at a time....
				if(volumeTag_volumeTagGroupedJobs.containsKey(alreadyInUseTapeBarCode)) {
					List<StorageJob> toBeIgnoredVolumeTagList = volumeTag_volumeTagGroupedJobs.get(alreadyInUseTapeBarCode);
					// Iterating just for the log statement for greater visibility on whats going on...
					for (Iterator<StorageJob> iterator2 = toBeIgnoredVolumeTagList.iterator(); iterator2.hasNext();) {
						StorageJob storageJob = (StorageJob) iterator2.next();
						logger.trace(storageJob.getJob().getJobId() + " - same tape job. so ignoring it");
					}
					volumeTag_volumeTagGroupedJobs.remove(alreadyInUseTapeBarCode);
					tapeJobsList.removeAll(toBeIgnoredVolumeTagList);
				}
				else {
					// do nothing
				}
			}

			// Should we check the nonconcurrentwrites only against running ***write*** jobs and not ***read*** jobs??? 
			// In other words if libraryclass pub-video is already ***reading*** from a volume on copy 1 - should we allow libraryclass pub-video to write copy 2 or not...
			// if power goes off will it only affect the tapes that are writing or will it affect even if its a read operation...
			// ANS: 2nd Feb 2020 - Swami says the read jobs wouldnt affect the tape, and so is ok to skip nonconcurrentwrites checks for reads...
			if(isWriteJobsOn) {
				logger.trace("isWriteJobsOn - " + isWriteJobsOn);
				List<StorageJob> revisedTapeJobsList = new ArrayList<StorageJob>();
				revisedTapeJobsList.addAll(tapeJobsList);
				// NOTE that archiveJobsList now contains entries after removing the jobs that all need to use the same tape as a currently running job...
				for (Iterator<StorageJob> iterator = tapeJobsList.iterator(); iterator.hasNext();) {
					StorageJob tapeJob = (StorageJob) iterator.next();
					if(!revisedTapeJobsList.contains(tapeJob)) {
						continue;
					}
					logger.trace("checking candidacy for - " + tapeJob.getJob().getJobId());
					int storageOperationId = tapeJob.getStorageOperation().getStorageOperationId();
					if(storageOperationId == StorageOperation.WRITE.getStorageOperationId()) { // only for writes we need to check on the concurrent overlapping 
						// check overlapping on concurrent writes...
						if(!tapeJob.isConcurrentCopies()) { // if concurrent copy on the job is not allowed
							logger.trace("concurrent copy on this tape job not allowed. checking if any already running write jobs overlap concurrency with this...");
							Copy tapeJobCopy = copyDao.findByTaskId(tapeJob.getJob().getTaskId());
							int tapeJobLibraryClassId = tapeJobCopy.getLibraryclassId();
							
							boolean isOverlappingConcurrency = false;
							for (Iterator<StorageJob> currentlyRunningWriteJobsIterator = currentlyRunningWriteJobsList.iterator(); currentlyRunningWriteJobsIterator.hasNext();) {
								StorageJob currentlyRunningWriteJob = (StorageJob) currentlyRunningWriteJobsIterator.next();
								Job crwj = currentlyRunningWriteJob.getJob();
								
								/* TODO @Swami
								 * same request or different request, if the 3 copies are loaded and power goes off then all 3 could go bad
								 * means at no point in time we should have all the 3 copies loaded at the same time in drives?
								 * So commenting out the below...
								if(tapeJob.getJob().getRequestId() != crwj.getRequestId()) // if the jobs are for different requests then no need to check concurrent writes. Really?
									return tapeJob;
								*/
								
								Copy crwjCopy = copyDao.findByTaskId(crwj.getTaskId());
								int crwjLibraryClassId = crwjCopy.getLibraryclassId();
								//							int crij_poolId = crij_copy.getPoolId();
								//							int crij_copyNumber = crij_copy.getCopyNumber();

								// TODO What happens if 2 libraryclasses share same set of pools... The check ideally should be on the copy tapes involved... for eg., if we have V5A001 then we shouldnt have V5B001 and V5C001  
								if(crwjLibraryClassId != tapeJobLibraryClassId) { // only if libraryclasses are different. if they are same we should not allow concurrent writes...
									logger.trace("running write job " + crwj.getJobId() + " is not a concurrent copy to " + tapeJob.getJob().getJobId());
									
								}else {
									logger.trace("running write job " + crwj.getJobId() + " is a concurrent copy to " + tapeJob.getJob().getJobId() + ". So skipping " + tapeJob.getJob().getJobId() + " from processing and removing it from list so its not checked again");
									isOverlappingConcurrency = true;
									revisedTapeJobsList.remove(tapeJob);
									break;
								}
							}
							if(!isOverlappingConcurrency) {
								logger.trace(tapeJob.getJob().getJobId() + " has not got a concurrent copy in writing. So selecting it");
								return tapeJob;
							}
						}
						else {
							logger.trace("concurrent copy on the job allowed. So selecting it");
							// nothing to worry
							return tapeJob; // return the current job as its a non concurrent write job...
						}
					}
				}
			}else {
				return tapeJobsList.get(0); // return the first job
			}
		}
		return chosenJob;
	}

	private List<StorageJob> getTheCurrentlyRunningTapeJobs(){
		List<StorageJob> currentlyRunningTapeJobsList = new ArrayList<StorageJob>();
		List<Tapedrive> tapedriveList = tapedriveDao.findAllByStatus("Busy".toUpperCase());
		for (Iterator<Tapedrive> iterator = tapedriveList.iterator(); iterator.hasNext();) {
			Tapedrive tapedrive = (Tapedrive) iterator.next();

			StorageJob tapeJob = new StorageJob();

			int jobId = tapedrive.getJobId();
			logger.trace(jobId + " currently running in " + tapedrive.getElementAddress());
			Job job = jobDao.findById(jobId).get();
			tapeJob.setJob(job);

			int subrequestId = job.getSubrequestId();
			Subrequest subrequest = subrequestDao.findById(subrequestId).get(); // TODO : Cache the call...
			Request request = requestDao.findById(subrequest.getRequestId()).get(); // TODO : Cache the call...
			int requestTypeId = request.getRequesttypeId();
			Requesttype requesttype = requesttypeDao.findById(requestTypeId).get();
			if(requesttype.getName().equals("INGEST")) {
				tapeJob.setStorageOperation(StorageOperation.WRITE);
				logger.trace("write job");
			}else if (requesttype.getName().equals("RESTORE")) {
				tapeJob.setStorageOperation(StorageOperation.READ);
				logger.trace("read job");
			}

			int tapeId = tapedrive.getTapeId();
			Tape tape = tapeDao.findById(tapeId).get();
			Volume volume = new Volume();
			volume.setTape(tape);
			tapeJob.setVolume(volume);

			currentlyRunningTapeJobsList.add(tapeJob);
		}
		return currentlyRunningTapeJobsList;
	}

}
