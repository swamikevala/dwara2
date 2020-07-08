package org.ishafoundation.dwaraapi.storage.storagelevel;

import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;

public interface IStoragelevel {
	
	public StorageResponse format(StoragetypeJob job) throws Exception;
	
	public StorageResponse write(StoragetypeJob job) throws Exception;
	
	public StorageResponse verify(StoragetypeJob job) throws Exception;
	
	public StorageResponse finalize(StoragetypeJob job) throws Exception;

	public StorageResponse restore(StoragetypeJob job) throws Exception;
}
