package org.ishafoundation.dwaraapi.storage.storageformat.ltfs;

import org.ishafoundation.dwaraapi.storage.StorageformatFactory;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storageformat.AbstractStorageformatArchiver;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchiveResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LTFSArchiver extends AbstractStorageformatArchiver {
    static {
    	StorageformatFactory.register("LTFS", LTFSArchiver.class);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(LTFSArchiver.class);
	@Override
	protected ArchiveResponse archive(StorageJob storageJob) {
		// TODO frames LTFS command, executes, parsesitsresponse and returns it back
		executeCommand();
		// TODO Handle both success and error scenarios
		return null;
	}

	@Override
	protected ArchiveResponse restore(StorageJob storageJob) {
		// TODO frames LTFS command for restore, executes, parsesitsresponse and returns it back
		executeCommand();
		// TODO Handle both success and error scenarios
		return null;
	}

	private void executeCommand(){
		// common method for both ingest and restore
		logger.trace("Archiving using LTFSArchiver");
		// the methods frame the command and delegate it to this method
		// executes the command, parses the response and returns it back..
	}
}
