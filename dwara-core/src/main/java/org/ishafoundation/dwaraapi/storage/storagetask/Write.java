package org.ishafoundation.dwaraapi.storage.storagetask;


import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("write")
//@Profile({ "!dev & !stage" })
public class Write extends AbstractStoragetaskAction{

    private static final Logger logger = LoggerFactory.getLogger(Write.class);
    
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
		
	@Override
	public StorageJob buildStorageJob(Job job){

		Request request = job.getRequest();
		org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
		
		Artifact artifact = null;
		String artifactName = null;
		String pathPrefix = null;
		String volumegroupId = null;
		Volume volume = null;
		Domain domain = null;
		if(requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.ingest) {
			//Dont use this as for proxy copy the artifact class is more apt to be picked from action element - Integer artifactclassId = job.getRequest().getDetails().getArtifactclass_id(); 
			String artifactclassId = job.getActionelement().getArtifactclassId();
			Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
			domain = artifactclass.getDomain();
			pathPrefix = artifactclass.getPath();
			
			Integer inputArtifactId = job.getInputArtifactId();
			artifact = domainUtil.getDomainSpecificArtifact(domain, inputArtifactId);
			artifactName = artifact.getName();			

			volumegroupId = job.getActionelement().getVolumeId();
			
			String artifactpathToBeCopied = pathPrefix + java.io.File.separator + artifactName;
			long sizeOfTheLibraryToBeWritten = 0;//TODO - FileUtils.sizeOfDirectory(new java.io.File(artifactpathToBeCopied)); 			
			volume = getToBeUsedPhysicalVolume(volumegroupId, sizeOfTheLibraryToBeWritten);

		}else if(requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.rewrite || requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.migrate) {
			artifactName = request.getDetails().getArtifact_name();
			pathPrefix = "whereverRestoredByThePrecedingRestoreJob";//artifactclass.getPath();
			// TODO have a util for Group Uid to Uid 
//			String volumegroupUid = request.getDetails().getVolume_group_uid();
//			Volume volume = volumeDao.findByUid(volumegroupUid);
//			volumegroupId = volume.getId();
			// TODO domain = ??
			String volumeUid = request.getDetails().getTo_volume_uid();
			volume = volumeDao.findById(volumeUid);
		}

		
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		storageJob.setDomain(domain);
		storageJob.setConcurrentCopies(artifact.getArtifactclass().isConcurrentVolumeCopies());
		// what needs to be ingested
		storageJob.setArtifact(artifact);
		storageJob.setArtifactPrefixPath(pathPrefix);
		storageJob.setArtifactName(artifactName);

		// to where
		storageJob.setVolume(volume);
		
		return storageJob;
	
	}
	
	private Volume getToBeUsedPhysicalVolume(String volumegroupId, long sizeOfTheLibraryToBeWritten) {
		
		Volume toBeUsedVolume = null;
		List<Volume> physicalVolumesList = volumeDao.findAllByVolumeRefIdAndFinalizedIsFalseOrderByIdAsc(volumegroupId);
		for (Volume nthPhysicalVolume : physicalVolumesList) {
			// The chosen volume may not have enough space because of queued write jobs using space and so we may have to get the volume again just before write(job selection)...
			//TODO if(Volume.getFree() > sizeOfTheLibraryToBeWritten) {
				toBeUsedVolume = nthPhysicalVolume; // for now defaulting to first one...
				break;
			//}
		}
		return toBeUsedVolume;		
	}
}
