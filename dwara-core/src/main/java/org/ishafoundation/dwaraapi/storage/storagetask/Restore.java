package org.ishafoundation.dwaraapi.storage.storagetask;


import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("restore")
//@Profile({ "!dev & !stage" })
public class Restore extends AbstractStoragetaskAction{

    private static final Logger logger = LoggerFactory.getLogger(Restore.class);
    
	@Autowired
	private VolumeDao volumeDao;
	
	@Override
	public StorageJob buildStorageJob(Job job) {
		Request request = job.getRequest();
		org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		
		// What type of job
//		storageJob.setAction(Action.restore);
//		
//		// what
//		int fileIdToBeRestored = subrequest.getBody().getFileId();
//		storageJob.setFileId(fileIdToBeRestored);
//		
//		// From where
		Integer locationId = request.getDetails().getLocation_id();
		if(locationId == null) {
			locationId = 123;//getdefault();
		}

			
			
//		//request.getCopyNumber(); 
////			String requestedBy = request.getUser()
////			int userId = userDao.findByName(requestedBy).getId();
//		
//		int userId = request.getUserId();
//
//		V_RestoreFile v_RestoreFile = v_RestoreFileDao.findByVolumesetLocationAndIdFileIdAndIdActionAndIdUserId(copyNumber, fileIdToBeRestored, Action.restore, userId);			
//
//		storageJob.setFilePathname(v_RestoreFile.getFilePathname());
//
		String uId = "V5A001";//v_RestoreFile.getId().getVolumeId();
		Volume volume = volumeDao.findByUid(uId);
//		Volume volume = new Volume();
//		volume.setVolume(volume);
		storageJob.setVolume(volume);
//
//		int block = v_RestoreFile.getFileVolumeBlock();
//		storageJob.setBlock(block);
//		
//		int offset = v_RestoreFile.getFileVolumeOffset();
//		storageJob.setOffset(offset);
//
//		// to where
//		String targetLocation = request.getTargetvolume().getPath();
		if(requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.restore) {
//			DestinationPath = requested destination path 
		}
		else {//if(action == org.ishafoundation.dwaraapi.enumreferences.Action.restore_process || action == org.ishafoundation.dwaraapi.enumreferences.Action.process) {
//			DestinationPath = inputlc.path_prefix
		}
//		storageJob.setDestinationPath(targetLocation + java.io.File.separator + request.getOutputFolder());
//		
//		// how
//		//storageJob.setOptimizeVolumeAccess(true); // TODO hardcoded for phase1
//		storageJob.setEncrypted(v_RestoreFile.isFileVolumeEncrypted());				
//		//storageJob.setPriority(10);  // TODO hardcoded for phase1subrequest.getPriority());
//		Volumeset volumeset = volume.getVolumeset();
//		Storageformat storageformat = volumeset.getStorageformat();
//		storageJob.setStorageformat(storageformat);
		return storageJob;
	}
}
