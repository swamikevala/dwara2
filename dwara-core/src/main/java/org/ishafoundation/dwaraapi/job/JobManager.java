package org.ishafoundation.dwaraapi.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.ishafoundation.dwaraapi.ApplicationStatus;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.process.thread.ProcessingJobManager;
import org.ishafoundation.dwaraapi.storage.storagetype.StoragetypeJobDelegator;
import org.ishafoundation.dwaraapi.storage.storagetype.thread.IStoragetypeThreadPoolExecutor;
import org.ishafoundation.dwaraapi.thread.executor.ProcessingtaskSingleThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class JobManager {
	
	private static final Logger logger = LoggerFactory.getLogger(JobManager.class);

	// Should the default be configurable so during dev we dont have to start the app and then run the api to have jobs dequeued?
	public static ApplicationStatus MODE = ApplicationStatus.maintenance;
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private StoragetypeJobDelegator storagetypeJobDelegator;
	
	@Autowired
	private ApplicationContext applicationContext;
		
	@Autowired
	private ProcessingtaskSingleThreadExecutor processingtaskSingleThreadExecutor;
	
	@Autowired
	private  Map<String, IStoragetypeThreadPoolExecutor> storagetypeThreadPoolExecutorMap;
	
	public void manageJobs() {
		logger.info("***** Managing jobs now *****");
		ThreadPoolExecutor tpe = (ThreadPoolExecutor) processingtaskSingleThreadExecutor.getExecutor();
		if(MODE == ApplicationStatus.maintenance) {
			logger.info("Application is in maintenance mode. No jobs will be taken up for action. Also clearing already lined up cached jobs as well");
			BlockingQueue<Runnable> runnableQueueList = tpe.getQueue();
			runnableQueueList.clear();
			
			Set<String> storageTypeTPESet = storagetypeThreadPoolExecutorMap.keySet();
			for (String nthStorageTypeTPEName : storageTypeTPESet) {
				IStoragetypeThreadPoolExecutor storagetypeThreadPoolExecutor = storagetypeThreadPoolExecutorMap.get(nthStorageTypeTPEName);
				
				ThreadPoolExecutor storageTypeTPE = storagetypeThreadPoolExecutor.getExecutor();
				BlockingQueue<Runnable> runnableStorageQueueList = storageTypeTPE.getQueue();
				// Ideally the Storagetype specific JobManager just need to delegate the job to the processor thread. So by the time the next schedule gets here the queue should be empty. 
				// But just in case if it has items clear them and feed it with the fresh job list...
				runnableStorageQueueList.clear();
			}
			return;
		}
		List<Job> storageJobsList = new ArrayList<Job>();
		
		List<Job> jobList = jobDao.findAllByStatusOrderById(Status.queued); // Irrespective of the tapedrivemapping or format request non storage jobs can still be dequeued, hence we are querying it all... 
		if(jobList.size() > 0) {
			for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
				Job job = (Job) iterator.next();
				
				String jobName = null;
				Action storagetaskAction = job.getStoragetaskActionId();
				String processingtaskId = job.getProcessingtaskId();
				if(storagetaskAction != null) {
					jobName = storagetaskAction.name();
				}
				else {
					jobName = processingtaskId;
				}
				logger.trace("Job - " + job.getId() + ":" + jobName);
				
				if(processingtaskId != null) { // a non-storage process job
					// This check is because of the same file getting queued up for processing again...
					// JobManager --> get all "Queued" processingjobs --> ProcessingJobManager ==== thread per file ====> ProcessingJobProcessor --> Only when the file's turn comes the status change to inprogress
					// Next iteration --> get all "Queued" processingjobs would still show the same job above sent already to ProcessingJobManager as it has to wait for its turn for CPU cycle... 
					
					BlockingQueue<Runnable> runnableQueueList = tpe.getQueue();
					boolean alreadyQueued = false;
					for (Runnable runnable : runnableQueueList) {
						ProcessingJobManager pjm = (ProcessingJobManager) runnable;
						if(job.getId() == pjm.getJob().getId()) {
							logger.debug(job.getId() + " already in ProcessingJobManager queue. Skipping it...");
							alreadyQueued = true;
							break;
						}
					}
					if(!alreadyQueued) { // only when the job is not already dispatched to the queue to be executed, send it now...
						ProcessingJobManager processingJobManager = applicationContext.getBean(ProcessingJobManager.class);
						processingJobManager.setJob(job);
						logger.debug(job.getId() + " added to the ProcessingJobManager queue.");
						tpe.execute(processingJobManager);
					}
				}else {
					storageJobsList.add(job);
				}
			}
			
			if(storageJobsList.size() > 0) {
				logger.debug(storageJobsList.size() + " storage jobs are process ready");
				storagetypeJobDelegator.delegate(storageJobsList);
			}else {
				logger.trace("No storage job to be processed");
			}
		}
		else {
			logger.trace("No jobs queued up");
		}
	}

}
	
