package org.ishafoundation.dwaraapi.storage.storagetype;

import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.enumreferences.Storagelevel;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.storagelevel.IStoragelevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public abstract class AbstractStoragetypeJobProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractStoragetypeJobProcessor.class);
	
	@Autowired
	private ApplicationContext applicationContext;	
	
	@Autowired
//	private IStoragelevel iStoragelevel;
	private Map<String, IStoragelevel> islMap;

	public AbstractStoragetypeJobProcessor() {
		System.out.println(this.getClass().getName());
	}
	
    public ArchiveResponse write(StoragetypeJob storageJob) throws Throwable{
    	ArchiveResponse ar = null;
    	beforeWrite(storageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelObj(storageJob);
    	ar = iStoragelevel.write(storageJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storageJob);
//    	ar = storageFormatter.write(storageJob);
    	afterWrite(storageJob);
    	return ar; 
    }
    
    // TODO Should we force this to be implemented or let it over
//    protected abstract void afterWrite(StorageTypeJob storageJob);
//
//	protected abstract void beforeWrite(StorageTypeJob storageJob);

    protected void  beforeWrite(StoragetypeJob storageJob) {}
    protected void  afterWrite(StoragetypeJob storageJob) {}
    
	//public ArchiveResponse restore(StorageJob storageJob) throws Throwable{
	public ArchiveResponse restore(StoragetypeJob storageJob) throws Throwable{
		ArchiveResponse ar = null;
    	beforeRestore(storageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelObj(storageJob);
    	ar = iStoragelevel.restore(storageJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storageJob);
//    	ar = storageFormatter.restore(storageJob);
    	afterRestore(storageJob);
    	return ar; 
   	
    }
	
	private IStoragelevel getStoragelevelObj(StoragetypeJob storageJob){
		Storagelevel storagelevel = storageJob.getStorageJob().getVolume().getStoragelevel();
		return islMap.get(storagelevel.name()+DwaraConstants.StoragelevelSuffix);//+"Storagelevel");
	}
	
//	protected abstract void afterRestore(StorageTypeJob storageJob);
//
//	protected abstract void beforeRestore(StorageTypeJob storageJob);
    protected void  beforeRestore(StoragetypeJob storageJob) {}
    protected void  afterRestore(StoragetypeJob storageJob) {}

}
