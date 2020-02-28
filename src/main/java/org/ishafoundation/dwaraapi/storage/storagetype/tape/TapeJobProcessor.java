package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.constants.TapedriveStatus;
import org.ishafoundation.dwaraapi.db.dao.TapedriveDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileTapeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.LibraryTapeDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.db.model.transactional.FileTape;
import org.ishafoundation.dwaraapi.db.model.transactional.LibraryTape;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storageformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.storageformat.bru.response.components.File;
import org.ishafoundation.dwaraapi.storage.storagetype.StorageTypeJobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeJobProcessor extends StorageTypeJobProcessor {
//    static {
//    	CopyFactory.register("TAPE", TapeJobProcessor.class);
//    }
	
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
		// Delegate the format specific archiving to AbstractStorageFormatArchiver
		ArchiveResponse archiveResponse = super.write(storageJob);
		
		logger.trace("Processing job using storage type - TAPE. DB updates specific to Tape goes in here");

		// Update tape specific DB - File_Tape...
		if(!storageJob.isNoFileRecords()) { // For prev proxy copy there isnt any entry to file_tape
			// TODO : Check If for Mezz copy libraryId = MezzLibraryId coming from input...
			int libraryId = storageJob.getLibraryId();
			int tapeId = storageJob.getVolume().getTape().getTapeId();
			// Get a map of Paths and their File Ids
			List<org.ishafoundation.dwaraapi.db.model.transactional.File> libraryFileList = fileDao.findAllByLibraryId(libraryId);
			
			HashMap<Integer, String> fileIdToPath = new HashMap<Integer, String>();
			HashMap<String, Integer> filePathToId = new HashMap<String, Integer>();
			for (Iterator<org.ishafoundation.dwaraapi.db.model.transactional.File> iterator = libraryFileList.iterator(); iterator.hasNext();) {
				org.ishafoundation.dwaraapi.db.model.transactional.File nthFile = iterator.next();
				filePathToId.put(nthFile.getPathname(), nthFile.getFileId());
				fileIdToPath.put(nthFile.getFileId(), nthFile.getPathname());
			}
			
			//  some tape specific code like updating db file_tape and tapedrive...
			List<File> fileList = archiveResponse.getFileList();
			List<FileTape> toBeAddedFileTapeTableEntries = new ArrayList<FileTape>();
			for (Iterator<File> iterator = fileList.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
	
				
				FileTape ft = new FileTape();
				ft.setBlock(file.getBlockNumber());
	
				String fileName = file.getFileName(); 
				// TODO get the file id from File table using this fileName...
				int fileId = filePathToId.get(fileName);
				ft.setFileId(fileId);
				//ft.setOffset(offset); // TODO ?? How
				ft.setTapeId(tapeId);
				
				toBeAddedFileTapeTableEntries.add(ft);
			}
			
		    if(toBeAddedFileTapeTableEntries.size() > 0) {
		    	logger.debug("DB FileTape entries Creation");   
		    	fileTapeDao.saveAll(toBeAddedFileTapeTableEntries);
		    	logger.debug("DB FileTape entries Creation - Success");
		    }
		    
		    LibraryTape libraryTape = new LibraryTape();
		    libraryTape.setTapeId(tapeId);
		    libraryTape.setLibraryId(libraryId);
		    libraryTape.setEncrypted(storageJob.isEncrypted());
		    libraryTape.setCopyNumber(storageJob.getCopyNumber());
		    logger.debug("DB LibraryTape Creation");
		    libraryTapeDao.save(libraryTape);
		    logger.debug("DB LibraryTape Creation - Success");
		}

		Tapedrive tapedrive = tapedriveDao.findByElementAddress(storageJob.getDriveNo());
		tapedrive.setStatus(TapedriveStatus.AVAILABLE.toString());
		logger.debug("DB Tapedrive Updation " + tapedrive.getStatus());
		tapedriveDao.save(tapedrive);
		logger.debug("DB Tapedrive Updation - Success");

		
		return archiveResponse;
	}

	@Override
	public ArchiveResponse read(StorageJob archiveJob) throws Throwable {
		ArchiveResponse archiveResponse = super.read(archiveJob);
		return archiveResponse;
	}
}
