package org.ishafoundation.dwaraapi.storage;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.view.V_RestoreFileDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageJobBuilder {
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private VolumeDao volumeDao;

	@Autowired
	private V_RestoreFileDao v_RestoreFileDao;

	public StorageJob buildStorageJob(Job job) {
		StorageJob storageJob = null;
		Integer storagetaskId = job.getStoragetaskId();
		Subrequest subrequest = job.getSubrequest();
		Request request = subrequest.getRequest();
		org.ishafoundation.dwaraapi.enumreferences.Action action = request.getAction();
		if (storagetaskId != null) { // double verifying if its a storage job...
			if(action == org.ishafoundation.dwaraapi.enumreferences.Action.ingest) {
				
					// TODO - Lazy loading of archivejob... just get all the information only when the job is picked for processing. Why load all info upfront only to get discarded.
					
					// for a dependent artifactclassVolumeset job - the source to artifactclassVolumeset is different and the input artifact id comes from the prerequisite task
				Domain domain = job.getSubrequest().getRequest().getDomain();
				
				Integer inputArtifactId = job.getInputArtifactId();
				Artifact artifact = domainUtil.getDomainSpecificArtifact(domain, inputArtifactId);

					
				String artifactName = artifact.getName();			
				Artifactclass artifactclass = artifact.getArtifactclass();	
				int artifactclassId = artifactclass.getId();
				String artifactpathToBeCopied = artifactclass.getPath() + java.io.File.separator + artifactName;
				
				long sizeOfTheLibraryToBeWritten = 0;//FileUtils.sizeOfDirectory(new java.io.File(artifactpathToBeCopied)); 
				
				storageJob = new StorageJob();
				storageJob.setJob(job);
				
				// what needs to be ingested
				storageJob.setArtifact(artifact);
				storageJob.setArtifactPrefixPath(artifactclass.getPath());
				storageJob.setArtifactName(artifactName);

				int volumegroupId = job.getActionelement().getVolumeId();
				// to where
				storageJob.setVolume(getToBeUsedPhysicalVolume(volumegroupId, sizeOfTheLibraryToBeWritten));
			}
//			else if(action == org.ishafoundation.dwaraapi.enumreferences.Action.restore) {
//				storageJob = new StorageJob();
//				storageJob.setJob(job);
//				
//				// What type of job
//				storageJob.setStoragetask(Storagetask.restore);
//				
//				// what
//				int fileIdToBeRestored = subrequest.getBody().getFileId();
//				storageJob.setFileId(fileIdToBeRestored);
//				
//				// From where
//				int copyNumber = subrequest.getBody().getLocationId();
//				//request.getCopyNumber(); 
//	//			String requestedBy = request.getUser()
//	//			int userId = userDao.findByName(requestedBy).getId();
//				
//				int userId = request.getUserId();
//	
//				V_RestoreFile v_RestoreFile = v_RestoreFileDao.findByVolumesetLocationAndIdFileIdAndIdActionAndIdUserId(copyNumber, fileIdToBeRestored, Action.restore, userId);			
//	
//				storageJob.setFilePathname(v_RestoreFile.getFilePathname());
//	
//				int volumeId = v_RestoreFile.getId().getVolumeId();
//				Volume volume = volumeDao.findById(volumeId).get();
//				Volume volume = new Volume();
//				volume.setVolume(volume);
//				storageJob.setVolume(volume);
//	
//				int block = v_RestoreFile.getFileVolumeBlock();
//				storageJob.setBlock(block);
//				
//				int offset = v_RestoreFile.getFileVolumeOffset();
//				storageJob.setOffset(offset);
//	
//				// to where
//				String targetLocation = request.getTargetvolume().getPath();
//				storageJob.setDestinationPath(targetLocation + java.io.File.separator + request.getOutputFolder());
//				
//				// how
//				//storageJob.setOptimizeVolumeAccess(true); // TODO hardcoded for phase1
//				storageJob.setEncrypted(v_RestoreFile.isFileVolumeEncrypted());				
//				//storageJob.setPriority(10);  // TODO hardcoded for phase1subrequest.getPriority());
//				Volumeset volumeset = volume.getVolumeset();
//				Storageformat storageformat = volumeset.getStorageformat();
//				storageJob.setStorageformat(storageformat);
//			}
//			else if(action == org.ishafoundation.dwaraapi.enumreferences.Action.rewrite) {
//				// Rewrite should build a storagejob in such a way that it has both restore and ingest details...
//				// Volume will be the source from where the artifact need to be restored
//				// destination path should be set to artifactclass.getPath() so that write will use from the below correctly 
//				// storageJob.setLibraryPrefixPath(artifactclass.getPath());
//				// Volume for writing will set where???
//				
//				
//			}
//			else if(action == org.ishafoundation.dwaraapi.enumreferences.Action.format) {
//				storageJob = new StorageJob();
//				storageJob.setJob(job);
//				
//				// What type of job
//				storageJob.setStoragetask(Storagetask.format);
//	
//				String volumeBarcode = "V5A999L7"; //FIXME: subrequest.getVolumeBarcode();
//				Volume volume = getVolume(volumeBarcode);
//				
//				Volume volume = new Volume();
//				
//				volume.setVolume(volume);
//				storageJob.setVolume(volume);	
//				
//				storageJob.setStorageformat(volume.getVolumeset().getStorageformat());
//			}
		}
		return storageJob;
	}

	private Volume getToBeUsedPhysicalVolume(int volumegroupId, long sizeOfTheLibraryToBeWritten) {
		
		Volume toBeUsedVolume = null;
		List<Volume> volumesList = volumeDao.findAllByVolumeRefIdAndFinalizedIsFalse(volumegroupId);
		for (Volume volume : volumesList) {
			//TODO if(Volume.getFree() > sizeOfTheLibraryToBeWritten) {
				toBeUsedVolume = volume; // for now defaulting to first one...
				break;
			//}
		}
		return toBeUsedVolume;		
	}


}
