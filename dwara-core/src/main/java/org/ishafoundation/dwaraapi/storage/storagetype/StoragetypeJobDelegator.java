package org.ishafoundation.dwaraapi.storage.storagetype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.ActionAttributeConverter;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
import org.ishafoundation.dwaraapi.storage.storagetype.thread.AbstractStoragetypeJobManager;
import org.ishafoundation.dwaraapi.storage.storagetype.thread.StorageSingleThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class StoragetypeJobDelegator {
	
	private static final Logger logger = LoggerFactory.getLogger(StoragetypeJobDelegator.class);
	
	@Autowired
	private ActionAttributeConverter actionAttributeConverter;
	
	@Autowired
	private Map<String, AbstractStoragetaskAction> storagetaskActionMap;
	
	@Autowired
	private Map<String, AbstractStoragetypeJobManager> storageTypeJobManagerMap;
	
	// TODO : shouldnt this be single thread per storagetype???
	@Autowired
	private StorageSingleThreadExecutor storageSingleThreadExecutor;

	/**
	 * Wraps the jobs with extra storage info and groups based on storagetype and then delegates it to appropriate storagetype specific jobs processor
	 */
	public void process(List<Job> storageJobList) {
		Map<Storagetype, List<StorageJob>> storagetype_storageTypeGroupedJobsList_Map = wrapJobsWithMoreInfoAndGroupOnStorageType(storageJobList);
		Set<Storagetype> storagetypeSet = storagetype_storageTypeGroupedJobsList_Map.keySet();
		
		// delegate the task to appropriate storagetype specific jobs processor
		for (Storagetype storagetype : storagetypeSet) {
			logger.debug("Delegating to " + storagetype.name() + "job manager's separate thread ================");
			AbstractStoragetypeJobManager storageTypeJobManager = storageTypeJobManagerMap.get(storagetype.name() + DwaraConstants.StorageTypeJobManagerSuffix);
			storageTypeJobManager.setStorageJobList(storagetype_storageTypeGroupedJobsList_Map.get(storagetype));
			storageSingleThreadExecutor.getExecutor().execute(storageTypeJobManager);
		}
	}
	
	private Map<Storagetype, List<StorageJob>> wrapJobsWithMoreInfoAndGroupOnStorageType(List<Job> storageJobList){
		Map<Storagetype, List<StorageJob>> storagetype_storageTypeGroupedJobsList_Map = new HashMap<>();
		logger.debug("WrapJobsWithMoreInfoAndGroupOnStorageType");
		for (Job job : storageJobList) {
			Integer storagetaskActionId = job.getStoragetaskActionId();
			Action storagetaskAction = actionAttributeConverter.convertToEntityAttribute(storagetaskActionId);
			AbstractStoragetaskAction storagetaskActionImpl = storagetaskActionMap.get(storagetaskAction.name());
			StorageJob storageJob = storagetaskActionImpl.buildStorageJob(job);
			Storagetype storagetype = storageJob.getVolume().getStoragetype();
			if(storagetype_storageTypeGroupedJobsList_Map.get(storagetype) != null) {
				List<StorageJob> jobList = storagetype_storageTypeGroupedJobsList_Map.get(storagetype);
				jobList.add(storageJob);
			}
			else {
				List<StorageJob> jobList = new ArrayList<StorageJob>();
				jobList.add(storageJob);
				storagetype_storageTypeGroupedJobsList_Map.put(storagetype, jobList);
			}

			
//			int volumeId = job.getActionelement().getVolumeId();
//			if(volumeId > 0) {
//				Volume volume = volumeDao.findById(volumeId).get();
//			}
		}
		return storagetype_storageTypeGroupedJobsList_Map;
	}
	
}