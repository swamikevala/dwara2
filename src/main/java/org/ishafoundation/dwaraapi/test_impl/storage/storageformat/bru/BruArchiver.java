package org.ishafoundation.dwaraapi.test_impl.storage.storageformat.bru;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ishafoundation.dwaraapi.storage.StorageFormatFactory;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storageformat.AbstractStorageFormatArchiver;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchivedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "dev | stage" })
public class BruArchiver extends AbstractStorageFormatArchiver {
    static {
    	StorageFormatFactory.register("BRU", BruArchiver.class);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(BruArchiver.class);
    
	@Override
	protected ArchiveResponse archive(StorageJob storageJob) throws Exception {
		String libraryPrefixPath = storageJob.getLibraryPrefixPath();
		String libraryToBeWritten = storageJob.getLibraryToBeCopied();
		
		
		return createArchiveResponse(libraryPrefixPath, libraryToBeWritten);
	}

	@Override
	protected ArchiveResponse restore(StorageJob storageJob) throws Exception {
		String filePathNameToBeRestored = storageJob.getFilePathname();
		String destinationPath = storageJob.getDestinationPath();

		logger.trace("Creating the directory " + destinationPath + ", if not already present");
		FileUtils.forceMkdir(new java.io.File(destinationPath));
		
		java.io.File outputFile = new java.io.File(destinationPath + java.io.File.separator + FilenameUtils.getName(filePathNameToBeRestored));
		FileUtils.write(outputFile, "some dummmy content");

		return createArchiveResponseForRestore(filePathNameToBeRestored);
	}
	
	private ArchiveResponse createArchiveResponse(String libraryPrefixPath, String libraryToBeWritten){
		ArchiveResponse ar = new ArchiveResponse();

		List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();
		Collection<java.io.File> bruedFileList = FileUtils.listFiles(new java.io.File(libraryPrefixPath + java.io.File.separator + libraryToBeWritten), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (java.io.File file : bruedFileList) {
			ArchivedFile af = new ArchivedFile();
			af.setBlockNumber(1);
			af.setFilePathName(file.getAbsolutePath().replace(libraryPrefixPath + java.io.File.separator , ""));
			archivedFileList.add(af);
		}

		ar.setArchivedFileList(archivedFileList);
		return ar;
	}
	
	private ArchiveResponse createArchiveResponseForRestore(String filePathNameToBeRestored){
		ArchiveResponse ar = new ArchiveResponse();

		List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();
		ArchivedFile af = new ArchivedFile();
		af.setBlockNumber(1);
		af.setFilePathName(filePathNameToBeRestored);
		archivedFileList.add(af);
		ar.setArchivedFileList(archivedFileList);
		return ar;
	}
	
//	@Override
//	readlabel(){
//		// bru specific label reading
//	}
}
