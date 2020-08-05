package org.ishafoundation.dwaraapi.storage.storagelevel;

import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;

public interface IStoragelevel {
	
	public StorageResponse format(SelectedStorageJob job) throws Exception;
	
	public StorageResponse write(SelectedStorageJob job) throws Exception;
	
	public StorageResponse verify(SelectedStorageJob job) throws Exception;
	
	public StorageResponse finalize(SelectedStorageJob job) throws Exception;

	public StorageResponse restore(SelectedStorageJob job) throws Exception;
}
