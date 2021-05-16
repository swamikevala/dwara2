package org.ishafoundation.dwaraapi.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.ishafoundation.dwaraapi.ApplicationStatus;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDaoQueryProps;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class JobManager {
	
	private static final Logger logger = LoggerFactory.getLogger(JobManager.class);
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private StoragetypeJobDelegator storagetypeJobDelegator;
	
	@Autowired
	private ApplicationContext applicationContext;
		
	@Autowired
	private ProcessingtaskSingleThreadExecutor processingtaskSingleThreadExecutor;
	
	@Autowired
	private Map<String, IStoragetypeThreadPoolExecutor> storagetypeThreadPoolExecutorMap;
		
	@Autowired
	private Configuration configuration;
	
	@Value("${wowo.useNewJobManagementLogic:true}")
	private boolean useNewJobManagementLogic;
	
	public void manageJobs() {
		logger.info("***** Managing jobs now *****");
		ThreadPoolExecutor tpe = (ThreadPoolExecutor) processingtaskSingleThreadExecutor.getExecutor();
		if(ApplicationStatus.valueOf(configuration.getAppMode()) == ApplicationStatus.maintenance) {
			logger.info("Application is in maintenance mode. No jobs will be taken up for action");
			BlockingQueue<Runnable> runnableQueueList = tpe.getQueue();
			if(runnableQueueList.size() > 0) {
				runnableQueueList.clear();
				logger.info("Cleared Processing tasks from ThreadPoolExecutor queue");
			}
			
//			Set<String> processingtaskSet = processingtaskActionMap.keySet();
//			for (String nthProcessingtask : processingtaskSet) {
//				IProcessingTask processingtaskImpl = processingtaskActionMap.get(nthProcessingtask);
//				if(processingtaskImpl == null)
//					continue;
//				
//				ThreadPoolExecutor executor = (ThreadPoolExecutor) IProcessingTask.taskName_executor_map.get(nthProcessingtask.toLowerCase());
//				if(executor == null)
//					executor = (ThreadPoolExecutor) IProcessingTask.taskName_executor_map.get(IProcessingTask.GLOBAL_THREADPOOL_IDENTIFIER);
//				
//				BlockingQueue<Runnable> processingtaskSpecificQueueList = executor.getQueue();
//				int queueSize = processingtaskSpecificQueueList.size();
//				if(queueSize > 0) {
//					processingtaskSpecificQueueList.clear();
//					logger.info("Flushed " + queueSize + " " + nthProcessingtask + "'s queued jobs from ThreadPoolExecutor queue");
//				}
//			}
			
			Set<String> storageTypeTPESet = storagetypeThreadPoolExecutorMap.keySet();
			for (String nthStorageTypeTPEName : storageTypeTPESet) {
				IStoragetypeThreadPoolExecutor storagetypeThreadPoolExecutor = storagetypeThreadPoolExecutorMap.get(nthStorageTypeTPEName);
				
				ThreadPoolExecutor storageTypeTPE = storagetypeThreadPoolExecutor.getExecutor();
				BlockingQueue<Runnable> runnableStorageQueueList = storageTypeTPE.getQueue();
				if(runnableStorageQueueList.size() > 0) {
					// Ideally the Storagetype specific JobManager just need to delegate the job to the processor thread. So by the time the next schedule gets here the queue should be empty. 
					// But just in case if it has items clear them and feed it with the fresh job list...
					runnableStorageQueueList.clear();
					logger.info("Cleared Storage tasks from ThreadPoolExecutor queue");
				}
			}
			return;
		}
		List<Job> storageJobsList = new ArrayList<Job>();
		if(!useNewJobManagementLogic) {
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
		else {
			// storage specific jobs
			// defective tape migration use case with lot of same tape jobs just give one or two..give.
			for (String executorName : JobDao.executorName_queryProps_map.keySet()) {
				JobDaoQueryProps jobDaoQueryProps = JobDao.executorName_queryProps_map.get(executorName);
				
				Pageable limitBy = PageRequest.of(0, jobDaoQueryProps.getLimit());
				List<Job> jobList = jobDao.findAllByStatusAndProcessingtaskIdInOrderById(Status.queued, jobDaoQueryProps.getTaskNameList(), limitBy);
				logger.trace("===" + executorName + ":" + jobDaoQueryProps.getTaskNameList() + ":" + jobDaoQueryProps.getLimit() + ":" + jobList.size());
				
				if(jobList.size() > 0) {
					for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
						Job job = (Job) iterator.next();
						String processingtaskName = job.getProcessingtaskId();
						
						logger.trace("Job - " + job.getId() + ":" + processingtaskName);
						// This check is because of the same file getting queued up for processing again...
						// JobManager --> get all "Queued" processingjobs --> ProcessingJobManager ==== thread per file ====> ProcessingJobProcessor --> Only when the file's turn comes the status change to inprogress
						// Next iteration --> get all "Queued" processingjobs would still show the same job above sent already to ProcessingJobManager as it has to wait for its turn for CPU cycle... 
	
						BlockingQueue<Runnable> runnableQueueList = tpe.getQueue();
						boolean alreadyQueued = false;
						// NOT POSSIBLE - HENCE COMMENTING OUT
//						for (Runnable runnable : runnableQueueList) {
//							ProcessingJobManager pjm = (ProcessingJobManager) runnable;
//							if(job.getId() == pjm.getJob().getId()) {
//								logger.debug(job.getId() + " already in ProcessingJobManager queue. Skipping it...");
//								alreadyQueued = true;
//								break;
//							}
//						}
						if(!alreadyQueued) { // only when the job is not already dispatched to the queue to be executed, send it now...
							ProcessingJobManager processingJobManager = applicationContext.getBean(ProcessingJobManager.class);
							processingJobManager.setJob(job);
							logger.debug(job.getId() + " added to the ProcessingJobManager queue.");
							tpe.execute(processingJobManager);
						}
					}
				}
			}
			
			List<Job> jobList = jobDao.findAllByStoragetaskActionIdIsNotNullAndStatusOrderById(Status.queued); // Irrespective of the tapedrivemapping or format request non storage jobs can still be dequeued, hence we are querying it all... 
			if(jobList.size() > 0) {
				for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
					Job job = (Job) iterator.next();
					
					Action storagetaskAction = job.getStoragetaskActionId();
					String jobName = storagetaskAction.name();
					logger.trace("Job - " + job.getId() + ":" + jobName);
					storageJobsList.add(job);
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
}
	
