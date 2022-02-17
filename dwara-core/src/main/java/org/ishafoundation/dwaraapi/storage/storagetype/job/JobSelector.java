package org.ishafoundation.dwaraapi.storage.storagetype.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ishafoundation.dwaraapi.storage.model.GroupedJobsCollection;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSelector {
	
	private static final Logger logger = LoggerFactory.getLogger(JobSelector.class);
	
	public GroupedJobsCollection groupJobsBasedOnVolumeTag(List<StorageJob> tapeJobsList){
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


}
