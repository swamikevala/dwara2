package org.ishafoundation.dwaraapi.storage.archiveformat.bru;

import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.storagetype.thread.AbstractStoragetypeJobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("bru"+DwaraConstants.ARCHIVER_SUFFIX)
@Profile({ "!dev & !stage" })
public class BruArchiver implements IArchiveformatter {

	private static final Logger logger = LoggerFactory.getLogger(BruArchiver.class);
	// Just framebru command here and dont overwrite the write();
	
	@Override
	public ArchiveResponse write(String artifactSourcePath,
			int blockSizeInKB, String deviceName, String artifactNameToBeWritten) throws Exception {
		logger.debug(this.getClass().getName() + " Bru write " +  deviceName + " :: " + artifactNameToBeWritten);

		
		return new ArchiveResponse();
	}

	@Override
	public ArchiveResponse restore(String destinationPath,
			int blockSizeInKB, String deviceName, Integer noOfBlocksToBeRead, Integer skipByteCount,
			String filePathNameToBeRestored) throws Exception {
		logger.debug("Bru read");
		return new ArchiveResponse();
	}}
