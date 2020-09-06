package org.ishafoundation.dwaraapi.storage.storagetype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
import org.ishafoundation.dwaraapi.storage.storagetype.thread.AbstractStoragetypeJobManager;
import org.ishafoundation.dwaraapi.storage.storagetype.thread.IStoragetypeThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class StoragetypeJobDelegator {
	
	private static final Logger logger = LoggerFactory.getLogger(StoragetypeJobDelegator.class);
		
	@Autowired
	private Map<String, AbstractStoragetaskAction> storagetaskActionMap;
	
	@Autowired
	private Map<String, AbstractStoragetypeJobManager> storageTypeJobManagerMap;
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private  Map<String, IStoragetypeThreadPoolExecutor> storagetypeThreadPoolExecutorMap;

	/**
	 * Wraps the jobs with extra storage info and groups based on storagetype and then delegates it to appropriate storagetype specific jobs processor
	 */
	public void delegate(List<Job> storageJobList) {
		Map<Storagetype, List<StorageJob>> storagetype_storageTypeGroupedJobsList_Map = wrapJobsWithMoreInfoAndGroupOnStorageType(storageJobList);
		Set<Storagetype> storagetypeSet = storagetype_storageTypeGroupedJobsList_Map.keySet();
		
		// delegate the task to appropriate storagetype specific jobs processor
		for (Storagetype storagetype : storagetypeSet) {
			IStoragetypeThreadPoolExecutor storagetypeThreadPoolExecutor = storagetypeThreadPoolExecutorMap.get(storagetype.name() + DwaraConstants.STORAGETYPE_THREADPOOLEXECUTOR_SUFFIX);
			
			ThreadPoolExecutor tpe = storagetypeThreadPoolExecutor.getExecutor();
			BlockingQueue<Runnable> runnableQueueList = tpe.getQueue();
			// Ideally the Storagetype specific JobManager just need to delegate the job to the processor thread. So by the time the next schedule gets here the queue should be empty. 
			// But just in case if it has items clear them and feed it with the fresh job list...
			runnableQueueList.clear();
			logger.debug("Cleared existing IStoragetypeThreadPoolExecutor queue item and added the fresh storage job list...");
			/*
			if(runnableQueueList.size() >= 3) {
				logger.debug("Already 3 storagejobset lined up in " + storagetype.name() + DwaraConstants.STORAGETYPE_THREADPOOLEXECUTOR_SUFFIX + ". Skipping this schedule");
			}else {
			 */
				
//			boolean alreadyQueued = false;
//			for (Runnable runnable : runnableQueueList) {
//				AbstractStoragetypeJobManager asjm = (AbstractStoragetypeJobManager) runnable;
//				
//				if(job.getId() == asjm.getJob().getId()) {
//					alreadyQueued = true;
//					break;
//				}
//			}
				logger.debug("Delegating to " + storagetype.name() + DwaraConstants.STORAGETYPE_JOBMANAGER_SUFFIX + "'s separate thread ================");
				AbstractStoragetypeJobManager storageTypeJobManager = storageTypeJobManagerMap.get(storagetype.name() + DwaraConstants.STORAGETYPE_JOBMANAGER_SUFFIX);
				storageTypeJobManager.setStorageJobList(storagetype_storageTypeGroupedJobsList_Map.get(storagetype));
				tpe.execute(storageTypeJobManager);
			/*}*/
		}
	}
	
	private Map<Storagetype, List<StorageJob>> wrapJobsWithMoreInfoAndGroupOnStorageType(List<Job> storageJobList){
		Map<Storagetype, List<StorageJob>> storagetype_storageTypeGroupedJobsList_Map = new HashMap<>();
		logger.debug("Wrapping jobs with more storage info and grouping them on Storagetype");
		for (Job job : storageJobList) {
			Action storagetaskAction = job.getStoragetaskActionId();
			AbstractStoragetaskAction storagetaskActionImpl = storagetaskActionMap.get(storagetaskAction.name());
			logger.trace("building storage job - " + job.getId() + ":" + storagetaskActionImpl.getClass().getSimpleName());
			StorageJob storageJob = null;
			try {
				storageJob = storagetaskActionImpl.buildStorageJob(job);
			} catch (Exception e) {
				logger.error("Unable to gather necessary details for executing the job " + job.getId() + " - " + Status.failed, e);
				job.setStatus(Status.failed); // fail the job so it doesnt keep looping...
				job = jobDao.save(job);
				continue;
			}
			Volume volume = storageJob.getVolume();
			if(volume == null) {
				String msg = "No volume available just yet for job "  + job.getId() + ". So skipping this job this schedule...";
				logger.info(msg);
				job.setErrorMsg(msg);
				job = jobDao.save(job);
				continue;
			}
			Storagetype storagetype = volume.getStoragetype();
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