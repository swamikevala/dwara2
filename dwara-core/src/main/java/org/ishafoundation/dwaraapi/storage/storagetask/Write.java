package org.ishafoundation.dwaraapi.storage.storagetask;


import org.apache.commons.io.FileUtils;
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
import org.ishafoundation.dwaraapi.utils.VolumeUtil;
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
	
	@Autowired
	private VolumeUtil volumeUtil;
		
	@Override
	public StorageJob buildStorageJob(Job job) throws Exception{

		Request request = job.getRequest();
		org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
		
		Artifact artifact = null;
		String artifactName = null;
		String pathPrefix = null;
		String volumegroupId = null;
		Volume volume = null;
		Domain domain = null;
		long artifactSize = 0L;
		if(requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.ingest) {
			//Dont use this as for proxy copy the artifact class is more apt to be picked from action element - Integer artifactclassId = job.getRequest().getDetails().getArtifactclass_id(); 
			String artifactclassId = job.getActionelement().getArtifactclassId();
			Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
			domain = artifactclass.getDomain();
			pathPrefix = artifactclass.getPath();
			
			Integer inputArtifactId = job.getInputArtifactId();
			artifact = domainUtil.getDomainSpecificArtifact(domain, inputArtifactId);
			artifactName = artifact.getName();			

			volumegroupId = job.getGroupVolume().getId(); 
			
			String artifactpathToBeCopied = pathPrefix + java.io.File.separator + artifactName;
			artifactSize = FileUtils.sizeOf(new java.io.File(artifactpathToBeCopied)); 
			volume = volumeUtil.getToBeUsedPhysicalVolume(domain, volumegroupId, artifactSize);
		}else if(requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.rewrite || requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.migrate) {
			artifactName = request.getDetails().getStagedFilename();
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
		storageJob.setArtifactSize(artifactSize);
		// to where
		storageJob.setVolume(volume);
		
		return storageJob;
	
	}

}
