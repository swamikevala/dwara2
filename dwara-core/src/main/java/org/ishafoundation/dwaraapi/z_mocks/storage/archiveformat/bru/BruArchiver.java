package org.ishafoundation.dwaraapi.z_mocks.storage.archiveformat.bru;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.BruResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.BruResponseParser;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.components.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("bru"+DwaraConstants.ARCHIVER_SUFFIX)
@Profile({ "dev | stage" })
public class BruArchiver implements IArchiveformatter {

	private static final Logger logger = LoggerFactory.getLogger(BruArchiver.class);
	
	@Override
	public ArchiveResponse write(String artifactSourcePath,
			int blockSizeInKB, String deviceName, String artifactNameToBeWritten) throws Exception {
		logger.debug(this.getClass().getName() + " Bru write " +  deviceName + " :: " + artifactNameToBeWritten);
		
		return createArchiveResponse("bru", artifactNameToBeWritten);
	}

	@Override
	public ArchiveResponse restore(String destinationPath,
			int blockSizeInKB, String deviceName, Integer noOfBlocksToBeRead, Integer skipByteCount,
			String filePathNameToBeRestored) throws Exception {
		logger.debug("Bru read");
		return new ArchiveResponse();
	}

	private ArchiveResponse createArchiveResponse(String archive, String artifactName) throws Exception{
		
		String testArtifactName = StringUtils.substringBeforeLast(artifactName,"_");
		URL fileUrl = this.getClass().getResource("/responses/" + archive + "/ingest-response/" + testArtifactName + ".txt");
		String testResponseFileAsString = FileUtils.readFileToString(new java.io.File(fileUrl.getFile()));
		testResponseFileAsString = testResponseFileAsString.replaceAll(testArtifactName, artifactName);
		//String jkl = FileUtils.readFileToString(new java.io.File("C:\\Users\\prakash\\src-code\\dwara2-dev\\dwara-service\\src\\test\\resources\\responses\\bru\\ingest-response\\Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9.txt"));//fileUrl.getFile()));
		
		BruResponseParser bruResponseParser = new BruResponseParser();
		BruResponse bruResponse = bruResponseParser.parseBruResponse(testResponseFileAsString);

		
		ArchiveResponse archiveResponse = convertBruResponseToArchiveResponse(bruResponse);

		return archiveResponse;
	}
	
	private ArchiveResponse convertBruResponseToArchiveResponse(BruResponse br){
		ArchiveResponse ar = new ArchiveResponse();
		List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();
		List<File> bruedFileList = br.getFileList();
		for (Iterator<File> iterator = bruedFileList.iterator(); iterator.hasNext();) {
			File bruedFile = (File) iterator.next();
			ArchivedFile af = new ArchivedFile();
			af.setBlockNumber(bruedFile.getBlockNumber() + 1); // +1, because for some reason bru "copy" responds with -1 block for BOT, while bru "t - table of contents"/"x - extraction" shows the block as 0 for same. Also while seek +1 followed by t/x returns faster results...
			af.setFilePathName(bruedFile.getFileName());
			
			archivedFileList.add(af);
		}
		ar.setArchivedFileList(archivedFileList);
		return ar;
	}
}
