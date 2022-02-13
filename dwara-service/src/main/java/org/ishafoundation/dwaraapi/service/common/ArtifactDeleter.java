package org.ishafoundation.dwaraapi.service.common;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ArtifactVolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.TFileVolumeDeleter;
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
	private ArtifactDao artifactDao;
	
	@Autowired
	private TFileDao tFileDao;
	
	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private ArtifactVolumeDao artifactVolumeDao;

	@Autowired
	private MamUpdateTaskExecutor mamUpdateTaskExecutor;
	
	@Autowired
	private TFileVolumeDeleter tFileVolumeDeleter;
//	@Autowired
//	private Map<String, IProcessingTask> processingtaskActionMap;


	public void validateArtifactclass(String artifactclassId){
    	// Commenting out for now
//		if(!artifactclassId.equals("video-digitization-pub")) {
//			String errorMsg = "Only deletion of artifactclass video-digitization-pub is supported.";
//        	logger.error(errorMsg);
//        	throw new DwaraException(errorMsg);
//		}
    }
    
	public void validateRequest(Request request){
    	if(request.getActionId() != Action.ingest && request.getActionId() != Action._import) {
    		String errorMsg = "Right now Cancel/Delete is only supported for " + Action.ingest + " and " + Action._import;
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
    
	public void cleanUp(Request userRequest, Request requestToBeActioned) throws Exception{
		cleanUp(userRequest, requestToBeActioned, null);
	}
	
	public void cleanUp(Request userRequest, Request requestToBeActioned, Artifact artifactToBeDeleted) throws Exception{	
		int requestId = requestToBeActioned.getId();
		HashMap<Integer, List<org.ishafoundation.dwaraapi.db.model.transactional.File>> artifactId_ArtifactFileList = new HashMap<Integer, List<org.ishafoundation.dwaraapi.db.model.transactional.File>>();
		HashMap<Integer, List<TFile>> artifactId_ArtifactTFileList = new HashMap<Integer, List<TFile>>();
		HashMap<Integer, Artifact> artifactId_Artifact = new HashMap<Integer, Artifact>();
		
    	// Step 3 - Find all artifacts involved
    	List<Artifact> artifactList = artifactDao.findAllByWriteRequestId(requestId);
    	if(artifactList.size() == 0 && artifactToBeDeleted != null)
    		artifactList.add(artifactToBeDeleted);

    	for (Artifact nthArtifact : artifactList) {
			logger.info("Now deleting " + nthArtifact.getName() + "[" + nthArtifact.getId() + "] related File/Artifact DB entries and Filesystem files");
			
			artifactId_Artifact.put(nthArtifact.getId(), nthArtifact);
			
	    	// Step 4 - Flag all the file entries as softdeleted
			List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileList = fileDao.findAllByArtifactIdAndDeletedFalse(nthArtifact.getId());
			artifactId_ArtifactFileList.put(nthArtifact.getId(), artifactFileList);
			
			for (org.ishafoundation.dwaraapi.db.model.transactional.File nthFile : artifactFileList) {
				nthFile.setDeleted(true);
			}
			
	    	fileDao.saveAll(artifactFileList);
			logger.info("Files flagged Deleted");
			
			// Step 4.5 - Flag all the tFile entries as softdeleted
			List<TFile> artifactTFileList  = tFileDao.findAllByArtifactId(nthArtifact.getId()); 
			if(artifactTFileList != null) { // An artifact can be deleted even after the tape is finalized at that point no TFile entries will be there
				artifactId_ArtifactTFileList.put(nthArtifact.getId(), artifactTFileList);
				for (TFile nthTFile : artifactTFileList) {
					nthTFile.setDeleted(true);
				}
				tFileDao.saveAll(artifactTFileList);
				logger.info("TFiles flagged Deleted");
			}
			
	    	// Step 5 - Flag the artifact as softdeleted
			nthArtifact.setQueryLatestRequest(userRequest);
			nthArtifact.setDeleted(true);
	    	artifactDao.save(nthArtifact);
	    	logger.info("Artifact flagged Deleted");
	    	
	    	// Step 6 - Flag the artifactVolume deleted
			List<ArtifactVolume> artifactVolumeList = artifactVolumeDao.findAllByIdArtifactId(nthArtifact.getId());

			if(artifactVolumeList.size() > 0) {
				for (ArtifactVolume artifactVolume : artifactVolumeList) {
					artifactVolume.setStatus(ArtifactVolumeStatus.deleted);
				}
				
				artifactVolumeDao.saveAll(artifactVolumeList);
			}
			logger.info("ArtifactVolume records flagged Deleted");
			
	    	
	    	// Step 7 - Move/Delete the file system files
	    	// TODO - should we delete or move the file system files???
	    	boolean shouldWeDelete = false;
	    	if(nthArtifact.getArtifactclass().getId().startsWith(DwaraConstants.VIDEO_DIGI_ARTIFACTCLASS_PREFIX))
	    		shouldWeDelete = true;
	    	if(shouldWeDelete) {
		    	String artifactFilepathName = nthArtifact.getArtifactclass().getPath() + java.io.File.separator + nthArtifact.getName(); 
		    	
	    		logger.info("Now deleting the artifact from file system " + artifactFilepathName);
	    		java.io.File artifactFile = new java.io.File(artifactFilepathName);
	    		if(artifactFile.exists()) {
	    			if(artifactFile.isDirectory())
	        			FileUtils.deleteDirectory(artifactFile);
	        		else
	        			artifactFile.delete();
	    			logger.info("Deleted successfully " + artifactFilepathName);
	    		}
	    	}
    		else {
				String destRootLocation = requestToBeActioned.getDetails().getStagedFilepath();
				if(destRootLocation != null) {
					try {
						java.io.File srcFile = FileUtils.getFile(nthArtifact.getArtifactclass().getPath(), nthArtifact.getName());
						java.io.File destFile = FileUtils.getFile(destRootLocation, Status.cancelled.name(), StringUtils.substringAfter(nthArtifact.getName(), "_"));
	
						if(srcFile.isFile())
							Files.createDirectories(Paths.get(FilenameUtils.getFullPathNoEndSeparator(destFile.getAbsolutePath())));		
						else
							Files.createDirectories(destFile.toPath());
		
						Files.move(srcFile.toPath(), destFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
						logger.info(srcFile.toPath() + " moved successfully to " + destFile.toPath());
					}
					catch (Exception e) {
						logger.error("Unable to move file "  + e.getMessage());
					}
				}
	    	}
		}
    	
    	//boolean any 
    	List<Job> jobList = jobDao.findAllByRequestId(requestId);
    	if(jobList.size() == 0) {
			List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileList = artifactId_ArtifactFileList.get(artifactToBeDeleted.getId());
			List<TFile> artifactTFileList = artifactId_ArtifactTFileList.get(artifactToBeDeleted.getId());
			
    		tFileVolumeDeleter.softDeleteTFileVolumeEntries(artifactFileList, artifactTFileList, artifactToBeDeleted);
    	}else {
			for (Job nthJob : jobList) {
				if(nthJob.getStatus() != Status.cancelled) {
					List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileList = artifactId_ArtifactFileList.get(nthJob.getInputArtifactId());
					List<TFile> artifactTFileList = artifactId_ArtifactTFileList.get(nthJob.getInputArtifactId());
					
					Action storagetaskAction = nthJob.getStoragetaskActionId();
					String processingtaskId = nthJob.getProcessingtaskId();
					Integer artifactId = nthJob.getInputArtifactId();
					Artifact artifact = artifactId_Artifact.get(artifactId);
					
					if(storagetaskAction != null && storagetaskAction == Action.write) {
						tFileVolumeDeleter.softDeleteTFileVolumeEntries(artifactFileList, artifactTFileList, artifact, nthJob.getVolume().getId());
					}
					else if(processingtaskId != null) {
						// TODO - Need to call processingTask specific delete method here 
						// Tentatively calling hardcoded
						//processingtaskActionMap.get(processingtaskName)
						if(processingtaskId.equals("video-mam-update")) {
							mamUpdateTaskExecutor.cleanUp(nthJob.getId(), artifact.getName(), artifact.getArtifactclass().getCategory());
						}
					}
				}
			}
    	}
    }
}

