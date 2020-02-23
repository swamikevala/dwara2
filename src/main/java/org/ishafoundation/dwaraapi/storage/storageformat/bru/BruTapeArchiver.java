package org.ishafoundation.dwaraapi.storage.storageformat.bru;

import org.ishafoundation.dwaraapi.storage.storageformat.ArchiveResponse;

public class BruTapeArchiver extends BruArchiver {

	// TODO ??? is bru command for tape different from other storage types...
	
	
	@Override
	protected ArchiveResponse archive() {
		// TODO frames bru command for tape, 
		executeCommand();
		// TODO Handle both success and error scenarios
		return null;
	}

	@Override
	protected ArchiveResponse restore() {
		// TODO frames bru command for restore for tape 
		executeCommand();
		// TODO Handle both success and error scenarios
		return null;
	}

}
