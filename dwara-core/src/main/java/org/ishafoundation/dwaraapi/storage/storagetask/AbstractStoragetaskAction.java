package org.ishafoundation.dwaraapi.storage.storagetask;



import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractStoragetaskAction{

	private static final Logger logger = LoggerFactory.getLogger(AbstractStoragetaskAction.class);
	
	@Autowired
	private JobDao jobDao;		
	
	protected List<org.ishafoundation.dwaraapi.enumreferences.Action> storagetaskActionList = new ArrayList<org.ishafoundation.dwaraapi.enumreferences.Action>();
	
	public List<Job> createJobsForStoragetaskAction(Request request, Action action){
		List<Job> simpleActionJobs = new ArrayList<Job>();
		
		if(storagetaskActionList.size() == 0) { // one action to one storage task - like format/finalize etc..
			simpleActionJobs.add(createStoragetaskActionJob(null, request, action));
		}
		else { // one action to many storagetask like - rewrite to restore/write/verify
			Job job = null;
			for (Action nthAction : storagetaskActionList) {
				simpleActionJobs.add(job = createStoragetaskActionJob(job, request, nthAction));
			}
		}
		return simpleActionJobs;
	}
	
	protected Job createStoragetaskActionJob(Job parentJob, Request request, Action storagetaskAction){
		Job job = new Job();

		if(parentJob != null)
			job.setJobRef(parentJob);
		
		job.setStoragetaskActionId(storagetaskAction);
		job.setRequest(request);
		// TODO needed only for sub actions - job.setInputArtifactId(subrequest.getDetails().getArtifact_id());
		job.setCreatedAt(LocalDateTime.now());
		job.setStatus(Status.queued);
		// TODO ??? - here or we will do this in buildstoragejob???
//		if(action == Action.restore) {
//			//subrequest
//			Volume volume = null;
//			//getvolume related stuff from the file here...
//			job.setVolume(volume);
//		}
		return saveJob(job);
	}

	
	public StorageJob buildStorageJob(Job job){
		// If needed to be overwritten by subclass implementations
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		return storageJob;
	}
	
	public void postProcessDbUpdates(Job job, ArchiveResponse archiveResponse) {
		// If needed to be overwritten by subclass implementations
	}

//	public ArchiveResponse process(StoragetypeJob storagejob) throws Throwable {
//		Job job = null;
//		ArchiveResponse archiveResponse = null;
//		try {
////			job = storagejob.getJob();
////			jobUtils.updateJobInProgress(job);
//			
//			archiveResponse = execute(storagejob);
//			
////			jobUtils.updateJobCompleted(job);
//		}catch (Exception e) {
////			jobUtils.updateJobFailed(job);
//			e.printStackTrace();
//			throw e;
//		}
//		return archiveResponse;
//
//	}

	//public abstract ArchiveResponse execute(StoragetypeJob storagejob) throws Throwable;

	private Job saveJob(Job job) {
		job = jobDao.save(job);
		logger.info("Job " + job.getId() + " - Created");
		return job;
	}



}
