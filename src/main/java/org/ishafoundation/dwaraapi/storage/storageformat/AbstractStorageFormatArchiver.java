package org.ishafoundation.dwaraapi.storage.storageformat;

import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStorageFormatArchiver {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractStorageFormatArchiver.class);
	
	public ArchiveResponse write(StorageJob storageJob){
		logger.trace("will be writing " + storageJob.getLibraryPrefixPath() + "!-!" + storageJob.getLibraryToBeCopied());
		// dbUpdates
		ArchiveResponse ar = archive(storageJob);
		
		// dbupdates using the ArchiveResponse recd..
		return ar;
	}
	
	public ArchiveResponse read(StorageJob storageJob){
		logger.trace("will be reading " + storageJob.getFilePathname());
		// dbUpdates
		ArchiveResponse ar = restore(storageJob);
		// dbupdates
		return ar;
	}
	
	protected abstract ArchiveResponse archive(StorageJob storageJob);
	
	protected abstract ArchiveResponse restore(StorageJob storageJob);
}
