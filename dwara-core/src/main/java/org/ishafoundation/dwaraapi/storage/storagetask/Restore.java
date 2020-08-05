package org.ishafoundation.dwaraapi.storage.storagetask;


import org.ishafoundation.dwaraapi.db.dao.master.LocationDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
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
	public StorageJob buildStorageJob(Job job) throws Exception {
		Request request = job.getRequest();
		org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);

		// Domain
		Domain domain = request.getDomain();
		storageJob.setDomain(domain);
		
		RequestDetails requestDetails = request.getDetails();
		// what need to be restored
		int fileIdToBeRestored = requestDetails.getFile_id();
		storageJob.setFileId(fileIdToBeRestored);
		
		// From where - get the volume
		Integer locationId = requestDetails.getLocation_id();
		if(locationId == null) {
			Location location = locationDao.findByRestoreDefaultTrue();
			locationId = location.getId();
		}

		FileVolume fileVolume  = getFileVolume(domain, fileIdToBeRestored, locationId);
		if(fileVolume == null)
			throw new Exception("Not able to retrieve filevolume record for domain " + domain + " filedId " + fileIdToBeRestored + " location " + locationId);
		storageJob.setVolumeBlock(fileVolume.getVolumeBlock()); 
		storageJob.setArchiveBlock(fileVolume.getArchiveBlock());
		
		Volume volume = fileVolume.getVolume(); // need the volume for job selection
		storageJob.setVolume(volume);

		Boolean verify = requestDetails.getVerify();
		if(verify == null)
			verify = volume.getArchiveformat().isRestoreVerify();
		storageJob.setRestoreVerify(verify);
		
		String destinationPath = null;
		if(requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.restore) {
			destinationPath = requestDetails.getDestinationpath();//requested destination path 
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
