package org.ishafoundation.dwaraapi.storage.storageformat;

import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.tape.label.TapeLabelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractStorageFormatArchiver {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractStorageFormatArchiver.class);
	
	@Autowired
	private TapeLabelManager tapeLabelManager;
	
	public boolean format(StorageJob storageJob) throws Exception{
		boolean isSuccess = mount(storageJob);
		if(isSuccess)
			isSuccess = label(storageJob);
		
		return isSuccess;
	}
	
	public ArchiveResponse write(StorageJob storageJob) throws Exception{
		logger.trace("will be writing " + storageJob.getLibraryPrefixPath() + "!-!" + storageJob.getLibraryToBeCopied());
		// dbUpdates
		ArchiveResponse ar = archive(storageJob);
		
		// dbupdates using the ArchiveResponse recd..
		return ar;
	}
	
	public ArchiveResponse read(StorageJob storageJob) throws Exception{
		logger.trace("will be reading " + storageJob.getFilePathname());
		// dbUpdates
		ArchiveResponse ar = restore(storageJob);
		// dbupdates
		return ar;
	}
	
	protected abstract ArchiveResponse archive(StorageJob storageJob) throws Exception;
	
	protected abstract ArchiveResponse restore(StorageJob storageJob) throws Exception;
	
	// can be overridden by Specific formats like LTFS
	protected boolean mount(StorageJob storageJob) throws Exception{
		return true;
	}
	
	// labeling is not format specific hence logic goes here...
	protected boolean label(StorageJob storageJob) throws Exception{
		boolean isSuccess = false;
		
		isSuccess = tapeLabelManager.writeVolumeHeaderLabelSet(storageJob.getVolume().getTape().getBarcode(), storageJob.getStorageformat().getName(), storageJob.getDeviceWwid());
		return isSuccess;
	}
}
