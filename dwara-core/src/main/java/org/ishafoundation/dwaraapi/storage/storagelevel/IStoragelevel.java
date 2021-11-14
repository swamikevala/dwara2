package org.ishafoundation.dwaraapi.storage.storagelevel;

import org.ishafoundation.dwaraapi.exception.StorageException;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;

public interface IStoragelevel {
	
	public StorageResponse copy(SelectedStorageJob job) throws Exception;
	
	public StorageResponse initialize(SelectedStorageJob job) throws Exception;
	
	public StorageResponse write(SelectedStorageJob job) throws StorageException;
		
	public StorageResponse finalize(SelectedStorageJob job) throws Exception;

	public StorageResponse restore(SelectedStorageJob job) throws StorageException;
}
