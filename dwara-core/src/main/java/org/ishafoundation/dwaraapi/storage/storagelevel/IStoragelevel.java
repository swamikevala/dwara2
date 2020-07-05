package org.ishafoundation.dwaraapi.storage.storagelevel;

import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;

public interface IStoragelevel {
	
	public StorageResponse format(StoragetypeJob job);
	
	public StorageResponse write(StoragetypeJob job);
	
	public StorageResponse verify(StoragetypeJob job);
	
	public StorageResponse finalize(StoragetypeJob job);

	public StorageResponse restore(StoragetypeJob job);
}
