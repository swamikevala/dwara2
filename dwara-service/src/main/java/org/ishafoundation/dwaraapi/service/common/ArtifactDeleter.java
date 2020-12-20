package org.ishafoundation.dwaraapi.service.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepositoryUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.videopub.mam.MamUpdateTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArtifactDeleter {
	
	private static final Logger logger = LoggerFactory.getLogger(ArtifactDeleter.class);

	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private FileRepositoryUtil fileRepositoryUtil;

	@Autowired
	private MamUpdateTaskExecutor mamUpdateTaskExecutor;


	public void validateArtifactclass(String artifactclassId){
    	// Commenting out for now
//		if(!artifactclassId.equals("video-digitization-pub")) {
//			String errorMsg = "Only deletion of artifactclass video-digitization-pub is supported.";
//        	logger.error(errorMsg);
//        	throw new DwaraException(errorMsg);
//		}
    }
    
	public void validateRequest(Request request){
    	if(request.getActionId() != Action.ingest) {
    		String errorMsg = "Right now Cancel/Delete is only supported for " + Action.ingest;
	    	logger.error(errorMsg);
	    	throw new DwaraException(errorMsg); 
	    }
   	
    	Status requestStatus = request.getStatus();
    	if(requestStatus == Status.in_progress) { // dont allow IN_PROGRESS artifacts
    		String errorMsg = "Only Requests/Artifacts that are not in IN_PROGRESS state can be deleted. Please wait till the jobs are complete first before deleting";
        	logger.error(errorMsg);
        	throw new DwaraException(errorMsg); 
    	}
	}
    
	public void validateJobsAndUpdateStatus(Request request){
    	int requestId = request.getId();
		List<Job> jobList = jobDao.findAllByRequestId(requestId);
		for (Job job : jobList) {
			if(job.getStatus() == Status.in_progress) {
	    		String errorMsg = "Only Requests/Artifacts that are not having any jobs running(in IN_PROGRESS state) can be deleted. Please wait till the jobs are complete first before deleting";
	        	logger.error(errorMsg);
	        	throw new DwaraException(errorMsg); 
			}
			else if(job.getStatus() == Status.on_hold || job.getStatus() == Status.queued)
	    		job.setStatus(Status.cancelled);
		}
		
		// Step 2 - Update job status
		jobDao.saveAll(jobList);
	}
    
	public void cleanUp(Request request, Domain domain, ArtifactRepository artifactRepository) throws Exception{	
		int requestId = request.getId();
		HashMap<Integer, List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File>> artifactId_ArtifactFileList = new HashMap<Integer, List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File>>();
		HashMap<Integer, Artifact> artifactId_Artifact = new HashMap<Integer, Artifact>();
		
    	// Step 3 - Find all artifacts involved
    	List<Artifact> artifactList = artifactRepository.findAllByWriteRequestId(requestId);
    	for (Iterator iterator = artifactList.iterator(); iterator.hasNext();) {
			Artifact nthArtifact = (Artifact) iterator.next();
			logger.info("Now deleting " + nthArtifact.getName() + "[" + nthArtifact.getId() + "] related File/Artifact DB entries and Filesystem files");
			
			artifactId_Artifact.put(nthArtifact.getId(), nthArtifact);
			
	    	// Step 4 - Flag all the file entries as softdeleted
			List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList = fileRepositoryUtil.getArtifactFileList(nthArtifact, domain);
			artifactId_ArtifactFileList.put(nthArtifact.getId(), artifactFileList);
			
			for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : artifactFileList) {
				nthFile.setDeleted(true);
			}
			
	    	FileRepository<File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
	    	domainSpecificFileRepository.saveAll(artifactFileList);
			logger.info("Files flagged Deleted");
			
	    	// Step 5 - Flag the artifact as softdeleted
			nthArtifact.setDeleted(true);
	    	artifactRepository.save(nthArtifact);
	    	logger.info("Artifact flagged Deleted");
	    	
	    	// Step 6 - Flag the artifactVolume deleted
	    	// N/A
	    	
	    	// Step 7 - Delete the file system files
	    	String artifactFilepathName = nthArtifact.getArtifactclass().getPath() + java.io.File.separator + nthArtifact.getName(); 
	    	
    		logger.info("Now deleting the artifact " + artifactFilepathName);
    		java.io.File artifactFile = new java.io.File(artifactFilepathName);
    		if(artifactFile.exists()) {
    			if(artifactFile.isDirectory())
        			FileUtils.deleteDirectory(artifactFile);
        		else
        			artifactFile.delete();
    			logger.info("Deleted successfully " + artifactFilepathName);
    		}
		}
    	
    	//boolean any 
    	List<Job> jobList = jobDao.findAllByRequestId(requestId);
		for (Job nthJob : jobList) {
			if(nthJob.getStatus() != Status.cancelled) {
				List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> artifactFileList = artifactId_ArtifactFileList.get(nthJob.getInputArtifactId());
				
				Action storagetaskAction = nthJob.getStoragetaskActionId();
				String processingtaskId = nthJob.getProcessingtaskId();
				Integer artifactId = nthJob.getInputArtifactId();
				Artifact artifact = artifactId_Artifact.get(artifactId);
				
				if(storagetaskAction != null && storagetaskAction == Action.write) {
					// softDelete Filevolume entries
					List<FileVolume> toBeAddedFileVolumeTableEntries = new ArrayList<FileVolume>();
					for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : artifactFileList) {
						FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, nthFile.getId(), nthJob.getVolume().getId());
						if(fileVolume != null) {
							fileVolume.setDeleted(true);
							toBeAddedFileVolumeTableEntries.add(fileVolume);
						}
					}
				    if(toBeAddedFileVolumeTableEntries.size() > 0) {
				    	FileVolumeRepository<FileVolume> domainSpecificFileVolumeRepository = domainUtil.getDomainSpecificFileVolumeRepository(domain);
				    	domainSpecificFileVolumeRepository.saveAll(toBeAddedFileVolumeTableEntries);
				    	logger.info("All FileVolume records for " + artifact.getName() + " [" + artifactId + "] in volume " + nthJob.getVolume().getId() + " flagged deleted successfully");
				    }
				}
				else if(processingtaskId != null) {
					// TODO - Need to call processingTask specific delete method here 
					// Tentatively calling hardcoded
					//processingtaskActionMap.get(processingtaskName)
					if(processingtaskId.equals("video-mam-update")) {
						mamUpdateTaskExecutor.cleanUp(artifact.getName(), artifact.getArtifactclass().getCategory());
					}
				}
			}
		}
    }

}
