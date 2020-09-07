package org.ishafoundation.dwaraapi.storage.storagetask;


import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
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
	private DomainUtil domainUtil;
	
	@Override
	public StorageJob buildStorageJob(Job job) throws Exception {
		Request request = job.getRequest();
		org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);

		// Domain
		Domain domain = domainUtil.getDomain(request);
		storageJob.setDomain(domain);
		
		RequestDetails requestDetails = request.getDetails();
		// what need to be restored
		int fileIdToBeRestored = requestDetails.getFileId();
		storageJob.setFileId(fileIdToBeRestored);
		
		// From where - get the volume
		Integer copyNumber = requestDetails.getCopyId();
		if(copyNumber == null) {
			copyNumber = 1;// TODO : we need to default it
		}
		FileVolume fileVolume  = getFileVolume(domain, fileIdToBeRestored, copyNumber);
		if(fileVolume == null)
			throw new Exception("Not able to retrieve filevolume record for domain " + domain + " filedId " + fileIdToBeRestored + " copyNumber " + copyNumber);

		
//		String locationId = requestDetails.getLocationId();
//		if(locationId == null) {
//			Location location = configurationTablesUtil.getDefaultLocation();
//			locationId = location.getId();
//		}

//		FileVolume fileVolume  = getFileVolume(domain, fileIdToBeRestored, locationId);
//		if(fileVolume == null)
//			throw new Exception("Not able to retrieve filevolume record for domain " + domain + " filedId " + fileIdToBeRestored + " location " + locationId);
		
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
			destinationPath = requestDetails.getDestinationPath();//requested destination path 
		}
		else {//if(action == org.ishafoundation.dwaraapi.enumreferences.Action.restore_process || action == org.ishafoundation.dwaraapi.enumreferences.Action.process) {
//			destinationPath = inputlc.path_prefix
		}
		storageJob.setDestinationPath(destinationPath);
		String outputFolder = request.getDetails().getOutputFolder();
		storageJob.setOutputFolder(outputFolder);
		storageJob.setTargetLocationPath(destinationPath + java.io.File.separator + outputFolder);
		return storageJob;
	}
	

//	private FileVolume getFileVolume(Domain domain, int fileIdToBeRestored, String locationId) {
//    	@SuppressWarnings("unchecked")
//		FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
//    	return domainSpecificFileVolumeRepository.findByIdFileIdAndVolumeLocationId(fileIdToBeRestored, locationId);
//	}
	
	private FileVolume getFileVolume(Domain domain, int fileIdToBeRestored, int copyNumber) {
    	@SuppressWarnings("unchecked")
		FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
    	return domainSpecificFileVolumeRepository.findByIdFileIdAndVolumeGroupRefCopyId(fileIdToBeRestored, copyNumber);
	}
}
