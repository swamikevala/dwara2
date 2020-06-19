package org.ishafoundation.dwaraapi.storage.storagetype.thread;

import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.StoragetaskAttributeConverter;
import org.ishafoundation.dwaraapi.enumreferences.Storagetask;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetask;
import org.ishafoundation.dwaraapi.storage.storagetask.StoragetaskFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public abstract class AbstractStoragetypeJobManager implements Runnable{

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private StoragetaskAttributeConverter storagetaskAttributeConverter;
	
	@Autowired
	private Map<String, AbstractStoragetask> storagetaskMap;

	
	// Not thread safe - so ensure the subclass is prototype scoped
	private List<StorageJob> storageJobList;

	public List<StorageJob> getStorageJobList() {
		return storageJobList;
	}

	public void setStorageJobList(List<StorageJob> storageJobList) {
		this.storageJobList = storageJobList;
	}

//	TODO : How to enforce that invokeStoragetask is called without the below as run() for Tape will spawn threads which eventually needs to call invokeStoragetask()
//	public void run() {
//		StorageTypeJob storagetypeJob = selectStorageTypeJob();
//		invokeStoragetask(storagetypeJob);
//    }
//	
//	protected abstract StorageTypeJob selectStorageTypeJob();

	protected void invokeStoragetask(StoragetypeJob storagetypeJob){
		Integer dbData = storagetypeJob.getStorageJob().getJob().getStoragetaskId();
		Storagetask storagetask = storagetaskAttributeConverter.convertToEntityAttribute(dbData);
		org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetask storagetaskImpl = StoragetaskFactory.getInstance(applicationContext, storagetask.name());
		
		storagetaskImpl = storagetaskMap.get(storagetask.name());
		
		try {
			System.out.println("\t\tcalling storage task impl " + storagetask.name());
			storagetaskImpl.process(storagetypeJob);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//	protected abstract void preExecution();
//	
//	protected abstract void format();
	
//	public void manage(List<Job> archiveJobsList);
//		based on format
//		call formatspecificArchiver...
//	}

//	protected abstract void postExecution();
}
