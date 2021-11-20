package org.ishafoundation.dwaraapi.storage.storagetask;


import java.util.Map;

import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.storage.storagetype.AbstractStoragetypeJobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("rewrite")
//@Profile({ "!dev & !stage" })
public class Rewrite extends AbstractStoragetaskAction{

    private static final Logger logger = LoggerFactory.getLogger(Rewrite.class);
    
	@Autowired
	private Map<String, AbstractStoragetypeJobProcessor> storagetypeJobProcessorMap;
	
	public Rewrite() {
		storagetaskActionList.add(Action.restore);
		storagetaskActionList.add(Action.write);
		storagetaskActionList.add(Action.verify);
	}
	
//	public List<Job> createStorageTaskJobs(Subrequest subrequest, Action action){
//		List<Job> simpleActionJobs = new ArrayList<Job>();
//		Job job = new Job();
//		
//		int actionId = actionAttributeConverter.convertToDatabaseColumn(action);
//		job.setActionId(actionId);
//		job.setSubrequest(subrequest);
//		// TODO needed only for sub actions - job.setInputArtifactId(subrequest.getDetails().getArtifact_id());
//		job.setCreatedAt(LocalDateTime.now());
//		job.setStatus(Status.queued);
//		if(action == Action.restore) {
//			//subrequest
//			Volume volume = null;
//			//getvolume related stuff from the file here...
//			job.setVolume(volume);
//		}
//		job = jobUtils.saveJob(job);
//		
//		simpleActionJobs.add(job);
//
//		return simpleActionJobs;
//	}
	





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
////		this.action = Action.write;
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
//		List<org.ishafoundation.dwaraapi.db.model.transactional.File1> libraryFileList = fileDao.findAllByLibraryId(libraryId);
//		
//		HashMap<Integer, String> fileIdToPath = new HashMap<Integer, String>();
//		HashMap<String, Integer> filePathToId = new HashMap<String, Integer>();
//		HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File1> filePathNameTofileObj = new HashMap<String, org.ishafoundation.dwaraapi.db.model.transactional.File1>();
//		for (Iterator<org.ishafoundation.dwaraapi.db.model.transactional.File1> iterator = libraryFileList.iterator(); iterator.hasNext();) {
//			org.ishafoundation.dwaraapi.db.model.transactional.File1 nthFile = iterator.next();
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
//			org.ishafoundation.dwaraapi.db.model.transactional.File1 file = null;
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
