package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("tar"+DwaraConstants.ARCHIVER_SUFFIX)
@Profile({ "dev | stage" })
public class MockTarArchiver extends TarArchiver {
	
	private static final Logger logger = LoggerFactory.getLogger(MockTarArchiver.class);

	@Override
	protected String executeCommand(List<String> tarCommandParamsList, String artifactName, int volumeBlocksize)
			throws Exception {
		logger.trace("Simulating command execution inside mock tar archiver");
		String testArtifactName = StringUtils.substringBeforeLast(artifactName,"_");
		URL fileUrl = this.getClass().getResource("/responses/tar/ingest-response/"  + volumeBlocksize + "/" + testArtifactName + ".txt");
		String testResponseFileAsString = FileUtils.readFileToString(new java.io.File(fileUrl.getFile()));
		testResponseFileAsString = testResponseFileAsString.replaceAll(testArtifactName, artifactName);
		return testResponseFileAsString;
	}

	@Override
	protected boolean stream(List<String> commandList, int volumeBlocksize, int skipByteCount,
			String filePathNameWeNeed, boolean toBeRestored, String destinationPath, boolean toBeVerified, Checksumtype checksumtype,
			HashMap<String, byte[]> filePathNameToChecksumObj) throws Exception {

		if (toBeRestored) {
			if (toBeVerified) { // Restore with checksum validation
				logger.trace("Simulating restore and verify");
			} else {
				logger.trace("Simulating restore without checksum validation");
			}
				
		} else if (!toBeRestored && toBeVerified) {// Just on the fly checksum validation...
			logger.trace("Simulating checksum validation without restoring");
		}
		
		logger.debug("buffersize - " +  volumeBlocksize);
		logger.debug("Will be skipping - " +  skipByteCount);
		
		return true;
	}

}
