package org.ishafoundation.dwaraapi.storage.archiveformat.bru;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("bru"+DwaraConstants.ARCHIVER_SUFFIX)
@Profile({ "dev | stage" })
public class MockBruArchiver extends AbstractBruArchiver{

	private static final Logger logger = LoggerFactory.getLogger(MockBruArchiver.class);

	@Override
	protected String executeWriteCommand(List<String> writeCommandParamsList, String artifactName, int volumeBlocksize) throws Exception {
		logger.trace("Simulating command execution inside mock bru archiver");
		String testArtifactName = StringUtils.substringBeforeLast(artifactName,"_");
		InputStream inputStream = this.getClass().getResourceAsStream("/responses/bru/ingest-response/" + volumeBlocksize + "/" +  testArtifactName + ".txt");
		String testResponseFileAsString = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
		testResponseFileAsString = testResponseFileAsString.replaceAll(testArtifactName, artifactName);

		return testResponseFileAsString;	
	}

	@Override
	protected String executeRestoreCommand(List<String> restoreCommandParamsList) throws Exception {

		//"cd " + destinationPath + " ; " + "bru -B -xvvvvvvvvv -QV -b " + volumeBlocksize + " -f " + deviceName + " " + filePathNameToBeRestored
		
		String command = restoreCommandParamsList.get(2).trim();
		String destinationPath = StringUtils.substringBetween(command, "cd ", ";").trim();
		String filePathNameToBeRestored = StringUtils.substringAfterLast(command, " ").trim();
		//TODO : Hardcoded
		String readyToIngestPath =  "C:\\data\\ingested";
		if(StringUtils.isBlank(FilenameUtils.getExtension(filePathNameToBeRestored))) { //if file is folder
			FileUtils.copyDirectoryToDirectory(new File(readyToIngestPath + java.io.File.separator + filePathNameToBeRestored), new File(destinationPath));
		}
		else
			FileUtils.copyFile(new File(readyToIngestPath + java.io.File.separator + filePathNameToBeRestored), new File(destinationPath + java.io.File.separator + filePathNameToBeRestored));
		return null;
	}
	
}
