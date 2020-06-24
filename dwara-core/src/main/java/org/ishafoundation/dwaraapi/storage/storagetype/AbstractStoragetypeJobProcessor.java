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
		logger.debug(this.getClass().getName());
	}
	
    protected void beforeFormat(StoragetypeJob storageJob) {}
    
	//public ArchiveResponse restore(StorageJob storageJob) throws Throwable{
	public ArchiveResponse format(StoragetypeJob storageJob) throws Throwable{
		ArchiveResponse ar = null;
    	beforeFormat(storageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storageJob);
    	ar = iStoragelevel.format(storageJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storageJob);
//    	ar = storageFormatter.restore(storageJob);
    	afterFormat(storageJob);
    	return ar; 
   	
    }
	
	protected void afterFormat(StoragetypeJob storageJob) {}

	
    protected void beforeWrite(StoragetypeJob storageJob) {}
    
    public ArchiveResponse write(StoragetypeJob storageJob) throws Throwable{
    	ArchiveResponse ar = null;
    	beforeWrite(storageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storageJob);
    	ar = iStoragelevel.write(storageJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storageJob);
//    	ar = storageFormatter.write(storageJob);
    	afterWrite(storageJob);
    	return ar; 
    }
    
    protected void afterWrite(StoragetypeJob storageJob) {}
    
    // TODO Should we force this to be implemented or let it be overwritten
//    protected abstract void afterWrite(StorageTypeJob storageJob);
//
//	protected abstract void beforeWrite(StorageTypeJob storageJob);

    protected void beforeVerify(StoragetypeJob storageJob) {}
    
	//public ArchiveResponse restore(StorageJob storageJob) throws Throwable{
	public ArchiveResponse verify(StoragetypeJob storageJob) throws Throwable{
		ArchiveResponse ar = null;
    	beforeVerify(storageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storageJob);
    	ar = iStoragelevel.verify(storageJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storageJob);
//    	ar = storageFormatter.restore(storageJob);
    	afterVerify(storageJob);
    	return ar; 
   	
    }
	
	protected void afterVerify(StoragetypeJob storageJob) {}
    
    protected void beforeFinalize(StoragetypeJob storageJob) {}
    
	//public ArchiveResponse restore(StorageJob storageJob) throws Throwable{
	public ArchiveResponse finalize(StoragetypeJob storageJob) throws Throwable{
		ArchiveResponse ar = null;
    	beforeFinalize(storageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storageJob);
    	ar = iStoragelevel.finalize(storageJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storageJob);
//    	ar = storageFormatter.restore(storageJob);
    	afterFinalize(storageJob);
    	return ar; 
   	
    }
	
	protected void afterFinalize(StoragetypeJob storageJob) {}


    protected void beforeRestore(StoragetypeJob storageJob) {}
    
	//public ArchiveResponse restore(StorageJob storageJob) throws Throwable{
	public ArchiveResponse restore(StoragetypeJob storageJob) throws Throwable{
		ArchiveResponse ar = null;
    	beforeRestore(storageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storageJob);
    	ar = iStoragelevel.restore(storageJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storageJob);
//    	ar = storageFormatter.restore(storageJob);
    	afterRestore(storageJob);
    	return ar; 
   	
    }
	
	protected void afterRestore(StoragetypeJob storageJob) {}
	
	private IStoragelevel getStoragelevelImpl(StoragetypeJob storageJob){
		Storagelevel storagelevel = storageJob.getStorageJob().getVolume().getStoragelevel();
		return islMap.get(storagelevel.name()+DwaraConstants.StoragelevelSuffix);//+"Storagelevel");
	}
	
//	protected abstract void afterRestore(StorageTypeJob storageJob);
//
//	protected abstract void beforeRestore(StorageTypeJob storageJob);

}
