package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("tar"+DwaraConstants.ArchiverSuffix)
//@Profile({ "!dev & !stage" })
public class TarArchiver implements IArchiveformatter {

	@Override
	public ArchiveResponse write(String artifactSourcePath,
			int blockSizeInKB, String deviceName, String artifactNameToBeWritten) throws Exception {
		System.out.println(this.getClass().getName() + " Tar write " +  deviceName + " :: " + artifactNameToBeWritten);
		return new ArchiveResponse();
	}

	@Override
	public ArchiveResponse restore(String destinationPath,
			int blockSizeInKB, String deviceName, Integer noOfBlocksToBeRead, Integer skipByteCount,
			String filePathNameToBeRestored) throws Exception {
		System.out.println("Tar read");
		return new ArchiveResponse();
	}}
