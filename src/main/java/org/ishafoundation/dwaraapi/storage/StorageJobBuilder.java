package org.ishafoundation.dwaraapi.storage;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.constants.Requesttype;
import org.ishafoundation.dwaraapi.db.dao.master.TapeDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.LibraryclassTapesetDao;
import org.ishafoundation.dwaraapi.db.dao.view.V_RestoreFileDao;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.Storageformat;
import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.master.Tapeset;
import org.ishafoundation.dwaraapi.db.model.master.Task;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassTapeset;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.db.model.view.V_RestoreFile;
import org.ishafoundation.dwaraapi.job.TaskUtils;
import org.ishafoundation.dwaraapi.model.Storagetype;
import org.ishafoundation.dwaraapi.model.Volume;
import org.ishafoundation.dwaraapi.storage.constants.StorageOperation;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageJobBuilder {
	
	@Autowired
	private TapeDao tapeDao;
	
	@Autowired
	private V_RestoreFileDao v_RestoreFileDao;
	
	@Autowired
	private LibraryclassTapesetDao libraryclassTapesetDao;
		
	@Autowired
	private TaskUtils taskUtils;
	
	public StorageJob buildStorageJob(Job job) {
		StorageJob storageJob = null;
		Task task = job.getTask();
		int taskId = task.getId();
		Subrequest subrequest = job.getSubrequest();
		Request request = subrequest.getRequest();
		org.ishafoundation.dwaraapi.constants.Requesttype requestType = request.getRequesttype();
		if(requestType == org.ishafoundation.dwaraapi.constants.Requesttype.ingest) {
			if (taskUtils.isTaskStorage(task)) { // means its a copy job...
				LibraryclassTapeset libraryclassTapeset = libraryclassTapesetDao.findByTaskId(taskId);
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
				storageJob.setCopyNumber(libraryclassTapeset.getTapeset().getCopyNumber());
				
				// how
				storageJob.setConcurrentCopies(libraryclassTapesetTaskLibraryclass.isConcurrentCopies());
				storageJob.setEncrypted(libraryclassTapeset.isEncrypted());
			}
		}
		else if(requestType == org.ishafoundation.dwaraapi.constants.Requesttype.restore) {
			storageJob = new StorageJob();
			storageJob.setJob(job);
			
			// What type of job
			storageJob.setStorageOperation(StorageOperation.READ);
			
			// what
			int fileIdToBeRestored = subrequest.getFileId();
			storageJob.setFileId(fileIdToBeRestored);
			
			// From where
			int copyNumber = request.getCopyNumber(); 
//			String requestedBy = request.getUser()
//			int userId = userDao.findByName(requestedBy).getId();
			
			int userId = request.getUserId();

			V_RestoreFile v_RestoreFile = v_RestoreFileDao.findByTapesetCopyNumberAndIdFileIdAndIdRequesttypeAndIdUserId(copyNumber, fileIdToBeRestored, Requesttype.restore, userId);			

			storageJob.setFilePathname(v_RestoreFile.getFilePathname());

			int tapeId = v_RestoreFile.getId().getTapeId();
			Tape tape = tapeDao.findById(tapeId).get();
			Volume volume = new Volume();
			volume.setTape(tape);
			storageJob.setVolume(volume);

			int block = v_RestoreFile.getFileTapeBlock();
			storageJob.setBlock(block);
			
			int offset = v_RestoreFile.getFileTapeOffset();
			storageJob.setOffset(offset);

			// to where
			String targetLocation = request.getTargetvolume().getPath();
			storageJob.setDestinationPath(targetLocation + java.io.File.separator + request.getOutputFolder());
			
			// how
			//storageJob.setOptimizeTapeAccess(true); // TODO hardcoded for phase1
			storageJob.setEncrypted(v_RestoreFile.isFileTapeEncrypted());				
			//storageJob.setPriority(10);  // TODO hardcoded for phase1subrequest.getPriority());
			Tapeset tapeset = tape.getTapeset();
			Storageformat storageformat = tapeset.getStorageformat();
			storageJob.setStorageformat(storageformat);
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
