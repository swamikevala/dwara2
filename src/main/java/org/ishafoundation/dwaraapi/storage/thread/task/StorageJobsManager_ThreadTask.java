package org.ishafoundation.dwaraapi.storage.thread.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.storage.StorageJobBuilder;
import org.ishafoundation.dwaraapi.storage.StorageTypeManager;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class StorageJobsManager_ThreadTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(StorageJobsManager_ThreadTask.class);

	@Autowired
	private StorageJobBuilder storageJobBuilder;

	@Autowired
	private StorageTypeManager storageTypeManager;
	
	private List<Job> jobList;
	

	public List<Job> getJobList() {
		return jobList;
	}

	public void setJobList(List<Job> jobList) {
		this.jobList = jobList;
	}


	@Override
    public void run() {
		List<StorageJob> storageJobList = new ArrayList<StorageJob>();
		
		for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
			Job job = (Job) iterator.next();
			
			storageJobList.add(storageJobBuilder.buildStorageJob(job));
		}
		
		storageTypeManager.process(storageJobList);
	}
}
