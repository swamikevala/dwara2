package org.ishafoundation.dwaraapi.storage.storagetask;


import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("verify")
//@Profile({ "!dev & !stage" })
public class Verify extends AbstractStoragetaskAction{

    private static final Logger logger = LoggerFactory.getLogger(Verify.class);

	@Autowired
	private VolumeDao volumeDao; 
    
	@Autowired
	private DomainUtil domainUtil;
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;
	
	
	@Override
	public StorageJob buildStorageJob(Job job){
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		
		Job writeJobToBeVerified = job.getJobRef();
		Integer volumeIdWriteJobUsed = writeJobToBeVerified.getDetails().getVolume_id();
		Volume volume = volumeDao.findById(volumeIdWriteJobUsed).get();
		storageJob.setVolume(volume);
		
		Integer artifactclassId = job.getActionelement().getArtifactclassId();
		Artifactclass artifactclass = (Artifactclass) dBMasterTablesCacheManager
				.getRecord(CacheableTablesList.artifactclass.name(), artifactclassId);
		Domain domain = artifactclass.getDomain();

		Integer inputArtifactId = job.getInputArtifactId();
		Artifact artifact = domainUtil.getDomainSpecificArtifact(domain, inputArtifactId);
		storageJob.setArtifact(artifact);			

		/** lazy loading other details once the job is selected for processing - Refer AbstractStoragetypeJobProcessor.verify()**/
		
		return storageJob;
	}
}
