package org.ishafoundation.dwaraapi.storage.storagetask;


import java.io.File;
import java.util.List;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
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
	private JobDao jobDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private Configuration configuration;
	
	public String getRestoreLocation(Job job) {
		String restoreLocation = null;
	
		Request request = job.getRequest();
		RequestDetails requestDetails = request.getDetails();
		org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
		if(requestedAction == Action.restore || (requestedAction == Action.restore_process && DwaraConstants.RESTORE_AND_VERIFY_FLOW_NAME.equals(requestDetails.getFlowName()))) {
			String destinationPath = requestDetails.getDestinationPath();//requested destination path
			String outputFolder = requestDetails.getOutputFolder();
			restoreLocation = destinationPath + java.io.File.separator + outputFolder + java.io.File.separator + configuration.getRestoreInProgressFileIdentifier();
		}
		else if(requestedAction == Action.ingest || requestedAction == Action.restore_process)
			restoreLocation = configuration.getRestoreTmpLocationForVerification() + File.separator + "job-" + job.getId();
		
		return restoreLocation;
	}

	@Override
	public StorageJob buildStorageJob(Job job) throws Exception {
		Request request = job.getRequest();
		Action requestedAction = request.getActionId();

		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		
		if(requestedAction == Action.ingest) {
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
			
			
			// TODO - Need to take this out storageJob.setRestoreVerify(verify);
			
			Integer inputArtifactId = job.getInputArtifactId();
			// Domain
			Domain domain = null;
			Artifact artifact = null;
	    	Domain[] domains = Domain.values();
   		
    		for (Domain nthDomain : domains) {
    			artifact = domainUtil.getDomainSpecificArtifact(nthDomain, inputArtifactId);
    			if(artifact != null) {
    				domain = nthDomain;
    				break;
    			}
			}
			storageJob.setDomain(domain);
			
			
			FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
			org.ishafoundation.dwaraapi.db.model.transactional.domain.File artifactFile = domainSpecificFileRepository.findByPathname(artifact.getName());

			// what need to be restored
			int fileIdToBeRestored = artifactFile.getId();
			storageJob.setFileId(fileIdToBeRestored);
			
			FileVolume fileVolume  = getFileVolume(domain, fileIdToBeRestored, volumeId);
			if(fileVolume == null)
				throw new Exception("Not able to retrieve filevolume record for domain " + domain + " filedId " + fileIdToBeRestored + " volumeId " + volumeId);
	
			storageJob.setVolumeBlock(fileVolume.getVolumeBlock()); 
			storageJob.setArchiveBlock(fileVolume.getArchiveBlock());
			
			// to where
			String targetLocationPath = getRestoreLocation(job);
			storageJob.setTargetLocationPath(targetLocationPath);
		}
		else {
		
			// Domain
			Domain domain = domainUtil.getDomain(request);
			storageJob.setDomain(domain);
			
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
			
			if(requestedAction == Action.restore || (requestedAction == Action.restore_process && DwaraConstants.RESTORE_AND_VERIFY_FLOW_NAME.equals(requestDetails.getFlowName()))) {
//			if(requestedAction == Action.restore && !storageJob.isRestoreVerify()) {
				String destinationPath = requestDetails.getDestinationPath();//requested destination path
				storageJob.setDestinationPath(destinationPath);
				String outputFolder = requestDetails.getOutputFolder();
				storageJob.setOutputFolder(outputFolder);
				String targetLocationPath = getRestoreLocation(job);
				storageJob.setTargetLocationPath(targetLocationPath);
			}
			else if(requestedAction == Action.restore_process) {
				String targetLocationPath = getRestoreLocation(job);
				storageJob.setTargetLocationPath(targetLocationPath);
			}
		}
		
		
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
	
	private FileVolume getFileVolume(Domain domain, int fileIdToBeRestored, String volumeId) {
    	@SuppressWarnings("unchecked")
		FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
    	return domainSpecificFileVolumeRepository.findByIdFileIdAndIdVolumeId(fileIdToBeRestored, volumeId);
	}
}
