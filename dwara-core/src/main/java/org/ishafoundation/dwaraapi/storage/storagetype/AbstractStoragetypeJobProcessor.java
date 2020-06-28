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

public abstract class AbstractStoragetypeJobProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractStoragetypeJobProcessor.class);
	
	@Autowired
	private Map<String, IStoragelevel> islMap;

	public AbstractStoragetypeJobProcessor() {
		logger.debug(this.getClass().getName());
	}
	
    protected void beforeFormat(StoragetypeJob storagetypeJob) {}
    
	//public ArchiveResponse restore(StorageJob storagetypeJob) throws Throwable{
	public ArchiveResponse format(StoragetypeJob storagetypeJob) throws Throwable{
		ArchiveResponse ar = null;
    	beforeFormat(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	ar = iStoragelevel.format(storagetypeJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storagetypeJob);
//    	ar = storageFormatter.restore(storagetypeJob);
    	afterFormat(storagetypeJob);
    	return ar; 
   	
    }
	
	protected void afterFormat(StoragetypeJob storagetypeJob) {}

	
    protected void beforeWrite(StoragetypeJob storagetypeJob) {}
    
    public ArchiveResponse write(StoragetypeJob storagetypeJob) throws Throwable{
    	logger.info("Writing job " + storagetypeJob.getStorageJob().getJob().getId());
    	ArchiveResponse ar = null;
    	beforeWrite(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	ar = iStoragelevel.write(storagetypeJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storagetypeJob);
//    	ar = storageFormatter.write(storagetypeJob);
    	afterWrite(storagetypeJob);
    	return ar; 
    }
    
    protected void afterWrite(StoragetypeJob storagetypeJob) {}
    
    // TODO Should we force this to be implemented or let it be overwritten
//    protected abstract void afterWrite(StorageTypeJob storagetypeJob);
//
//	protected abstract void beforeWrite(StorageTypeJob storagetypeJob);

    protected void beforeVerify(StoragetypeJob storagetypeJob) {}
    
	//public ArchiveResponse restore(StorageJob storagetypeJob) throws Throwable{
	public ArchiveResponse verify(StoragetypeJob storagetypeJob) throws Throwable{
		logger.info("Verifying job " + storagetypeJob.getStorageJob().getJob().getId());
		ArchiveResponse ar = null;
    	beforeVerify(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	ar = iStoragelevel.verify(storagetypeJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storagetypeJob);
//    	ar = storageFormatter.restore(storagetypeJob);
    	afterVerify(storagetypeJob);
    	return ar; 
   	
    }
	
	protected void afterVerify(StoragetypeJob storagetypeJob) {}
    
    protected void beforeFinalize(StoragetypeJob storagetypeJob) {}
    
	//public ArchiveResponse restore(StorageJob storagetypeJob) throws Throwable{
	public ArchiveResponse finalize(StoragetypeJob storagetypeJob) throws Throwable{
		ArchiveResponse ar = null;
    	beforeFinalize(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	ar = iStoragelevel.finalize(storagetypeJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storagetypeJob);
//    	ar = storageFormatter.restore(storagetypeJob);
    	afterFinalize(storagetypeJob);
    	return ar; 
   	
    }
	
	protected void afterFinalize(StoragetypeJob storagetypeJob) {}


    protected void beforeRestore(StoragetypeJob storagetypeJob) {}
    
	//public ArchiveResponse restore(StorageJob storagetypeJob) throws Throwable{
	public ArchiveResponse restore(StoragetypeJob storagetypeJob) throws Throwable{
		logger.info("Restoring job " + storagetypeJob.getStorageJob().getJob().getId());
		ArchiveResponse ar = null;
    	beforeRestore(storagetypeJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(storagetypeJob);
    	ar = iStoragelevel.restore(storagetypeJob);
    	
//    	AbstractStorageformatArchiver storageFormatter = getStorageformatArchiver(storagetypeJob);
//    	ar = storageFormatter.restore(storagetypeJob);
    	afterRestore(storagetypeJob);
    	return ar; 
   	
    }
	
	protected void afterRestore(StoragetypeJob storagetypeJob) {}
	
	private IStoragelevel getStoragelevelImpl(StoragetypeJob storagetypeJob){
		Storagelevel storagelevel = storagetypeJob.getStorageJob().getVolume().getStoragelevel();
		return islMap.get(storagelevel.name()+DwaraConstants.STORAGELEVEL_SUFFIX);//+"Storagelevel");
	}
	
//	protected abstract void afterRestore(StorageTypeJob storagetypeJob);
//
//	protected abstract void beforeRestore(StorageTypeJob storagetypeJob);

}
