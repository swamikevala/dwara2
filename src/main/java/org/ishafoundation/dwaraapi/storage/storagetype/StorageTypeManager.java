package org.ishafoundation.dwaraapi.storage.storagetype;

import java.util.List;

import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.job.TapeJobsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageTypeManager {
	
	private static final Logger logger = LoggerFactory.getLogger(StorageTypeManager.class);
	
	@Autowired
	private TapeJobsManager tapeJobsManager;
	
	/**
	 * Group the jobs based on storagetype
	 * Delegate it to storagetype Specific jobs processor
	 */
	public void process(List<StorageJob> storageJobList) {
//		Map<Integer, List<StorageJob>> storageTypeId_storageTypeGroupedJobs = copyJobsLister.groupAllPendingJobsOnStorageType(pendingArchiveJobsList);
//		
//		
//		List<Storagetype> storagetypeList = storagetypeUtil.getStoragetypeList();
//		for (Iterator<Storagetype> iterator = storagetypeList.iterator(); iterator.hasNext();) {
//			Storagetype storagetype = (Storagetype) iterator.next();
//	        
//			switch (storagetype.getName().toUpperCase()) {
//				case "TAPE":
//					tapeJobsManager.manage(storageTypeId_storageTypeGroupedJobs.get(storagetype.getStoragetypeId()));
//					break;
//				default:
//					break;
//			}
//		}
		
		logger.trace("Calling Storage type specific manager");
		logger.trace("hardcoded to TAPE");
		
		tapeJobsManager.manage(storageJobList);
		
	}
}