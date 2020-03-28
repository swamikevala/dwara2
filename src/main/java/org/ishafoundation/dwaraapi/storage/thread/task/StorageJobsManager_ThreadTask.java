package org.ishafoundation.dwaraapi.storage.thread.task;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class StorageJobsManager_ThreadTask implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
//
//    private static final Logger logger = LoggerFactory.getLogger(StorageJobsManager_ThreadTask.class);
//
//	@Autowired
//	private StorageJobBuilder storageJobBuilder;
//
//	@Autowired
//	private StorageTypeManager storageTypeManager;
//	
//	private List<Job> jobList;
//	
//
//	public List<Job> getJobList() {
//		return jobList;
//	}
//
//	public void setJobList(List<Job> jobList) {
//		this.jobList = jobList;
//	}
//
//
//	@Override
//    public void run() {
//		List<StorageJob> storageJobList = new ArrayList<StorageJob>();
//		
//		for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
//			Job job = (Job) iterator.next();
//			
//			storageJobList.add(storageJobBuilder.buildStorageJob(job));
//		}
//		
//		storageTypeManager.process(storageJobList);
//	}
}
