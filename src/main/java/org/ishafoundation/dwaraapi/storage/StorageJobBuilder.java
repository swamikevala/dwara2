package org.ishafoundation.dwaraapi.storage;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.db.dao.master.LibraryclassDao;
import org.ishafoundation.dwaraapi.db.dao.master.RequesttypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.StorageformatDao;
import org.ishafoundation.dwaraapi.db.dao.master.TapeDao;
import org.ishafoundation.dwaraapi.db.dao.master.TapesetDao;
import org.ishafoundation.dwaraapi.db.dao.master.TargetvolumeDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.FileTapeDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.LibraryTapeDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.LibraryclassTapesetDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.LibraryDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.Storageformat;
import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.master.Tapeset;
import org.ishafoundation.dwaraapi.db.model.master.Targetvolume;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassTapeset;
import org.ishafoundation.dwaraapi.db.model.transactional.File;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileTape;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.LibraryTape;
import org.ishafoundation.dwaraapi.model.Storagetype;
import org.ishafoundation.dwaraapi.model.Volume;
import org.ishafoundation.dwaraapi.storage.constants.StorageOperation;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageJobBuilder {
	
	@Autowired
	private RequesttypeDao requesttypeDao;
		
	@Autowired
	private LibraryclassTapesetDao libraryclassTapesetDao;
	
	@Autowired
	private LibraryclassDao libraryclassDao;
	
	@Autowired
	private RequestDao requestDao;

	@Autowired
	private SubrequestDao subrequestDao;
	
	@Autowired
	private LibraryDao libraryDao;

	@Autowired
	private TapesetDao tapesetDao;
	
	@Autowired
	private StorageformatDao storageformatDao;
	
	@Autowired
	private TapeDao tapeDao;
	
	@Autowired
	private FileTapeDao fileTapeDao;

	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private LibraryTapeDao libraryTapeDao;
	
	@Autowired
	private TargetvolumeDao targetvolumeDao;
	
	
	public StorageJob buildStorageJob(Job job) {
		StorageJob storageJob = null;
		int taskId = job.getTask().getId();
		Subrequest subrequest = job.getSubrequest();
		Request request = subrequest.getRequest();
		org.ishafoundation.dwaraapi.constants.Requesttype requestType = request.getRequesttype();
		if(requestType == org.ishafoundation.dwaraapi.constants.Requesttype.ingest) {
			LibraryclassTapeset libraryclassTapeset = libraryclassTapesetDao.findByTaskId(taskId);
			if (libraryclassTapeset != null) { // means its a libraryclassTapeset job...
				// TODO - Lazy loading of archivejob... just get all the information only when the job is picked for processing. Why load all info upfront only to get discarded.
				
				// for a dependent libraryclassTapeset job - the source to libraryclassTapeset is different and the input library id comes from the prerequisite task
				Library library = job.getInputLibrary();
				String libraryName = library.getName();			
				Libraryclass libraryclassTapesetTaskLibraryclass = library.getLibraryclass();	
				String librarypathToBeCopied = libraryclassTapesetTaskLibraryclass.getPath() + java.io.File.separator + libraryName;
				
				double sizeOfTheLibraryToBeWritten = FileUtils.sizeOfDirectory(new java.io.File(librarypathToBeCopied)); 
				
				storageJob = new StorageJob();
				storageJob.setJob(job);
				
				// what type of job
				storageJob.setStorageOperation(StorageOperation.WRITE);
				
				// what needs to be copied
				storageJob.setLibrary(library);
				storageJob.setLibraryPrefixPath(libraryclassTapesetTaskLibraryclass.getPath());
				storageJob.setLibraryToBeCopied(libraryName);

				// to where
				storageJob.setStoragetype(getStoragetype());
				storageJob.setVolume(getVolume(libraryclassTapeset, sizeOfTheLibraryToBeWritten));
				storageJob.setStorageformat(getStorageformat(libraryclassTapeset));
				storageJob.setCopyNumber(libraryclassTapeset.getCopyNumber());
				
				// how
				storageJob.setConcurrentCopies(libraryclassTapesetTaskLibraryclass.isConcurrentCopies());
				storageJob.setEncrypted(libraryclassTapeset.isEncrypted());
			}
		}
		else if(requestType == org.ishafoundation.dwaraapi.constants.Requesttype.restore) {
			storageJob = new StorageJob();
			storageJob.setJob(job);
			
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
//			
//			int libraryId = file.getLibrary().getId(); 
//			// TODO : Assumes storagetype as tape. Need to revisit....
//			LibraryTape libraryTape = libraryTapeDao.findByLibraryIdAndCopyNumber(libraryId, copyNumber);
//			Tape tape = libraryTape.getTape();
//			
//			Volume volume = new Volume();
//			volume.setTape(tape);
//			storageJob.setVolume(volume);
//			
//			
//			FileTape fileOnTape = fileTapeDao.findByFileIdAndTapeId(fileIdToBeRestored, tape.getId());
//			int block = fileOnTape.getBlock();
//			storageJob.setBlock(block);
//			
//			int offset = fileOnTape.getOffset();
//			storageJob.setOffset(offset);
//
//			// to where
//			Targetvolume targetvolume = request.getTargetvolume();
//			String targetLocation = targetvolume.getName();
//			storageJob.setDestinationPath(targetLocation + java.io.File.separator + request.getOutputFolder());
//			
//			// how
//			//storageJob.setOptimizeTapeAccess(true); // TODO hardcoded for phase1
//			storageJob.setEncrypted(fileOnTape.isEncrypted()); //(libraryTape.isEncrypted());				
//			//storageJob.setPriority(10);  // TODO hardcoded for phase1subrequest.getPriority());
//
//			Tapeset tapeset = tape.getTapeset();
//			Storageformat storageformat = tapeset.getStorageformat();
//			
//			
//			
//			storageJob.setStorageformat(storageformat);
		}
		return storageJob;
	}

	private Storagetype getStoragetype(){
		// TODO Assumes storagetype is tape. For now its ok. Need to extend...
		Storagetype storagetype = new Storagetype();
		storagetype.setStoragetypeId(1);
		storagetype.setName("TAPE");
		storagetype.setSequential(true);
		return storagetype;
	}
	
	private Volume getVolume(LibraryclassTapeset libraryclassTapeset, double sizeOfTheLibraryToBeWritten) {
		int tapesetId = libraryclassTapeset.getTapeset().getId();
		Tape tape = chooseATape(tapesetId, sizeOfTheLibraryToBeWritten);
		Volume volume = new Volume();
		volume.setTape(tape);
		return volume;
	}
	
	private Tape chooseATape(int tapesetId, double sizeOfTheLibraryToBeWritten) {
		List<Tape> writableTapesList = tapeDao.findAllByTapesetIdAndFinalizedIsFalse(tapesetId);
		Tape toBeUsedTape = null;
		for (Iterator<Tape> iterator = writableTapesList.iterator(); iterator.hasNext();) {
			Tape tape = (Tape) iterator.next();
			//TODO if(Tape.getFree() > sizeOfTheLibraryToBeWritten) {
				toBeUsedTape = tape; // for now defaulting to first one...
				break;
			//}
		}
		return toBeUsedTape;		
	}
	
	// TODO : DB Check storageformat is not tape specific and so should move out of tapeset...
	private Storageformat getStorageformat(LibraryclassTapeset libraryclassTapeset) {
		return libraryclassTapeset.getTapeset().getStorageformat();
	}


}
