package org.ishafoundation.dwaraapi.storage.storageformat.zip;

import org.ishafoundation.dwaraapi.storage.StorageFormatFactory;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storageformat.AbstractStorageFormatArchiver;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchiveResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZipArchiver extends AbstractStorageFormatArchiver {
    static {
    	StorageFormatFactory.register("ZIP", ZipArchiver.class);
    }
    private static final Logger logger = LoggerFactory.getLogger(ZipArchiver.class);
    
	@Override
	protected ArchiveResponse archive(StorageJob storageJob) {
		// TODO frames zip command, executes, parsesitsresponse and returns it back
		executeCommand();
		// TODO Handle both success and error scenarios
		return null;
	}

	@Override
	protected ArchiveResponse restore(StorageJob storageJob) {
		// TODO frames zip command for restore, executes, parsesitsresponse and returns it back
		executeCommand();
		// TODO Handle both success and error scenarios
		return null;
	}

	private void executeCommand(){
		// common method for both ingest and restore
		logger.trace("Archiving using ZipArchiver");
		// the methods frame the command and delegate it to this method
		// executes the command, parses the response and returns it back..
	}
}
