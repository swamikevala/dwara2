package org.ishafoundation.dwaraapi.z_mocks.storage.storageformat.bru;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.storage.StorageformatFactory;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storageformat.AbstractStorageformatArchiver;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchivedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "dev | stage" })
public class BruArchiver extends AbstractStorageformatArchiver {
    static {
    	StorageformatFactory.register("BRU", BruArchiver.class);
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
		Libraryclass libraryclass = storageJob.getJob().getSubrequest().getRequest().getLibraryclass();
		String pathPrefix = null;
		if(libraryclass == null)// will be null for restore
			pathPrefix =  "/data/ingested";// "C:\\data\\ingested";// 
		else
			pathPrefix = libraryclass.getPathPrefix(); // will not be null for read after write...
		
		java.io.File srcDir = new java.io.File(pathPrefix + java.io.File.separator + FilenameUtils.getName(filePathNameToBeRestored));
		java.io.File destDir = new java.io.File(destinationPath);
		FileUtils.copyDirectoryToDirectory(srcDir, destDir);

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
