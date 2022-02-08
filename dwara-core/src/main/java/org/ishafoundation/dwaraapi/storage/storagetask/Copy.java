package org.ishafoundation.dwaraapi.storage.storagetask;


import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.utils.ArtifactclassUtil;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.utils.VolumeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("copy")
//@Profile({ "!dev & !stage" })
public class Copy extends AbstractStoragetaskAction{
	
	@Autowired
	private ArtifactDao artifactDao;
	
	@Autowired
	private VolumeUtil volumeUtil;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private ArtifactclassUtil artifactclassUtil;

	
	public StorageJob buildStorageJob(Job job) throws Exception{
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		
		
		String volumegroupId = job.getGroupVolume().getId(); 

		Integer inputArtifactId = job.getInputArtifactId();
		Artifact artifact = artifactDao.findById(inputArtifactId).get();
		//String artifactpathToBeCopied = pathPrefix + java.io.File.separator + artifactName;
		long artifactSize = artifact.getTotalSize();//FileUtils.sizeOf(new java.io.File(artifactpathToBeCopied)); 

		Volume volume = volumeUtil.getToBeUsedPhysicalVolume(volumegroupId, artifactSize);
		
		storageJob.setVolume(volume);
		
		Request request = job.getRequest();
		org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
		String artifactclassId = null;
		if(requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.ingest)
			artifactclassId = request.getDetails().getArtifactclassId();

		Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
		String pathPrefix = artifactclassUtil.getPath(artifactclass);
		storageJob.setArtifactPrefixPath(pathPrefix);
		
		String artifactName = artifact.getName();
		storageJob.setArtifactName(artifactName);
		
		return storageJob;
	}

}
