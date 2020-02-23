package org.ishafoundation.dwaraapi.storage.storageformat.bru;

import org.ishafoundation.dwaraapi.storage.StorageFormatFactory;
import org.ishafoundation.dwaraapi.storage.storageformat.AbstractStorageFormatArchiver;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchiveResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BruArchiver extends AbstractStorageFormatArchiver {
    static {
    	StorageFormatFactory.register("BRU", BruArchiver.class);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(BruArchiver.class);
    
	@Override
	protected ArchiveResponse archive() {
		// TODO frames bru command, executes, parsesitsresponse and returns it back
		executeCommand();
		// TODO Handle both success and error scenarios
		return null;
	}

	@Override
	protected ArchiveResponse restore() {
		// TODO frames bru command for restore, executes, parsesitsresponse and returns it back
		executeCommand();
		// TODO Handle both success and error scenarios
		return null;
	}

	protected void executeCommand(){
		
		// common method for both ingest and restore
		logger.trace("Archiving using BruArchiver");
		// the methods frame the command and delegate it to this method
		// executes the command, parses the response and returns it back..
	}
}
