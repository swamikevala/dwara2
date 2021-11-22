package org.ishafoundation.dwaraapi.storage.storagetype.tape.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.transactional.TActivedeviceDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Priority;
import org.ishafoundation.dwaraapi.storage.model.GroupedJobsCollection;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagesubtype.AbstractStoragesubtype;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeDeviceUtil;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.utils.VolumeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeJobSelector {
	
	private static final Logger logger = LoggerFactory.getLogger(TapeJobSelector.class);
	
	private static final Pattern SEQUENCE_NUMBER_PATTERN = Pattern.compile("[A-Z]+(\\d+)");
	
	@Autowired
	private TActivedeviceDao tActivedeviceDao;
	
	@Autowired
	private TapeDeviceUtil tapeDeviceUtil;	

	@Autowired
	private VolumeUtil volumeUtil;
	
	@Autowired
	private Map<String, AbstractStoragesubtype> storagesubtypeMap;
	
	/**
	 * Method responsible for getting a job for the drive
	 * 
	 * First check if there are ignoreVolumeOptimisation Jobs and choose a job
	 * else choose a job from the exhaustive list 
	 * 
	 * @param tapeJobsList - the jobs list against which the selection happens
	 * @param driveDetails - for which drive the job is getting selected
	 * @return
	 * @throws Exception 
	 */
	
	public StorageJob selectJob(List<StorageJob> tapeJobsList, DriveDetails driveDetails) throws Exception {
		if(tapeJobsList.size() <= 0)
			return null;
		StorageJob tapeJob = null;
		
		List<StorageJob> ignoreOptimisationTapeJobsList = getIgnoreOptimisationTapeJobsList(tapeJobsList);
		
		if(ignoreOptimisationTapeJobsList.size() > 0) { // are there IgnoreVolumeOptimisation jobs
			logger.debug("Contains Ignore Tape Optimisation jobs");
			// if yes then get a job that is optimised - Yes we need optimisation even on the ignoreVolumeOptimisation job list...
			tapeJob = chooseAJobForTheDrive(ignoreOptimisationTapeJobsList, driveDetails);
		} else {
			logger.debug("Doesnt contain any Ignore Tape Optimisation jobs");// Optimise Tape Access - true");*/
			tapeJob = chooseAJobForTheDrive(tapeJobsList, driveDetails);
		}
		
		// checking the selected Job's volume capacity against the artifact size
		if(tapeJob != null && tapeJob.getJob().getStoragetaskActionId() == Action.write) {
			// Scheduler selects a volume for a job 
			// 2020-09-25 20:24:03,437 TRACE org.ishafoundation.dwaraapi.storage.storagetype.StoragetypeJobDelegator [scheduling-1] building storage job - 413:Write
			// 2020-09-25 20:24:03,439 TRACE org.ishafoundation.dwaraapi.utils.VolumeUtil [scheduling-1] R20002L7 has enough space. Selecting it
			// .
			// .
			// 2020-09-25 20:24:03,509 DEBUG org.ishafoundation.dwaraapi.storage.storagetype.StoragetypeJobDelegator [scheduling-1] Cleared existing IStoragetypeThreadPoolExecutor queue item and added the fresh storage job list...
			// 2020-09-25 20:24:03,509 DEBUG org.ishafoundation.dwaraapi.storage.storagetype.StoragetypeJobDelegator [scheduling-1] Delegating to tapeJobManager's separate thread ================
			
			// But a running marks the tape as suspect
			// 2020-09-25 20:24:32,292 INFO org.ishafoundation.dwaraapi.storage.storagetype.thread.AbstractStoragetypeJobManager [pool-2-thread-4~!~sr-60-job-401] Marked the volume R20002L7 as suspect
			
			// In the next schedule
			// 2020-09-25 20:25:03,554 TRACE org.ishafoundation.dwaraapi.storage.storagetype.StoragetypeJobDelegator [scheduling-1] building storage job - 413:Write
			// 2020-09-25 20:25:03,556 WARN org.ishafoundation.dwaraapi.utils.VolumeUtil [scheduling-1] R2 hasnt got enough capacity. Bump it
			
			// And if no storagejob to be processed at all, "Cleared existing IStoragetypeThreadPoolExecutor queue" wont happen..
			// This happens when there is no extra tape in the pool and so no storagejob added by scheduler for the next steps...
			if(!volumeUtil.isPhysicalVolumeGood(tapeJob.getVolume().getId())) {
				logger.debug("Selected job " + tapeJob.getJob().getId() + " volume " + tapeJob.getVolume().getId() + " is flagged as suspect. Removing it and other same volume jobs from the list so they all can be picked up in next schedule. Now re-selecting a job again");
				
				GroupedJobsCollection gjc = groupJobsBasedOnVolumeTag(tapeJobsList);
				Map<String, List<StorageJob>> volumeTag_volumeTagGroupedJobs = gjc.getVolumeTag_volumeTagGroupedJobs();
				List<StorageJob> toBeIgnoredVolumeTagList = volumeTag_volumeTagGroupedJobs.get(tapeJob.getVolume().getId());
				tapeJobsList.removeAll(toBeIgnoredVolumeTagList);
				tapeJob = selectJob(tapeJobsList, driveDetails);
			}
			
			long projectedArtifactSize = volumeUtil.getProjectedArtifactSize(tapeJob.getArtifactSize(), tapeJob.getVolume());
			if(volumeUtil.getVolumeUnusedCapacity(tapeJob.getVolume()) <= projectedArtifactSize) {
				logger.debug("Selected job " + tapeJob.getJob().getId() + " volume " + tapeJob.getVolume().getId() + " doesnt have enough capacity to hold the artifact. Removing it from the list so it can be picked up in next schedule. Now re-selecting a job again");
				tapeJobsList.remove(tapeJob);
				tapeJob = selectJob(tapeJobsList, driveDetails);
			}
		}
		
		return tapeJob;
	}

	
	private List<StorageJob> getIgnoreOptimisationTapeJobsList(List<StorageJob> tapeJobsList){
		logger.trace("Getting ignore optimisation list");
		List<StorageJob> ignoreOptimisationTapeJobsList = new ArrayList<StorageJob>();
		for (Iterator<StorageJob> iterator = tapeJobsList.iterator(); iterator.hasNext();) {
			StorageJob nthTapeJob = (StorageJob) iterator.next();
			if(nthTapeJob.getPriority() == Priority.critical.getPriorityValue())
				ignoreOptimisationTapeJobsList.add(nthTapeJob);
//			TODO : Commented out
//			if(!nthTapeJob.isOptimizeTapeAccess()) {
//				ignoreOptimisationTapeJobsList.add(nthTapeJob);
//			}
		}
		return ignoreOptimisationTapeJobsList;
	}
	
	
	/*
	checks if drive has a tape
	 	1 yes
			a check if any job in the job list need to use the same tape
				i	yes
					*	group the job list based on the same tape
					*	order the job list within a tape 
					*	choose a job
				ii	no
						goto !~!
		2 no !~!
			group the job list based on tapes
			order the job list within a tape 
			choose a job

	@returns JobToBeProcessed
	 */
	private StorageJob chooseAJobForTheDrive(List<StorageJob> tapeJobsList, DriveDetails driveDetails) throws Exception{
		logger.debug("Choosing a job for the drive " + driveDetails.getDriveId());//driveDetails.getDriveName() + "(" + driveDetails.getDte().getsNo() + ")");
		// TODO :  Filtering job which involves a tape' generation not supported by the Drive
		String driveStoragesubtype = driveDetails.getDriveStoragesubtype();
		int[] driveSupportedGenerationsOnWrite = storagesubtypeMap.get(driveStoragesubtype).getWriteSupportedGenerations();
		int[] driveSupportedGenerationsOnRead = storagesubtypeMap.get(driveStoragesubtype).getReadSupportedGenerations();
		List<StorageJob> tapeJobsListNotSupportedForThisDrive = new ArrayList<StorageJob>(); 
		for (Iterator<StorageJob> tapeJobsIterator = tapeJobsList.iterator(); tapeJobsIterator.hasNext();) {
			StorageJob tapeJob = (StorageJob) tapeJobsIterator.next();
			Volume toBeUsedVolume = tapeJob.getVolume();
			String volumeStoragesubtype = toBeUsedVolume.getStoragesubtype();
			Action jobStorageAction = tapeJob.getJob().getStoragetaskActionId();
			// check if the action on volume is supported by the drive
			int volumeGeneration = storagesubtypeMap.get(volumeStoragesubtype).getGeneration();
			boolean volumeSupportedByDrive = false;
			int[] supportedGenerations;
			if(jobStorageAction == Action.write) {
				supportedGenerations = driveSupportedGenerationsOnWrite;
			}
			else {
				supportedGenerations = driveSupportedGenerationsOnRead;
			}
			
			for (int i : supportedGenerations) {
				if(i == volumeGeneration) {
					volumeSupportedByDrive = true;
					break;
				}
			}
			if(!volumeSupportedByDrive) {
				logger.debug(tapeJob.getJob().getId() + ":" + toBeUsedVolume + ":" + volumeGeneration + " not supported in this drive. Will be removing it from the job list");
				tapeJobsListNotSupportedForThisDrive.add(tapeJob);
			}
		}
		tapeJobsList.removeAll(tapeJobsListNotSupportedForThisDrive);

		// tapedrive and tape linked via device.details.type(LTO7) and volume.details.storagesubtype(LTO7)...
//			driveDetails.getDriveId();
		
		StorageJob chosenTapeJob = null;
		String volumeTag = driveDetails.getDte().getVolumeTag();

		if(StringUtils.isNotBlank(volumeTag)) { // means the drive has a tape already loaded
			logger.debug("Drive " + driveDetails.getDriveId() + " already has the tape " + volumeTag + " loaded. Checking if the tape is needed by any of the queued jobs");
			for (Iterator<StorageJob> tapeJobsIterator = tapeJobsList.iterator(); tapeJobsIterator.hasNext();) {
				StorageJob tapeJob = (StorageJob) tapeJobsIterator.next();
				Volume toBeUsedVolume = tapeJob.getVolume();
				String toBeUsedVolumeCode = toBeUsedVolume.getId();
				// checking if the tape is needed by any of the jobs
				if(toBeUsedVolumeCode.equals(volumeTag)) { 
					logger.debug("Jobs in the list match the tape " + volumeTag);
					// if yes group and order the jobs...
					List<StorageJob> groupedAndOrderedJobsOnVolumeTagList = groupAndOrderJobsBasedOnVolumeTag(tapeJobsList, toBeUsedVolumeCode); // returns only the tape specific jobs
					// pick a job by checking if another job is not using the tape and also verify if there is not a concurrent overlap
					chosenTapeJob = chooseAJob(groupedAndOrderedJobsOnVolumeTagList, false, null); // needVerificationAgainstOtherDrives = false - as only the drive specific tape related jobs are in the list and hence job's tape needs no verification against other drives...
					//chosenTapeJob.setDriveAlreadyLoadedWithTape(true);
					logger.debug("Removing the same tape jobs from the list");
					tapeJobsList.removeAll(groupedAndOrderedJobsOnVolumeTagList); // removing all same tape specific jobs...
					return chosenTapeJob;
				}
				// else continue checking with the next job...
			}
		}


		if(chosenTapeJob == null) { // means either Drive not already loaded with a Tape or Tape already loaded but not usable as none of the jobs from the list match the same tape 
			if(StringUtils.isNotBlank(volumeTag))
				logger.debug("None of the jobs from the list match the tape");
			else
				logger.debug("Drive not already loaded with a Tape");
			// TODO : Check if we have to groupAndOrderJobs on Priority and volumetag... 
			List<StorageJob> groupedAndOrderedJobsList = groupAndOrderJobs(tapeJobsList);
			// NOTE : It might be tempting to move this out as for every tapejobselection call(which is per drive), we are getting the drivedetails - but is needed so we get the latest correct status
			List<DriveDetails> allDrivesList = tapeDeviceUtil.getAllDrivesDetails();
			chosenTapeJob = chooseAJob(groupedAndOrderedJobsList, true, allDrivesList);
			
			logger.debug("Updating the original job list");
			tapeJobsList = groupedAndOrderedJobsList; // removing all same tape specific jobs...

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
			write/verify - orders on seq code..

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
				writes/verify - based on seqCode
			and one tape needing both read and write

			Job1 = Priority 1, Volume Tag V5A001, Read, Block 77
			Job3 = Priority 1, Volume Tag V5A001, Read, Block 99
			Job4 = Priority 50, Volume Tag V5A001, Read, Block 11
			Job2 = Priority 1, Volume Tag V5A005, Read, Block 55
			Job5 = Priority 2, Volume Tag V5A003, Read, Block 66
			Job6 = Priority 100, Volume Tag V5A005, Write, 12345_SadhguruOnNationBuilding_Farmers
			Job7 = Priority 100, Volume Tag V5A005, Write, 12346_SadhguruOnNationBuilding_FocusingOnPeople
			Job8 = Priority 100, Volume Tag V5A005, Verify, 12344_SadhguruOnNationBuilding_SecuringTeritorialIntegrity

		THE RESULT would be (note Job4 with block 11 is the frontrunner)
			Job4 = Priority 50, Volume Tag V5A001, Read, Block 11
			Job1 = Priority 1, Volume Tag V5A001, Read, Block 77
			Job3 = Priority 1, Volume Tag V5A001, Read, Block 99
			Job2 = Priority 1, Volume Tag V5A005, Read, Block 55
			Job8 = Priority 100, Volume Tag V5A005, Verify, 12344_SadhguruOnNationBuilding_SecuringTeritorialIntegrity
			Job6 = Priority 100, Volume Tag V5A005, Write, 12345_SadhguruOnNationBuilding_Farmers
			Job7 = Priority 100, Volume Tag V5A005, Write, 12346_SadhguruOnNationBuilding_FocusingOnPeople
			Job5 = Priority 2, Volume Tag V5A003, Read, Block 66
	 */	
	private List<StorageJob> groupAndOrderJobsBasedOnVolumeTag(List<StorageJob> tapeJobsList, String toBeUsedVolumeCode){
		logger.debug("Grouping And Ordering Jobs Based On VolumeTag " + toBeUsedVolumeCode);
		List<StorageJob> groupedAndOrderedJobsList = new ArrayList<StorageJob>();

		// To group and order the jobs
		// STEP 1 - Grouping Jobs based on  volumeTags
		GroupedJobsCollection gjc = groupJobsBasedOnVolumeTag(tapeJobsList);
		Map<String, List<StorageJob>> volumeTag_volumeTagGroupedJobs = gjc.getVolumeTag_volumeTagGroupedJobs();
		List<StorageJob> groupedOnVolumeTagJobsList = volumeTag_volumeTagGroupedJobs.get(toBeUsedVolumeCode);
		if(groupedOnVolumeTagJobsList != null) { // if its not already part of other priority
			// STEP 2 - Ordering Jobs within a volume - 
			// say 
			// V5A001 - [Job1, Job3, Job4] becomes [Job4, Job1, Job3]
			// V5A005 - [Job2, Job6, Job7] becomes [Job2, Job6, Job7]
			List<StorageJob> orderedJobsList = orderJobsWithinATape(groupedOnVolumeTagJobsList);
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
		logger.debug("Grouping jobs on tape And Ordering them");
		List<StorageJob> groupedAndOrderedJobsList = new ArrayList<StorageJob>();

		// To group and order the jobs
		// STEP 1 - Grouping Jobs based on priority and volumeTags
		GroupedJobsCollection gjc = groupJobsBasedOnVolumeTag(tapeJobsList);
		Set<Integer> priorityOrder = gjc.getPriorityOrder();
		Map<String, List<StorageJob>> volumeTag_volumeTagGroupedJobs = gjc.getVolumeTag_volumeTagGroupedJobs();
//
//		Set<Integer> priorityOrder = new TreeSet<Integer>();
//		
//	    for (Priority nthPriority : Priority.values()) {
//	    	nthPriority.getPriorityValue()
//	    	
//	        if (ct.javaStyleStoragesubtype.equals(javaStyleStoragesubtype)) {
//	        	storagesubtype = ct;
//	        	break;
//	        }
//	    }

		
		
		// Now iterate through on the sorted jobs priority and get the first job matching the priority.  
		// then use the grouped jobs on the volume of the picked job
		// now order the jobs among the volume - so that the job in the order can be used on conditions...
		for (Iterator<Integer> iterator = priorityOrder.iterator(); iterator.hasNext();) {
			Integer priority = (Integer) iterator.next();
			for (Iterator<StorageJob> tapeJobIterator = tapeJobsList.iterator(); tapeJobIterator.hasNext();) {
				StorageJob tapeJob = (StorageJob) tapeJobIterator.next();
				if(tapeJob.getPriority() == priority) {
					String toBeUsedVolumeCode = tapeJob.getVolume().getId();

					List<StorageJob> groupedOnVolumeTagJobsList = volumeTag_volumeTagGroupedJobs.get(toBeUsedVolumeCode);
					if(groupedOnVolumeTagJobsList != null) { // if its not already part of other priority
						// STEP 2 - Ordering Jobs within a volume - 
						// say 
						// V5A001 - [Job1, Job3, Job4] becomes [Job4, Job1, Job3]
						// V5A005 - [Job2, Job6, Job7] becomes [Job2, Job6, Job7]
						List<StorageJob> orderedJobsList = orderJobsWithinATape(groupedOnVolumeTagJobsList);
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


	private GroupedJobsCollection groupJobsBasedOnVolumeTag(List<StorageJob> tapeJobsList){
		logger.debug("Grouping the jobs based on volume tag");
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
			String toBeUsedVolumeCode = tapeJob.getVolume().getId();
			
			List<StorageJob> groupedOnVolumeTagJobsList = volumeTag_volumeTagGroupedJobs.get(toBeUsedVolumeCode);
			if(groupedOnVolumeTagJobsList == null) {
				groupedOnVolumeTagJobsList = new ArrayList<StorageJob>();
				volumeTag_volumeTagGroupedJobs.put(toBeUsedVolumeCode, groupedOnVolumeTagJobsList);	
			}
			groupedOnVolumeTagJobsList.add(tapeJob);
		}	

		GroupedJobsCollection gjc = new GroupedJobsCollection();
		gjc.setPriorityOrder(priorityOrder);
		gjc.setVolumeTag_volumeTagGroupedJobs(volumeTag_volumeTagGroupedJobs);

		return gjc;
	}
	
	/*
	 	Orders/Sorts the list of jobs waiting to be executed on the candidate tape
	
		read jobs had to be ordered based on blocks
		write jobs need to be ordered based on sequence id of the artifact names
	*/
	private List<StorageJob> orderJobsWithinATape(List<StorageJob> groupedOnVolumeTagJobsList) {
		logger.debug("Ordering the jobs");
		List<StorageJob> orderedJobsList = new ArrayList<StorageJob>();

		Map<String, List<StorageJob>> storagetaskAction_storagetaskActionGroupedJobs = new HashMap<String, List<StorageJob>>();

		logger.trace("First step - Grouping the jobs based on storagetaskaction");
		// First group all read and write jobs...
		// say for V5A001 - readjobs = [Job1, Job3, Job4]
		// and for V5A005 - readjobs = [Job2] and verifyjobs = [Job5] and writejobs = [Job6, Job7]
		for (Iterator<StorageJob> iterator = groupedOnVolumeTagJobsList.iterator(); iterator.hasNext();) {
			StorageJob tapeJob = (StorageJob) iterator.next();
			Action action = tapeJob.getJob().getStoragetaskActionId();
			String storagetaskAction = marshallKey(action);
			logger.trace(storagetaskAction + tapeJob.getJob().getId());
			if(storagetaskAction_storagetaskActionGroupedJobs.containsKey(storagetaskAction)) {
				List<StorageJob> groupedOnActionJobsList = storagetaskAction_storagetaskActionGroupedJobs.get(storagetaskAction);
				groupedOnActionJobsList.add(tapeJob);
				storagetaskAction_storagetaskActionGroupedJobs.put(storagetaskAction, groupedOnActionJobsList);
			}
			else {
				List<StorageJob> groupedOnActionJobsList = new ArrayList<StorageJob>();
				groupedOnActionJobsList.add(tapeJob);
				storagetaskAction_storagetaskActionGroupedJobs.put(storagetaskAction, groupedOnActionJobsList);
			}			
		}
		
		Set<String> storagetaskActionGroupedJobsKeySet = storagetaskAction_storagetaskActionGroupedJobs.keySet();
		List<String> actionList = new ArrayList<String>(storagetaskActionGroupedJobsKeySet) ;        //set -> list
		//Sort the list
		Collections.sort(actionList);
		
		logger.trace("Completed first step");
		if(logger.isTraceEnabled()) {
			logger.trace("The jobs are grouped like below");
			for (String storagetaskAction : actionList) {
				logger.trace("Grouped job list for storagetask action - " + storagetaskAction);
				List<StorageJob> storagetaskActionGroupedJobsList = storagetaskAction_storagetaskActionGroupedJobs.get(storagetaskAction);
				for (StorageJob storageJob : storagetaskActionGroupedJobsList) {
					logger.trace(""+storageJob.getJob().getId());
				}
				logger.trace("--------------------------------------");
			}
		}
		
		logger.trace("Second step - Ordering the jobs based on storagetaskaction");
		for (String action : actionList) {
			Action storagetaskAction = unmarshallKey(action);
			if(storagetaskAction == Action.finalize) {
				orderedJobsList = storagetaskAction_storagetaskActionGroupedJobs.get(action);
			}
			else if(storagetaskAction == Action.restore) {
				// TODO better this...
				for (int i = Priority.high.getPriorityValue(); i <= Priority.normal.getPriorityValue(); i++) {
					// READ/RESTORE - ordered based on seqBlocks takes precedence over write jobs
					// V5A001 - readjobs = [Job1, Job3, Job4] becomes [Job4, Job1, Job3]
					
					logger.trace("Ordering the read jobs using blocknumber");
					List<StorageJob> readJobs = storagetaskAction_storagetaskActionGroupedJobs.get(action);
					Set<Integer> seqBlockOrderSortedSet = new TreeSet<Integer>();
					// In a single block there could be multiple files so need the List<StorageJob>...
					Map<Integer, List<StorageJob>> seqBlock_seqBlockGroupedJobs = new HashMap<Integer, List<StorageJob>>();
					Map<Integer, StorageJob> seqOffSet_ArchiveJob = new HashMap<Integer, StorageJob>();
					for (Iterator<StorageJob> iterator2 = readJobs.iterator(); iterator2.hasNext();) {
						StorageJob tapeJob = (StorageJob) iterator2.next();
						
						if(tapeJob.getPriority() != i)
							continue;
						
						int seqBlock = tapeJob.getVolumeBlock();
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
					logger.trace("Completed ordering the read jobs using blocknumber");
					
				}
			}
			else if(storagetaskAction == Action.write) { // WRITE/INGEST - ordered based on seqId of the artifact...
				// V5A005 - readjobs = [Job2] and writejobs = [Job6, Job7], retains the same order
				logger.trace("Ordering the write jobs based on artifact seqId");
				List<StorageJob> writeJobs = storagetaskAction_storagetaskActionGroupedJobs.get(action);
				orderOnArtifactSeqId(writeJobs, orderedJobsList);
				logger.trace("Completed ordering the write jobs based on artifact seqId");
			}
		}
		if(logger.isTraceEnabled()) {
			logger.trace("Completed second step. Ordered job list ");
			for (StorageJob storageJob : orderedJobsList) {
				logger.trace(""+storageJob.getJob().getId());
			}
		}
		return orderedJobsList;
	}
	private String marshallKey(Action storagetaskAction){
		String key = null;
		switch (storagetaskAction) {
		case restore:
			key = "1restore";
			break;
		case write:
			key = "2write";
			break;
		default:
			key = storagetaskAction.name();
			break;
		}
		return key;
	}
	private Action unmarshallKey(String storagetaskAction){
		Action key = null;
		switch (storagetaskAction) {
		case "1restore":
			key = Action.restore;
			break;
		case "2write":
			key = Action.write;
			break;
		default:
			key = Action.valueOf(storagetaskAction);
			break;
		}
		return key;
	}

	private void orderOnArtifactSeqId(List<StorageJob> jobs, List<StorageJob> orderedJobsList) {
		
		SortedSet<Integer> artifactSequenceNumberOrderedSortedSet = new TreeSet<Integer>();
		Map<Integer, StorageJob> artifactSequenceNumber_TapeJob = new HashMap<Integer, StorageJob>();

		for (Iterator<StorageJob> iterator2 = jobs.iterator(); iterator2.hasNext();) {
			StorageJob tapeJob = (StorageJob) iterator2.next();
			Artifact artifact = tapeJob.getArtifact();
			String sequenceCode = artifact.getSequenceCode();

			Integer sequenceNumber = null;
			// String seqPrefix = artifact.getArtifactclass().getSequence().getPrefix();
			// String extractedSequenceNumber = sequenceCode.replace(seqPrefix, "");
			// sequenceNumber = Integer.parseInt(extractedSequenceNumber);
			
			Matcher sequenceNumberRegExMatcher = SEQUENCE_NUMBER_PATTERN.matcher(sequenceCode);		
			if(sequenceNumberRegExMatcher.find()) {
				String extractedSequenceNumber = sequenceNumberRegExMatcher.group(1);
				sequenceNumber = Integer.parseInt(extractedSequenceNumber);
			}
					
			artifactSequenceNumberOrderedSortedSet.add(sequenceNumber);
			artifactSequenceNumber_TapeJob.put(sequenceNumber, tapeJob);
		}
		// now running through each artifact Name in the order and adding their respective jobs to the orderedJob collection
		for (Iterator<Integer> iterator2 = artifactSequenceNumberOrderedSortedSet.iterator(); iterator2.hasNext();) {
			Integer artifactSequenceNumber = (Integer) iterator2.next();
			StorageJob tapeJob = artifactSequenceNumber_TapeJob.get(artifactSequenceNumber);
			orderedJobsList.add(tapeJob);
		}				
	}

	/*
	chooses a job by ensuring
		the candidate job is not needing a tape thats already been used by another running job
		the candidate job is not overlapping on concurrent writes with an already running job

		Define non concurrent writes
			1) say artifactclass pub-video is already writing copy 1(it could be any of the non-finalised tapes and need not be a similar copy. For simplicity we will assume its one of similar copy tape)
			2) then jobs for artifactclass pub-video to write copy 2 and 3 should not be allowed... 

	 */
	private StorageJob chooseAJob(List<StorageJob> tapeJobsList, boolean needVerificationAgainstOtherDrives, List<DriveDetails> allDrivesList) throws Exception {
		logger.debug("Choosing a job");
		logger.trace("Getting the currently running tape jobs...");
		List<StorageJob> currentlyRunningTapeJobsList = getTheCurrentlyRunningTapeJobs();
		if(currentlyRunningTapeJobsList == null || currentlyRunningTapeJobsList.size() == 0) {
			// no jobs currently running and hence need not worry about concurrency checks or same tape being used in another job
			logger.debug("No jobs currently running.");
			return getFirstCandidateJob(tapeJobsList, needVerificationAgainstOtherDrives, allDrivesList);
		}
		else {
			// means there are jobs running...
			logger.debug("There are jobs running currently. Filtering same tape jobs and the concurrent jobs that are running");
			// check if there are any writing jobs currently ON...
			List<StorageJob> currentlyRunningWriteJobsList = new ArrayList<StorageJob>();
			
			boolean isWriteJobsOn = false;

			GroupedJobsCollection gjc = groupJobsBasedOnVolumeTag(tapeJobsList);
			Map<String, List<StorageJob>> volumeTag_volumeTagGroupedJobs = gjc.getVolumeTag_volumeTagGroupedJobs();

			logger.trace("Checking if any queued job need the same tape as a currently running job and filtering it.");
			// checking if there are jobs that all need to use the same tape as a currently running job... if yes remove them from the candidate list...
			for (Iterator<StorageJob> iterator = currentlyRunningTapeJobsList.iterator(); iterator.hasNext();) {
				StorageJob runningTapeJob = (StorageJob) iterator.next();
				
				Action storagetaskAction = runningTapeJob.getJob().getStoragetaskActionId();
				if(storagetaskAction == Action.write) {
					isWriteJobsOn = true;
					currentlyRunningWriteJobsList.add(runningTapeJob);
				}

				Volume volumeOnUse = runningTapeJob.getVolume();
				if(volumeOnUse == null) {
					logger.debug("Volume used by a running job is not available. Running job must be a format job. No other job should be running when Format is on...");
					throw new Exception("Volume used by a running job is not available. Running job must be a format job. No other job should be running when Format is on...");
				}
				String alreadyInUseTapeBarCode = volumeOnUse.getId();
				logger.debug("Tape " + alreadyInUseTapeBarCode + " is already in use by " + runningTapeJob.getJob().getId());
				
				// The jobs needing the same tape thats already been used by another running job are removed as we cant run 2 jobs on a same tape at a time....
				if(volumeTag_volumeTagGroupedJobs.containsKey(alreadyInUseTapeBarCode)) {
					logger.trace("Queued jobs need the same tape.");
					List<StorageJob> toBeIgnoredVolumeTagList = volumeTag_volumeTagGroupedJobs.get(alreadyInUseTapeBarCode);
					// Iterating just for the log statement for greater visibility on whats going on...
					for (Iterator<StorageJob> iterator2 = toBeIgnoredVolumeTagList.iterator(); iterator2.hasNext();) {
						StorageJob storageJob = (StorageJob) iterator2.next();
						logger.trace("Ignoring same tape job - " + storageJob.getJob().getId());
					}
					volumeTag_volumeTagGroupedJobs.remove(alreadyInUseTapeBarCode);
					tapeJobsList.removeAll(toBeIgnoredVolumeTagList);
				}
				else {
					logger.trace("No queued job need the same tape.");
				}
				
			}

			// Should we check the nonconcurrentwrites only against running ***write*** jobs and not ***read*** jobs??? 
			// In other words if artifactclass pub-video is already ***reading*** from a volume on copy 1 - should we allow artifactclass pub-video to write copy 2 or not...
			// if power goes off will it only affect the tapes that are writing or will it affect even if its a read operation...
			// ANS: 2nd Feb 2020 - Swami says the read jobs wouldnt affect the tape, and so is ok to skip nonconcurrentwrites checks for reads...
			// How about copy 1 working artifact 1 and copy 2 working on artifact 2 - Order will be a problem
			// How about copy 1 working on artifact 2 and copy 2 working on artifact 1 - Will it still affect the tapes? 
			if(isWriteJobsOn) {
				logger.trace("There are write jobs running.");
				logger.debug("Choosing a job that isnt concurrent overlapping copy");
				List<StorageJob> revisedTapeJobsList = new ArrayList<StorageJob>(); //  jobs list that holds the exhaustive tapejobs but subsequently gets removed of the concurrent overlapping write jobs
				revisedTapeJobsList.addAll(tapeJobsList);
				// NOTE that archiveJobsList now contains entries after removing the jobs that all need to use the same tape as a currently running job...
				for (Iterator<StorageJob> iterator = tapeJobsList.iterator(); iterator.hasNext();) {
					StorageJob tapeJob = (StorageJob) iterator.next();
					if(!revisedTapeJobsList.contains(tapeJob)) {
						continue;
					}
					logger.trace("Checking candidacy for - " + tapeJob.getJob().getId());
					Action storagetaskAction = tapeJob.getJob().getStoragetaskActionId();
					if(storagetaskAction == Action.write) { // only for writes we need to check on the concurrent overlapping 
						// check overlapping on concurrent writes...
						if(!tapeJob.isConcurrentCopies()) { // if concurrent copy on the job is not allowed
							logger.trace("Concurrent copy on this tape job not allowed. Checking if any already running write jobs overlap concurrency with this...");
							String tapeJobArtifactClassId = tapeJob.getArtifact().getArtifactclass().getId();
							
							boolean isOverlappingConcurrency = false;
							for (Iterator<StorageJob> currentlyRunningWriteJobsIterator = currentlyRunningWriteJobsList.iterator(); currentlyRunningWriteJobsIterator.hasNext();) {
								StorageJob currentlyRunningWriteJobAsStorageJobObject = (StorageJob) currentlyRunningWriteJobsIterator.next();
								Job crwj = currentlyRunningWriteJobAsStorageJobObject.getJob();
								
								String crwjGroupVolumeId = crwj.getGroupVolume().getId(); // R1
								int crwjCopyId = crwj.getGroupVolume().getCopy().getId(); // 1
								String crwjPool = StringUtils.substringBeforeLast(crwjGroupVolumeId, crwjCopyId+""); // R
								
								String tapeJobGroupVolumeId = tapeJob.getJob().getGroupVolume().getId(); // R2
								int tapeJobCopyId = tapeJob.getJob().getGroupVolume().getCopy().getId(); // 2
								String tapeJobPool = StringUtils.substringBeforeLast(tapeJobGroupVolumeId, tapeJobCopyId+""); // R
								if(tapeJobPool.equals(crwjPool)) { 
								//if(crwjArtifactClassId.equals(tapeJobArtifactClassId)) { // only if artifactclasses are different. if they are same we should not allow concurrent writes...
									logger.trace("Running write job " + crwj.getId() + " is a concurrent copy to " + tapeJob.getJob().getId() + ". So skipping " + tapeJob.getJob().getId() + " from processing and removing it from list so its not checked again");
									isOverlappingConcurrency = true;
									revisedTapeJobsList.remove(tapeJob);
									break;
								}else {
									logger.trace("Running write job " + crwj.getId() + " is not a concurrent copy to " + tapeJob.getJob().getId());
								}
							}
							if(!isOverlappingConcurrency) {
								logger.debug(tapeJob.getJob().getId() + " has not got a concurrent copy already in writing.");
								if(needVerificationAgainstOtherDrives) {
									logger.debug("Verifying if the tape needed by the job is already loaded in any other Drive");
									if(isTapeNeededForTheJobAlreadyLoadedInAnyDrive(tapeJob, allDrivesList)) {
										logger.debug("Skipped, as tape is available in other drive");
										continue;
									}
								}
								logger.debug("Selected");
								return tapeJob;
							}
						}
						else {
							logger.debug("Concurrent copy on the job " + tapeJob.getJob().getId() + " allowed.");
							if(needVerificationAgainstOtherDrives) {
								logger.debug("Verifying if the tape needed by the job is already loaded in any other Drive");
								if(isTapeNeededForTheJobAlreadyLoadedInAnyDrive(tapeJob, allDrivesList)) {
									logger.debug("Skipped, as tape is available in other drive");
									continue;
								}
							}
							logger.debug("Selected");
							return tapeJob; // return the current job as its a non concurrent write job...
						}
					}
					else {
						
						logger.debug(tapeJob.getJob().getId() + " is not a write job");
						if(needVerificationAgainstOtherDrives) {
							logger.debug("Verifying if the tape needed by the job is already loaded in any other Drive");
							if(isTapeNeededForTheJobAlreadyLoadedInAnyDrive(tapeJob, allDrivesList)) {
								logger.debug("Skipped, as tape is available in other drive");
								continue;
							}
						}
						logger.debug("Selected");
						return tapeJob;
					}
				}
			}else {
				logger.debug("No write jobs currently ON.");
				return getFirstCandidateJob(tapeJobsList, needVerificationAgainstOtherDrives, allDrivesList);
			}
		}
		
		logger.debug("No job selected");
		return null;
	}
	
	private StorageJob getFirstCandidateJob(List<StorageJob> tapeJobsList, boolean needVerificationAgainstOtherDrives, List<DriveDetails> allDrivesList) {
		logger.debug("Getting the first candidate job");
		StorageJob chosenJob = null;
		if(needVerificationAgainstOtherDrives) {
			// verifying if the tape needed by the to be selected job is not already loaded in other drives...
			for (Iterator<StorageJob> iterator = tapeJobsList.iterator(); iterator.hasNext();) {
				StorageJob tapeJob = (StorageJob) iterator.next();
				if(!isTapeNeededForTheJobAlreadyLoadedInAnyDrive(tapeJob, allDrivesList)) {
					chosenJob = tapeJob;
					break;
				}
			}
		}
		else {
			chosenJob = tapeJobsList.get(0);
		}
		return chosenJob; // return the first job
	}

	// Lets say for drive 1 we are choosing a job copy 3 but the tape specific to the job is already available in other drive. Then we have to skip the job from selection. This method does the verification of the tape needed by the job against drives 
	private boolean isTapeNeededForTheJobAlreadyLoadedInAnyDrive(StorageJob tapeJob, List<DriveDetails> allDrivesList){
		String barcode = tapeJob.getVolume().getId();
		logger.trace("Checking if the tape " + barcode + " needed by the job " + tapeJob.getJob().getId() + " is already loaded in any of the drives.");
		
		if(allDrivesList.size() > 0) { // means drive(s) available
			for (Iterator<DriveDetails> driveDetailsIterator = allDrivesList.iterator(); driveDetailsIterator.hasNext();) {
				DriveDetails driveDetails = (DriveDetails) driveDetailsIterator.next();
				if(barcode.equals(driveDetails.getDte().getVolumeTag())) {
					logger.trace("Already loaded in drive " + driveDetails.getDte().getsNo());
					return true;
				}
			}
		}
		
		logger.trace("Tape needed for the job is not already loaded in any of the drives.");
		return false;
	}
	
	private List<StorageJob> getTheCurrentlyRunningTapeJobs(){
		List<StorageJob> currentlyRunningTapeJobsList = new ArrayList<StorageJob>();
		List<TActivedevice> activeDeviceList = (List<TActivedevice>) tActivedeviceDao.findAll();
		for (TActivedevice tActivedevice : activeDeviceList) {
			
			StorageJob tapeJob = new StorageJob();

			Job job = tActivedevice.getJob();
			int jobId = job.getId();
			logger.trace(jobId + " currently running in " + tActivedevice.getDevice().getWwnId());
			
			tapeJob.setJob(job);

			Volume volume = tActivedevice.getVolume();
			tapeJob.setVolume(volume);

			currentlyRunningTapeJobsList.add(tapeJob);
		}
		return currentlyRunningTapeJobsList;
	}
}
