package org.ishafoundation.dwaraapi.storage.storagetype.thread;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.ActionAttributeConverter;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.storagetype.AbstractStoragetypeJobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractStoragetypeJobManager implements Runnable{
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractStoragetypeJobManager.class);
	
	@Autowired
	private Map<String, AbstractStoragetypeJobProcessor> storagetypeJobProcessorMap;
	
	@Autowired
	private ActionAttributeConverter actionAttributeConverter;
	
	// Not thread safe - so ensure the subclass is prototype scoped
	private List<StorageJob> storageJobList;

	public List<StorageJob> getStorageJobList() {
		return storageJobList;
	}

	public void setStorageJobList(List<StorageJob> storageJobList) {
		this.storageJobList = storageJobList;
	}

//	TODO : How to enforce that invokeAction is called without the below as run() for Tape will spawn threads which eventually needs to call invokeAction()
//	public void run() {
//		StorageTypeJob storagetypeJob = selectStorageTypeJob();
//		invokeAction(storagetypeJob);
//    }
//	
//	protected abstract StorageTypeJob selectStorageTypeJob();
	
	protected ArchiveResponse process(StoragetypeJob storagejob){
		Job job = null;
		ArchiveResponse archiveResponse = null;
		try {
	//		job = storagejob.getJob();
	//		jobUtils.updateJobInProgress(job);
			
			archiveResponse = execute(storagejob);
			
	//		jobUtils.updateJobCompleted(job);
		}catch (Throwable e) {
	//		jobUtils.updateJobFailed(job);
			e.printStackTrace();
		}
		return archiveResponse;
	}

	private ArchiveResponse execute(StoragetypeJob storagetypeJob) throws Throwable {
		Integer storagetaskActionId = storagetypeJob.getStorageJob().getJob().getStoragetaskActionId();
		Action storagetaskAction = actionAttributeConverter.convertToEntityAttribute(storagetaskActionId);
		
		Storagetype storagetype = storagetypeJob.getStorageJob().getVolume().getStoragetype();
		AbstractStoragetypeJobProcessor storagetypeJobProcessorImpl = storagetypeJobProcessorMap.get(storagetype.name() + DwaraConstants.StorageTypeJobProcessorSuffix);
		Method storageTaskMethod = storagetypeJobProcessorImpl.getClass().getMethod(storagetaskAction.name(), StoragetypeJob.class);
		ArchiveResponse archiveResponse = (ArchiveResponse) storageTaskMethod.invoke(storagetypeJobProcessorImpl, storagetypeJob);

		return archiveResponse;
	}
	
//	protected void invokeAction(StoragetypeJob storagetypeJob){
//		Integer dbData = storagetypeJob.getStorageJob().getJob().getActionRefId();
//		Action action = actionAttributeConverter.convertToEntityAttribute(dbData);
//		AbstractStoragetaskAction actionImpl = storagetaskActionMap.get(action.name());
//		
//		try {
//			logger.debug("\t\tcalling storage task impl " + action.name());
//			actionImpl.process(storagetypeJob);
//		} catch (Throwable e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	protected abstract void preExecution();
//	
//	protected abstract void format();
	
//	public void manage(List<Job> archiveJobsList);
//		based on format
//		call formatspecificArchiver...
//	}

//	protected abstract void postExecution();
}
