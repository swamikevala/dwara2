package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.storagelevel.FileStoragelevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("tar"+DwaraConstants.ArchiverSuffix)
//@Profile({ "!dev & !stage" })
public class TarArchiver implements IArchiveformatter {
	
	private static final Logger logger = LoggerFactory.getLogger(TarArchiver.class);

	@Override
	public ArchiveResponse write(String artifactSourcePath,
			int blockSizeInKB, String deviceName, String artifactNameToBeWritten) throws Exception {
		logger.debug(this.getClass().getName() + " Tar write " +  deviceName + " :: " + artifactNameToBeWritten);
		return new ArchiveResponse();
	}

	@Override
	public ArchiveResponse restore(String destinationPath,
			int blockSizeInKB, String deviceName, Integer noOfBlocksToBeRead, Integer skipByteCount,
			String filePathNameToBeRestored) throws Exception {
		logger.debug("Tar read");
		return new ArchiveResponse();
	}}
