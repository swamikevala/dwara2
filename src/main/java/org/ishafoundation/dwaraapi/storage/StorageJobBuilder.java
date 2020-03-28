package org.ishafoundation.dwaraapi.storage;

import org.springframework.stereotype.Component;

@Component
public class StorageJobBuilder {
//	
//	@Autowired
//	private RequesttypeDao requesttypeDao;
//		
//	@Autowired
//	private CopyDao copyDao;
//	
//	@Autowired
//	private LibraryclassDao libraryclassDao;
//	
//	@Autowired
//	private RequestDao requestDao;
//
//	@Autowired
//	private SubrequestDao subrequestDao;
//	
//	@Autowired
//	private LibraryDao libraryDao;
//
//	@Autowired
//	private TapesetDao tapesetDao;
//	
//	@Autowired
//	private StorageformatDao storageformatDao;
//	
//	@Autowired
//	private TapeDao tapeDao;
//	
//	@Autowired
//	private FileTapeDao fileTapeDao;
//
//	@Autowired
//	private FileDao fileDao;
//	
//	@Autowired
//	private LibraryTapeDao libraryTapeDao;
//	
//	@Autowired
//	private TargetvolumeDao targetvolumeDao;
//	
//	
//	public StorageJob buildStorageJob(Job job) {
//		StorageJob storageJob = null;
//		int taskId = job.getTaskId();
//		
//		int subrequestId = job.getSubrequestId();
//		Subrequest subrequest = subrequestDao.findById(subrequestId).get();
//		
//		int requestId = subrequest.getRequestId();
//		Request request = requestDao.findById(requestId).get(); // TODO : Cache the call...
//
//		int requesttypeId = request.getRequesttypeId();
//		Requesttype requesttype = requesttypeDao.findById(requesttypeId).get();
//		String requesttypeName = requesttype.getName();
//		// TODO : Hardcoded requesttype
//		if(requesttypeName.equals("INGEST")) {
//			Copy copy = copyDao.findByTaskId(taskId);
//			if (copy != null) { // means its a copy job...
//				// TODO - Lazy loading of archivejob... just get all the information only when the job is picked for processing. Why load all info upfront only to get discarded.
//				
//
//				int libraryId = job.getInputLibraryId(); // for a dependent copy job - the source to copy is different and the input library id comes from the prerequisite task
//				Library library = libraryDao.findById(libraryId).get();
//				String libraryName = library.getName();			
//				int libraryclassId = library.getLibraryclassId();
//				Libraryclass copyTaskLibraryclass = libraryclassDao.findById(libraryclassId).get();	
//				
//				String librarypathToBeCopied = copyTaskLibraryclass.getPath() + java.io.File.separator + libraryName;
//				
//				double sizeOfTheLibraryToBeWritten = FileUtils.sizeOfDirectory(new java.io.File(librarypathToBeCopied)); 
//				
//				storageJob = new StorageJob();
//				storageJob.setJob(job);
//				
//				// what type of job
//				storageJob.setStorageOperation(StorageOperation.WRITE);
//				
//				// what needs to be copied
//				storageJob.setLibraryId(libraryId);
//				storageJob.setLibraryPrefixPath(copyTaskLibraryclass.getPath());
//				storageJob.setLibraryToBeCopied(libraryName);
//
//				// to where
//				storageJob.setStoragetype(getStoragetype());
//				storageJob.setVolume(getVolume(copy, sizeOfTheLibraryToBeWritten));
//				storageJob.setStorageformat(getStorageformat(copy));
//				storageJob.setCopyNumber(copy.getCopyNumber());
//				
//				// how
//				storageJob.setConcurrentCopies(copyTaskLibraryclass.isConcurrentCopies());
//				storageJob.setEncrypted(copy.isEncrypted());
//				storageJob.setOptimizeTapeAccess(subrequest.isOptimizeTapeAccess()); // TODO : For ingest will this come from request?
//				storageJob.setPriority(subrequest.getPriority());
//				//storageJob.setNoFileRecords(copyTaskLibraryclass.isNoFileRecords());
//			}
//		}
//		else if(requesttypeName.equals("RESTORE")) { // TODO Hardcoded
//			storageJob = new StorageJob();
//			storageJob.setJob(job);
//			
//			// What type of job
//			storageJob.setStorageOperation(StorageOperation.READ);
//
//			// what
//			int fileIdToBeRestored = subrequest.getFileId();
//			storageJob.setFileId(fileIdToBeRestored);
//			
//			// From where
//			int copyNumber = request.getCopyNumber(); 
//			File file = fileDao.findById(fileIdToBeRestored).get();
//			storageJob.setFilePathname(file.getPathname());
//			int libraryId = file.getLibraryId(); 
//
//			// TODO : Assumes storagetype as tape. Need to revisit....
//			LibraryTape libraryTape = libraryTapeDao.findByLibraryIdAndCopyNumber(libraryId, copyNumber);
//			int tapeId = libraryTape.getTapeId();
//			Tape tape = tapeDao.findById(tapeId).get();
//			
//			Volume volume = new Volume();
//			volume.setTape(tape);
//			storageJob.setVolume(volume);
//			FileTape fileOnTape = fileTapeDao.findByFileIdAndTapeId(fileIdToBeRestored, tape.getTapeId());
//			int block = fileOnTape.getBlock();
//			storageJob.setBlock(block);
//			int offset = fileOnTape.getOffset();
//			storageJob.setOffset(offset);
//
//			// to where
//			Targetvolume targetvolume = targetvolumeDao.findById(request.getTargetvolumeId()).get();
//			String targetLocation = targetvolume.getName();
//			storageJob.setDestinationPath(targetLocation + java.io.File.separator + request.getOutputFolder());
//			
//			// how
//			storageJob.setOptimizeTapeAccess(subrequest.isOptimizeTapeAccess());
//			storageJob.setEncrypted(libraryTape.isEncrypted());				
//			storageJob.setPriority(subrequest.getPriority());
//			int tapesetId = tape.getTapesetId();
//			Tapeset tapeset = tapesetDao.findById(tapesetId).get();
//			Storageformat storageformat = storageformatDao.findById(tapeset.getStorageformatId()).get();
//			storageJob.setStorageformat(storageformat);
//		}
//		return storageJob;
//	}
//
//	private Storagetype getStoragetype(){
//		// TODO Assumes storagetype is tape. For now its ok. Need to extend...
//		Storagetype storagetype = new Storagetype();
//		storagetype.setStoragetypeId(1);
//		storagetype.setName("TAPE");
//		storagetype.setSequential(true);
//		return storagetype;
//	}
//	
//	private Volume getVolume(Copy copy, double sizeOfTheLibraryToBeWritten) {
//		int tapesetId = copy.getTapesetId();
//		Tape tape = chooseATape(tapesetId, sizeOfTheLibraryToBeWritten);
//		Volume volume = new Volume();
//		volume.setTape(tape);
//		return volume;
//	}
//	
//	private Tape chooseATape(int tapesetId, double sizeOfTheLibraryToBeWritten) {
//		List<Tape> writableTapesList = tapeDao.getWritableTapes(tapesetId);
//		Tape toBeUsedTape = null;
//		for (Iterator<Tape> iterator = writableTapesList.iterator(); iterator.hasNext();) {
//			Tape tape = (Tape) iterator.next();
//			//TODO if(Tape.getFree() > sizeOfTheLibraryToBeWritten) {
//				toBeUsedTape = tape; // for now defaulting to first one...
//				break;
//			//}
//		}
//		return toBeUsedTape;		
//	}
//	
//	// TODO : DB Check storageformat is not tape specific and so should move out of tapeset...
//	private Storageformat getStorageformat(Copy copy) {
//		int tapesetId = copy.getTapesetId();
//		Tapeset tapeset = tapesetDao.findById(tapesetId).get();
//		Storageformat storageformat = storageformatDao.findById(tapeset.getStorageformatId()).get();
//		return storageformat;
//	}
//
//
}
