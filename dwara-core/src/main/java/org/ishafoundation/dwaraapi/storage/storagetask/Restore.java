package org.ishafoundation.dwaraapi.storage.storagetask;


import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.master.LocationDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
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
	private LocationDao locationDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	
	@Override
	public StorageJob buildStorageJob(Job job) {
		Request request = job.getRequest();
		org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);

		// Domain
		Domain domain = request.getDomain();
		storageJob.setDomain(domain);
		
		// what need to be restored
		int fileIdToBeRestored = request.getDetails().getFile_id();
		storageJob.setFileId(fileIdToBeRestored);
		
		// From where - get the volume
		Integer locationId = request.getDetails().getLocation_id();
		if(locationId == null) {
			Location location = locationDao.findByRestoreDefaultTrue();
			locationId = location.getId();
		}

		FileVolume fileVolume  = getFileVolume(domain, fileIdToBeRestored, locationId);
		storageJob.setVolumeBlock(fileVolume.getVolumeBlock()); 
		storageJob.setArchiveBlock(fileVolume.getArchiveBlock());
		
		Volume volume = fileVolume.getVolume(); // need the volume for job selection
		storageJob.setVolume(volume);


		String destinationPath = null;
		if(requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.restore) {
			destinationPath = request.getDetails().getDestinationpath();//requested destination path 
		}
		else {//if(action == org.ishafoundation.dwaraapi.enumreferences.Action.restore_process || action == org.ishafoundation.dwaraapi.enumreferences.Action.process) {
//			destinationPath = inputlc.path_prefix
		}
		storageJob.setDestinationPath(destinationPath + java.io.File.separator + request.getDetails().getOutput_folder());
		return storageJob;
	}
	

	private FileVolume getFileVolume(Domain domain, int fileIdToBeRestored, Integer locationId) {
    	@SuppressWarnings("unchecked")
		FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
    	return domainSpecificFileVolumeRepository.findByIdFileIdAndVolumeLocationId(fileIdToBeRestored, locationId);
	}
}
