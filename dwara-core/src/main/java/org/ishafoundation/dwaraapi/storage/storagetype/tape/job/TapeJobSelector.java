package org.ishafoundation.dwaraapi.storage.storagetype.tape.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.transactional.TActivedeviceDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.DeviceStatus;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.storage.model.GroupedJobsCollection;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.TapeDeviceUtil;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeJobSelector {
	
	private static final Logger logger = LoggerFactory.getLogger(TapeJobSelector.class);
	
	@Autowired
	private TapeDeviceUtil tapeDeviceUtil;	
	
	@Autowired
	private TActivedeviceDao tActivedeviceDao;

	/**
	 * Method responsible for getting a job for the drive
	 * 
	 * First check if there are ignoreVolumeOptimisation Jobs and choose a job
	 * else choose a job from the exhaustive list 
	 * 
	 * @param tapeJobsList - the jobs list against which the selection happens
	 * @param driveStatusDetails - for which drive the job is getting selected
	 * @return
	 */
	
	public StorageJob selectJob(List<StorageJob> tapeJobsList, DriveDetails driveStatusDetails) {
		if(tapeJobsList.size() <= 0)
			return null;
		
		List<StorageJob> ignoreOptimisationTapeJobsList = getIgnoreOptimisationTapeJobsList(tapeJobsList);
		
		// TODO For now defaulting it to the first one...
		//StorageJob archiveJob = tapeStorageJobsList.get(0);
		StorageJob tapeJob = null;
		if(ignoreOptimisationTapeJobsList.size() > 0) { // are there IgnoreVolumeOptimisation jobs
			logger.debug("Contains Ignore Tape Optimisation jobs");
			// if yes then get a job that is optimised - Yes we need optimisation even on the ignoreVolumeOptimisation job list...
			tapeJob = chooseAJobForTheDrive(ignoreOptimisationTapeJobsList, driveStatusDetails);
		} else {
			logger.debug("Doesnt contain any Ignore Tape Optimisation jobs");// Optimise Tape Access - true");
			tapeJob = chooseAJobForTheDrive(tapeJobsList, driveStatusDetails);
		}
		

		return tapeJob;
	}

	
	private List<StorageJob> getIgnoreOptimisationTapeJobsList(List<StorageJob> tapeJobsList){
		logger.trace("Getting ignore optimisation list");
		List<StorageJob> ignoreOptimisationTapeJobsList = new ArrayList<StorageJob>();
		for (Iterator<StorageJob> iterator = tapeJobsList.iterator(); iterator.hasNext();) {
			StorageJob nthTapeJob = (StorageJob) iterator.next();
//			TODO : Commented out
//			if(!nthTapeJob.isOptimizeTapeAccess()) {
//				ignoreOptimisationTapeJobsList.add(nthTapeJob);
//			}
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
	private StorageJob chooseAJobForTheDrive(List<StorageJob> tapeJobsList, DriveDetails driveStatusDetails){
		logger.debug("Choosing a job for the drive " + driveStatusDetails.getDriveName());
		StorageJob chosenTapeJob = null;
		String volumeTag = driveStatusDetails.getDte().getVolumeTag();

		if(StringUtils.isNotBlank(volumeTag)) { // means the drive has a tape already loaded
			logger.debug("Drive " + driveStatusDetails.getDte().getsNo() + " already has the tape " + volumeTag + " loaded. Checking if the tape is needed by any of the queued jobs");
			for (Iterator<StorageJob> tapeJobsIterator = tapeJobsList.iterator(); tapeJobsIterator.hasNext();) {
				StorageJob tapeJob = (StorageJob) tapeJobsIterator.next();
				Volume toBeUsedVolume = tapeJob.getVolume();
				String toBeUsedVolumeCode = toBeUsedVolume.getUid();
				// checking if the tape is needed by any of the jobs
				if(toBeUsedVolumeCode.equals(volumeTag)) { 
					logger.debug("Jobs in the list match the tape " + volumeTag);
					// if yes group and order the jobs...
					List<StorageJob> groupedAndOrderedJobsOnVolumeTagList = groupAndOrderJobsBasedOnVolumeTag(tapeJobsList, toBeUsedVolumeCode); // returns only the tape specific jobs
					// pick a job by checking if another job is not using the tape and also verify if there is not a concurrent overlap
					chosenTapeJob = chooseAJob(groupedAndOrderedJobsOnVolumeTagList, false, null); // false - as only the drive specific tape related jobs are in the list and hence job's tape needs no verification against other drives...
					//chosenTapeJob.setDriveAlreadyLoadedWithTape(true);
					logger.debug("Job chosen. Removing the same tape jobs from the list");
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
			List<DriveDetails> allAvailableDrivesList = tapeDeviceUtil.getAllAvailableDrivesDetails();
			chosenTapeJob = chooseAJob(groupedAndOrderedJobsList, true, allAvailableDrivesList);
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
		logger.debug("Grouping jobs on tape And Ordering them");
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
					String toBeUsedVolumeCode = tapeJob.getVolume().getUid();

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
			String toBeUsedVolumeCode = tapeJob.getVolume().getUid();
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
	 	Orders/Sorts the list of jobs waiting to be executed on the candidate tape
	
		read jobs had to be ordered based on blocks
		write jobs need to be ordered based on sequence id of the artifact names
	*/
	private List<StorageJob> orderTheJobsForTheTape(List<StorageJob> groupedOnVolumeTagJobsList) {
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
			System.out.println(storagetaskAction + tapeJob.getJob().getId());
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
			if(storagetaskAction == Action.verify) { // VERIFY - ordered based on seqId of the artifact... takes precedence over all other jobs
				logger.trace("Ordering the verify jobs based on artifact seqId");
				List<StorageJob> verifyJobs = storagetaskAction_storagetaskActionGroupedJobs.get(action);
				orderOnArtifactSeqId(verifyJobs, orderedJobsList);
				logger.trace("Completed ordering the verify jobs based on artifact seqId");
			}
			else if(storagetaskAction == Action.restore) {
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
		case verify:
			key = "1verify";
			break;
		case restore:
			key = "2restore";
			break;
		case write:
			key = "3write";
			break;

		default:
			break;
		}
		return key;
	}
	private Action unmarshallKey(String storagetaskAction){
		Action key = null;
		switch (storagetaskAction) {
		case "1verify":
			key = Action.verify;
			break;
		case "2restore":
			key = Action.restore;
			break;
		case "3write":
			key = Action.write;
			break;

		default:
			break;
		}
		return key;
	}
	private void orderOnArtifactSeqId(List<StorageJob> jobs, List<StorageJob> orderedJobsList) {
		
		Set<String> artifactNameOrderedSortedSet = new TreeSet<String>();
		Map<String, StorageJob> artifactName_TapeJob = new HashMap<String, StorageJob>();

		for (Iterator<StorageJob> iterator2 = jobs.iterator(); iterator2.hasNext();) {
			StorageJob tapeJob = (StorageJob) iterator2.next();
			Artifact artifact = tapeJob.getArtifact();
			String artifactName = artifact.getName();
			artifactNameOrderedSortedSet.add(artifactName);
			artifactName_TapeJob.put(artifactName, tapeJob);
		}
		// now running through each artifact Name in the order and adding their respective jobs to the orderedJob collection
		for (Iterator<String> iterator2 = artifactNameOrderedSortedSet.iterator(); iterator2.hasNext();) {
			String artifactName = (String) iterator2.next();
			StorageJob tapeJob = artifactName_TapeJob.get(artifactName);
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
	private StorageJob chooseAJob(List<StorageJob> tapeJobsList, boolean needVerificationAgainstOtherDrives, List<DriveDetails> allAvailableDrivesList) {
		logger.debug("Choosing a job");
		logger.trace("Getting the currently running tape jobs...");
		List<StorageJob> currentlyRunningTapeJobsList = getTheCurrentlyRunningTapeJobs();
		if(currentlyRunningTapeJobsList == null || currentlyRunningTapeJobsList.size() == 0) {
			// no jobs currently running and hence need not worry about concurrency checks or same tape being used in another job
			logger.debug("No jobs currently running.");
			return getFirstCandidateJob(tapeJobsList, needVerificationAgainstOtherDrives, allAvailableDrivesList);
		}
		else {
			// means there are jobs running...
			logger.debug("There are jobs running currently. Filtering same tape jobs and concurrent jobs");
			// check if there are any writing jobs currently ON...
			List<StorageJob> currentlyRunningWriteJobsList = new ArrayList<StorageJob>();
			
			boolean isWriteJobsOn = false;

			GroupedJobsCollection gjc = groupJobsOnVolumeTag(tapeJobsList);
			Map<String, List<StorageJob>> volumeTag_volumeTagGroupedJobs = gjc.getVolumeTag_volumeTagGroupedJobs();

			logger.trace("Checking if any other queued job need the same tape and filtering it.");
			// checking if there are jobs that all need to use the same tape as a currently running job... if yes remove them from the candidate list...
			for (Iterator<StorageJob> iterator = currentlyRunningTapeJobsList.iterator(); iterator.hasNext();) {
				StorageJob runningTapeJob = (StorageJob) iterator.next();
				
				Action storagetaskAction = runningTapeJob.getJob().getStoragetaskActionId();
				if(storagetaskAction == Action.write) {
					isWriteJobsOn = true;
					currentlyRunningWriteJobsList.add(runningTapeJob);
				}

				
				String alreadyInUseTapeBarCode = runningTapeJob.getVolume().getUid();
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
				logger.debug("Checking and filtering concurrent overlapping writes");
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
							int tapeJobArtifactClassId = tapeJob.getArtifact().getArtifactclassId();
							
							boolean isOverlappingConcurrency = false;
							for (Iterator<StorageJob> currentlyRunningWriteJobsIterator = currentlyRunningWriteJobsList.iterator(); currentlyRunningWriteJobsIterator.hasNext();) {
								StorageJob currentlyRunningWriteJobAsStorageJobObject = (StorageJob) currentlyRunningWriteJobsIterator.next();
								Job crwj = currentlyRunningWriteJobAsStorageJobObject.getJob();
								
								/* TODO @Swami
								 * same request or different request, if the 3 copies are loaded and power goes off then all 3 could go bad
								 * means at no point in time we should have all the 3 copies loaded at the same time in drives?
								 * So commenting out the below...
								if(tapeJob.getJob().getRequestId() != crwj.getRequestId()) // if the jobs are for different requests then no need to check concurrent writes. Yes swami clarified...
									return tapeJob;
								*/

								int crwjArtifactClassId = crwj.getRequest().getDetails().getArtifactclass_id();
//								Integer inputArtifactId = crwj.getInputArtifactId();
//								Domain domain = crwj.getRequest().getDomain();
//								Artifact artifact = domainUtil.getDomainSpecificArtifact(domain, inputArtifactId);
//
//								int crwjArtifactClassId = artifact.getArtifactclass().getId();
								//							int crij_poolId = crij_copy.getPoolId();
								//							int crij_copyNumber = crij_copy.getCopyNumber();

								// TODO ask swami - can we allow if V5A001 doing copy1 for artifact1 and V5B001 do copy2 on artifact2? Answer should be NO - as the order changes...
								// What happens if 2 artifactclasses share same set of pools... The check ideally should be on the copy tapes involved... for eg., if we have V5A001 then we shouldnt have V5B001 and V5C001
								
								if(crwjArtifactClassId != tapeJobArtifactClassId) { // only if artifactclasses are different. if they are same we should not allow concurrent writes...
									logger.trace("Running write job " + crwj.getId() + " is not a concurrent copy to " + tapeJob.getJob().getId());
									
								}else {
									logger.trace("Running write job " + crwj.getId() + " is a concurrent copy to " + tapeJob.getJob().getId() + ". So skipping " + tapeJob.getJob().getId() + " from processing and removing it from list so its not checked again");
									isOverlappingConcurrency = true;
									revisedTapeJobsList.remove(tapeJob);
									break;
								}
							}
							if(!isOverlappingConcurrency) {
								logger.debug(tapeJob.getJob().getId() + " has not got a concurrent copy already in writing.");
								if(needVerificationAgainstOtherDrives) {
									logger.debug("Verifying if the tape needed by the job is already loaded in any other Drive");
									if(isTapeNeededForTheJobAlreadyLoadedInAnyDrive(tapeJob, allAvailableDrivesList)) {
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
								if(isTapeNeededForTheJobAlreadyLoadedInAnyDrive(tapeJob, allAvailableDrivesList)) {
									logger.debug("Skipped, as tape is available in other drive");
									continue;
								}
							}
							logger.debug("Selected");
							return tapeJob; // return the current job as its a non concurrent write job...
						}
					}
				}
			}else {
				logger.debug("No write jobs currently ON.");
				return getFirstCandidateJob(tapeJobsList, needVerificationAgainstOtherDrives, allAvailableDrivesList);
			}
		}
		return null;
	}
	
	private StorageJob getFirstCandidateJob(List<StorageJob> tapeJobsList, boolean needVerificationAgainstOtherDrives, List<DriveDetails> allAvailableDrivesList) {
		logger.debug("Getting the first candidate job");
		StorageJob chosenJob = null;
		if(needVerificationAgainstOtherDrives) {
			// verifying if the tape needed by the to be selected job is not already loaded in other drives...
			for (Iterator<StorageJob> iterator = tapeJobsList.iterator(); iterator.hasNext();) {
				StorageJob tapeJob = (StorageJob) iterator.next();
				if(!isTapeNeededForTheJobAlreadyLoadedInAnyDrive(tapeJob, allAvailableDrivesList)) {
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
	private boolean isTapeNeededForTheJobAlreadyLoadedInAnyDrive(StorageJob tapeJob, List<DriveDetails> allAvailableDrivesList){
		String barcode = tapeJob.getVolume().getUid();
		logger.trace("Checking if the tape " + barcode + " needed by the job " + tapeJob.getJob().getId() + " is already loaded in any of the drives.");
		
		if(allAvailableDrivesList.size() > 0) { // means drive(s) available
			for (Iterator<DriveDetails> driveStatusDetailsIterator = allAvailableDrivesList.iterator(); driveStatusDetailsIterator.hasNext();) {
				DriveDetails driveStatusDetails = (DriveDetails) driveStatusDetailsIterator.next();
				if(barcode.equals(driveStatusDetails.getDte().getVolumeTag())) {
					logger.trace("Already loaded in drive " + driveStatusDetails.getDte().getsNo());
					return true;
				}
			}
		}
		
		logger.trace("Tape needed for the job is not already loaded in any of the drives.");
		return false;
	}
	
	private List<StorageJob> getTheCurrentlyRunningTapeJobs(){
		List<StorageJob> currentlyRunningTapeJobsList = new ArrayList<StorageJob>();
		List<TActivedevice> activeDeviceList = tActivedeviceDao.findAllByDeviceDevicetypeAndDeviceStatus(Devicetype.tape_drive, DeviceStatus.BUSY);
		for (TActivedevice tActivedevice : activeDeviceList) {
			
			StorageJob tapeJob = new StorageJob();

			Job job = tActivedevice.getJob();
			int jobId = job.getId();
			logger.trace(jobId + " currently running in " + tActivedevice.getDevice().getUid());
			
			tapeJob.setJob(job);

			Volume volume = tActivedevice.getVolume();
			tapeJob.setVolume(volume);

			currentlyRunningTapeJobsList.add(tapeJob);
		}
		return currentlyRunningTapeJobsList;
	}
}
