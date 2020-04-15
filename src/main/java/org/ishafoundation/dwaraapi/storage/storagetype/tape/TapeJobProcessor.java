package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.constants.TapedriveStatus;
import org.ishafoundation.dwaraapi.db.dao.master.TapedriveDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.FileTapeDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.LibraryTapeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileTape;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.LibraryTape;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.storagetype.StorageTypeJobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeJobProcessor extends StorageTypeJobProcessor {

	private static final Logger logger = LoggerFactory.getLogger(TapeJobProcessor.class);
	
	@Autowired
	private TapedriveDao tapedriveDao;
	
	@Autowired
	private FileTapeDao fileTapeDao;	
	
    @Autowired
    private FileDao fileDao;	
    
    @Autowired
    private LibraryTapeDao libraryTapeDao;

	// load the tape onto drive and position it - NOTE: can be done here or on the tape task
	// now check on the format
	// call the formatmanager accordingly and run the write/read command
	@Override
	public ArchiveResponse write(StorageJob storageJob) throws Throwable {
		try {
			// Delegate the format specific archiving to AbstractStorageFormatArchiver
			ArchiveResponse archiveResponse = super.write(storageJob);
		
			logger.trace("Processing job using storage type - TAPE. DB updates specific to Tape goes in here");
	
			// Update tape specific DB - File_Tape...
			// TODO : Check If for Mezz copy libraryId = MezzLibraryId coming from input...
			Library library = storageJob.getLibrary();
			int libraryId = library.getId();
			Tape tape = storageJob.getVolume().getTape();
			int tapeId = tape.getId();
			// Get a map of Paths and their File Ids
			List<org.ishafoundation.dwaraapi.db.model.transactional.File> libraryFileList = fileDao.findAllByLibraryId(libraryId);
			
			HashMap<Integer, String> fileIdToPath = new HashMap<Integer, String>();
			HashMap<String, Integer> filePathToId = new HashMap<String, Integer>();
			HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File> filePathNameTofileObj = new HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File>();
			for (Iterator<org.ishafoundation.dwaraapi.db.model.transactional.File> iterator = libraryFileList.iterator(); iterator.hasNext();) {
				org.ishafoundation.dwaraapi.db.model.transactional.File nthFile = iterator.next();
				filePathNameTofileObj.put(nthFile.getPathname(), nthFile);
				fileIdToPath.put(nthFile.getId(), nthFile.getPathname());
			}
			
			//  some tape specific code like updating db file_tape and tapedrive...
			List<ArchivedFile> archivedFileList = archiveResponse.getArchivedFileList();
			List<FileTape> toBeAddedFileTapeTableEntries = new ArrayList<FileTape>();
			for (Iterator<ArchivedFile> iterator = archivedFileList.iterator(); iterator.hasNext();) {
				ArchivedFile archivedFile = (ArchivedFile) iterator.next();
				String fileName = archivedFile.getFilePathName(); 
				// TODO get the file id from File table using this fileName...
				org.ishafoundation.dwaraapi.db.model.transactional.File file = null;
				if(filePathNameTofileObj != null)
					file = filePathNameTofileObj.get(fileName);

				logger.trace(filePathNameTofileObj.toString());
				logger.trace(fileName);
				if(file == null) {
					logger.trace("Junk folder not supposed to be written to tape. Filter it. Skipping its info getting added to DB");
					continue;
				}
				FileTape ft = new FileTape(file, tape);
				ft.setBlock(archivedFile.getBlockNumber());
				//ft.setOffset(offset); // TODO ?? How
				toBeAddedFileTapeTableEntries.add(ft);
			}
			
		    if(toBeAddedFileTapeTableEntries.size() > 0) {
		    	logger.debug("DB FileTape entries Creation");   
		    	fileTapeDao.saveAll(toBeAddedFileTapeTableEntries);
		    	logger.debug("DB FileTape entries Creation - Success");
		    }
		    
		    LibraryTape libraryTape = new LibraryTape(library, tape);
		    libraryTape.setBlock(0); // TODO Hardcoded
		    libraryTape.setEncrypted(storageJob.isEncrypted());
		    logger.debug("DB LibraryTape Creation");
		    libraryTapeDao.save(libraryTape);
		    logger.debug("DB LibraryTape Creation - Success");
		    return archiveResponse;
		}
		finally {
			updateTapeDriveTable(storageJob);
		}
	}
	
	@Override
	public ArchiveResponse read(StorageJob storageJob) throws Throwable {
		try {
			ArchiveResponse archiveResponse = super.read(storageJob);
			return archiveResponse;
		}
		finally {
			updateTapeDriveTable(storageJob);
		}
	}
	
	private void updateTapeDriveTable(StorageJob storageJob) {
		Tapedrive tapedrive = tapedriveDao.findByElementAddress(storageJob.getDriveNo());
		tapedrive.setStatus(TapedriveStatus.AVAILABLE.toString());
		logger.debug("DB Tapedrive Updation " + tapedrive.getStatus());
		tapedriveDao.save(tapedrive);
		logger.debug("DB Tapedrive Updation - Success");
	}
	

	
}
