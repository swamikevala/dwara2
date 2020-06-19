package org.ishafoundation.dwaraapi.storage.storagetask;


import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.enumreferences.Storagetask;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.storagetype.AbstractStoragetypeJobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("write")
//@Profile({ "!dev & !stage" })
public class Write extends AbstractStoragetask{

    private static final Logger logger = LoggerFactory.getLogger(Write.class);
    
	@Autowired
	private Map<String, AbstractStoragetypeJobProcessor> storagetypeJobProcessorMap;
	
	@Override
	public ArchiveResponse execute(StoragetypeJob storagetypeJob) throws Throwable {
		System.out.println(this.getClass().getName() + " write storagetask ");
		Storagetype storagetype = storagetypeJob.getStorageJob().getVolume().getStoragetype();
		//ArchiveResponse archiveResponse = stjpMap.get(storagetype.name() + "JobProcessor").write(storagejob);
		ArchiveResponse archiveResponse = storagetypeJobProcessorMap.get(storagetype.name() + DwaraConstants.StorageTypeJobProcessorSuffix).write(storagetypeJob);
		return archiveResponse;
	}




//	@Autowired
//	private FileVolumeDao fileVolumeDao;	
//	
//    @Autowired
//    private FileDao fileDao;	
//    
//    @Autowired
//    private LibraryVolumeDao libraryVolumeDao;
//	
//	@Autowired
//	private ApplicationContext applicationContext;
//
//	public Write() {
////		this.storagetask = Storagetask.write;
//	}
//	
//	@Override
//	public ArchiveResponse execute(StorageJob storagejob) throws Throwable {
//		
//		String storagetype = storagejob.getStoragetype().name();
//		StoragetypeJobProcessor storagetypeJobProcessor = StoragetypeJobProcessorFactory.getInstance(applicationContext, storagetype);
//
//		ArchiveResponse archiveResponse = storagetypeJobProcessor.write(storagejob); 
//
//		
//		// TODO - This should be domain specific
//		logger.trace("Processing job using storage type - " + storagetype);
//		// Update tape specific DB - File_Tape...
//		// TODO : Check If for Mezz copy libraryId = MezzLibraryId coming from input...
//		Library library = storagejob.getLibrary();
//		int libraryId = library.getId();
//		Volume volume = storagejob.getVolume();
//		// Get a map of Paths and their File1 Ids
//		List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File1> libraryFileList = fileDao.findAllByLibraryId(libraryId);
//		
//		HashMap<Integer, String> fileIdToPath = new HashMap<Integer, String>();
//		HashMap<String, Integer> filePathToId = new HashMap<String, Integer>();
//		HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File1> filePathNameTofileObj = new HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.domain.File1>();
//		for (Iterator<org.ishafoundation.dwaraapi.db.model.transactional.domain.File1> iterator = libraryFileList.iterator(); iterator.hasNext();) {
//			org.ishafoundation.dwaraapi.db.model.transactional.domain.File1 nthFile = iterator.next();
//			filePathNameTofileObj.put(nthFile.getPathname(), nthFile);
//			fileIdToPath.put(nthFile.getId(), nthFile.getPathname());
//		}
//
//		int libraryBlock = 0;
//		//  some tape specific code like updating db file_tape and tapedrive...
//		List<ArchivedFile> archivedFileList = archiveResponse.getArchivedFileList();
//		List<FileVolume> toBeAddedFileVolumeTableEntries = new ArrayList<FileVolume>();
//		for (Iterator<ArchivedFile> iterator = archivedFileList.iterator(); iterator.hasNext();) {
//			ArchivedFile archivedFile = (ArchivedFile) iterator.next();
//			String fileName = archivedFile.getFilePathName(); 
//			
//			// TODO get the file id from File1 table using this fileName...
//			org.ishafoundation.dwaraapi.db.model.transactional.domain.File1 file = null;
//			if(filePathNameTofileObj != null)
//				file = filePathNameTofileObj.get(fileName);
//
//			//logger.trace(filePathNameTofileObj.toString());
//			logger.trace(fileName);
//			if(file == null) {
//				logger.trace("Junk folder not supposed to be written to tape. Filter it. Skipping its info getting added to DB");
//				continue;
//			}
//			
//			if(fileName.equals(library.getName()))
//				libraryBlock = archivedFile.getBlockNumber();
//			
//			FileVolume ft = new FileVolume(file, volume);
//			ft.setBlock(archivedFile.getBlockNumber());
//			//ft.setOffset(offset); // TODO ?? How
//			toBeAddedFileVolumeTableEntries.add(ft);
//		}
//		
//	    if(toBeAddedFileVolumeTableEntries.size() > 0) {
//	    	logger.debug("DB FileVolume entries Creation");   
//	    	fileVolumeDao.saveAll(toBeAddedFileVolumeTableEntries);
//	    	logger.debug("DB FileVolume entries Creation - Success");
//	    }
//	    
//	    LibraryVolume libraryVolume = new LibraryVolume(library, volume);
//	    libraryVolume.setBlock(libraryBlock);
//	    libraryVolume.setEncrypted(storagejob.isEncrypted());
//	    logger.debug("DB LibraryVolume Creation");
//	    libraryVolumeDao.save(libraryVolume);
//	    logger.debug("DB LibraryVolume Creation - Success");
//	    
//	    archiveResponse.setLibraryName(library.getName());
//	    archiveResponse.setLibraryBlockNumber(libraryBlock);
//	    return archiveResponse;
//
//	}
}
