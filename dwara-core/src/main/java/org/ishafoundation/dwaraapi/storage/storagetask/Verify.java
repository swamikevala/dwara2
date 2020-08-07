package org.ishafoundation.dwaraapi.storage.storagetask;


import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobMapDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.JobMap;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
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
	private JobDao jobDao; 
	
	@Autowired
	private JobMapDao jobMapDao; 
    
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	
	@Override
	public StorageJob buildStorageJob(Job job){
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		
		Job writeJobToBeVerified = null;
		
		List<JobMap> preReqJobRefs = jobMapDao.findAllByIdJobId(job.getId());// getting all prerequisite jobss
		for (JobMap nthPreReqJobRef : preReqJobRefs) {
			// means a dependent job.
			Job preReqJobRef  = jobDao.findById(nthPreReqJobRef.getId().getJobRefId()).get();
			if(preReqJobRef != null && preReqJobRef.getStoragetaskActionId() == Action.write) {
				writeJobToBeVerified = preReqJobRef;
			}
		}		
		
		storageJob.setVolume(writeJobToBeVerified.getVolume());
		
		// TODO : assuming verify is part of the complex action... Fix it so that its generally works outside actionelement/ingest partnership too
		String artifactclassId = job.getActionelement().getArtifactclassId();
		Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
		Domain domain = artifactclass.getDomain();

		Integer inputArtifactId = job.getInputArtifactId();
		Artifact artifact = domainUtil.getDomainSpecificArtifact(domain, inputArtifactId);
		storageJob.setArtifact(artifact);			

		/** lazy loading other details once the job is selected for processing - Refer AbstractStoragetypeJobProcessor.verify()**/
		
		return storageJob;
	}
}
