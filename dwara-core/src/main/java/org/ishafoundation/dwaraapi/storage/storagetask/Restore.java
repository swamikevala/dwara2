package org.ishafoundation.dwaraapi.storage.storagetask;


import java.io.File;
import java.util.List;

import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ArtifactVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.FileVolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.ishafoundation.dwaraapi.enumreferences.Priority;
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
	private JobDao jobDao;
	
	@Autowired
	private ArtifactDao artifactDao;
	
	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private ArtifactVolumeDao artifactVolumeDao;
	
	@Autowired
	private FileVolumeDao fileVolumeDao;
	
	@Autowired
	private Configuration configuration;
	
	@Override
	public String getArtifactRootLocation(Job sourceJob) {
		String restoreLocation = null;
	
		Request request = sourceJob.getRequest();
		RequestDetails requestDetails = request.getDetails();
		org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
		if(requestedAction == Action.restore || requestedAction == Action.restore_process){
			String destinationPath = requestDetails.getDestinationPath();//requested destination path
			String outputFolder = requestDetails.getOutputFolder();
			//restoreLocation = destinationPath + java.io.File.separator + outputFolder + java.io.File.separator + configuration.getRestoreInProgressFileIdentifier();
			restoreLocation = destinationPath + java.io.File.separator + configuration.getRestoreInProgressFileIdentifier() + java.io.File.separator + outputFolder;
		}
		else if(requestedAction == Action.ingest || requestedAction == Action.rewrite)
			restoreLocation = configuration.getRestoreTmpLocationForVerification() + File.separator + "job-" + sourceJob.getId();
		
		return restoreLocation;
	}

	@Override
	public StorageJob buildStorageJob(Job job) throws Exception {
		Request request = job.getRequest();
		Action requestedAction = request.getActionId();

		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		
		if(requestedAction == Action.rewrite && job.getDependencies() == null) {
			Integer inputArtifactId = job.getInputArtifactId();
			// Domain
			Artifact artifact = artifactDao.findById(inputArtifactId).get();

			
			org.ishafoundation.dwaraapi.db.model.transactional.File artifactFile = fileDao.findByPathname(artifact.getName());

			// what need to be restored
			int fileIdToBeRestored = artifactFile.getId();
			storageJob.setFileId(fileIdToBeRestored);
			
			FileVolume fileVolume = null;
			Integer goodCopy = request.getDetails().getSourceCopy(); // artifact rewrite or defective_volume has this
			if(goodCopy != null)
				fileVolume = getFileVolume(fileIdToBeRestored, goodCopy);
			else //if(request.getDetails().getPurpose() == RewritePurpose.volume_migration || request.getDetails().getPurpose() == RewritePurpose.additonal_copy)
				fileVolume = getFileVolume(fileIdToBeRestored, request.getDetails().getVolumeId());
	
			if(fileVolume == null)
				throw new Exception("Not able to retrieve filevolume record for filedId " + fileIdToBeRestored + " volumeId " + fileVolume.getVolume().getId());
	
			storageJob.setVolume(fileVolume.getVolume());
			
			storageJob.setVolumeBlock(fileVolume.getVolumeStartBlock());
			storageJob.setArchiveBlock(fileVolume.getArchiveBlock());
			
			// to where
			String targetLocationPath = getArtifactRootLocation(job);
			storageJob.setTargetLocationPath(targetLocationPath);					
			
		}
		else if(requestedAction == Action.ingest || (requestedAction == Action.rewrite && job.getDependencies() != null)) {
			// From where - get the volume
			// get restore job's dependency - can't be anything but write, but looping for making the code generic giving some flexibility
			List<Integer> dependencies = job.getDependencies();
			Job writeJob = null;
			for (Integer nthDependencyJobId : dependencies) {
				Job nthDependencyJob = jobDao.findById(nthDependencyJobId).get();
				Action storagetaskAction = nthDependencyJob.getStoragetaskActionId();
				if(storagetaskAction != null && storagetaskAction == Action.write) {
					writeJob = nthDependencyJob;
					break;
				}
			}

			if(writeJob == null)
				throw new Exception("Restore' dependency job is not a write job. Currently not supported in Dwara. Please ensure the dependency is mapped properly in flowelement.dependencies");

			Volume volume = writeJob.getVolume();
			String volumeId = volume.getId();
			storageJob.setVolume(volume);
			
			Integer inputArtifactId = job.getInputArtifactId();
			// Domain
			Artifact artifact = artifactDao.findById(inputArtifactId).get();
			
			org.ishafoundation.dwaraapi.db.model.transactional.File artifactFile = fileDao.findByPathname(artifact.getName());

			// what need to be restored
			int fileIdToBeRestored = artifactFile.getId();
			storageJob.setFileId(fileIdToBeRestored);
			
			FileVolume fileVolume  = getFileVolume(fileIdToBeRestored, volumeId);
			if(fileVolume == null)
				throw new Exception("Not able to retrieve filevolume record for filedId " + fileIdToBeRestored + " volumeId " + volumeId);
	
			storageJob.setVolumeBlock(fileVolume.getVolumeStartBlock());
			storageJob.setArchiveBlock(fileVolume.getArchiveBlock());
			
			// to where
			String targetLocationPath = getArtifactRootLocation(job);
			storageJob.setTargetLocationPath(targetLocationPath);
		}
		else {
			RequestDetails requestDetails = request.getDetails();
			// what need to be restored
			int fileIdToBeRestored = requestDetails.getFileId();
			storageJob.setFileId(fileIdToBeRestored);
			storageJob.setTimecodeStart(requestDetails.getTimecodeStart());
			storageJob.setTimecodeEnd(requestDetails.getTimecodeEnd());
	
			// From where - get the volume
			Integer copyNumber = requestDetails.getCopyId();
			if(copyNumber == null) {
				copyNumber = 1;// TODO : we need to default it
			}
			FileVolume fileVolume  = getFileVolume(fileIdToBeRestored, copyNumber);
			if(fileVolume == null)
				throw new Exception("Not able to retrieve filevolume record for filedId " + fileIdToBeRestored + " copyNumber " + copyNumber);
	
			
	//		String locationId = requestDetails.getLocationId();
	//		if(locationId == null) {
	//			Location location = configurationTablesUtil.getDefaultLocation();
	//			locationId = location.getId();
	//		}
	
	//		FileVolume fileVolume  = getFileVolume(domain, fileIdToBeRestored, locationId);
	//		if(fileVolume == null)
	//			throw new Exception("Not able to retrieve filevolume record for domain " + domain + " filedId " + fileIdToBeRestored + " location " + locationId);
			
			storageJob.setVolumeBlock(fileVolume.getVolumeStartBlock());
			storageJob.setArchiveBlock(fileVolume.getArchiveBlock());
			
			Volume volume = fileVolume.getVolume(); // need the volume for job selection
			storageJob.setVolume(volume);
	
//			Boolean verify = requestDetails.getVerify();
//			if(verify == null)
//				verify = volume.getArchiveformat().isRestoreVerify();
//			storageJob.setRestoreVerify(verify);

			if(requestedAction == Action.restore || requestedAction == Action.restore_process){
//			if(requestedAction == Action.restore && !storageJob.isRestoreVerify()) {
				String destinationPath = requestDetails.getDestinationPath();//requested destination path
				storageJob.setDestinationPath(destinationPath);
				String outputFolder = requestDetails.getOutputFolder();
				storageJob.setOutputFolder(outputFolder);
				String targetLocationPath = getArtifactRootLocation(job);
				storageJob.setTargetLocationPath(targetLocationPath);
				
				Priority requestPriority = request.getPriority();
				if(requestPriority != null)
					storageJob.setPriority(requestPriority.getPriorityValue());
			}
//			else if(requestedAction == Action.restore_process) {
//				String targetLocationPath = getArtifactRootLocation(job);
//				storageJob.setTargetLocationPath(targetLocationPath);
//			}
		}
		
		
		return storageJob;
	}
	

//	private FileVolume getFileVolume(Domain domain, int fileIdToBeRestored, String locationId) {
//    	@SuppressWarnings("unchecked")
//		FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
//    	return domainSpecificFileVolumeRepository.findByIdFileIdAndVolumeLocationId(fileIdToBeRestored, locationId);
//	}
	
	private FileVolume getFileVolume(int fileIdToBeRestored, int copyNumber) throws Exception {
    	List<FileVolume> fileVolumeList = fileVolumeDao.findAllByIdFileIdAndVolumeGroupRefCopyId(fileIdToBeRestored, copyNumber);
    	FileVolume fileVolume = null;
    	if(fileVolumeList.size() > 1) {
    		
    		org.ishafoundation.dwaraapi.db.model.transactional.File file = fileDao.findById(fileIdToBeRestored).get();
    		Artifact artifact = file.getArtifact();
	    	for (FileVolume nthFileVolume : fileVolumeList) {
				ArtifactVolume artifactVolume = artifactVolumeDao.findByIdArtifactIdAndIdVolumeId(artifact.getId(), nthFileVolume.getId().getVolumeId());
				if(artifactVolume.getStatus() == ArtifactVolumeStatus.current || artifactVolume.getStatus() == null) {
					fileVolume = nthFileVolume;
					break;
				}
			}
    	}
    	else {
    		fileVolume = fileVolumeList.get(0);
    	}
    	return fileVolume;
	}
	
	private FileVolume getFileVolume(int fileIdToBeRestored, String volumeId) {
    	return fileVolumeDao.findByIdFileIdAndIdVolumeId(fileIdToBeRestored, volumeId);
	}
}
