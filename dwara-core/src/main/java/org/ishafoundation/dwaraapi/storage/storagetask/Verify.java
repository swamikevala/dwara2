package org.ishafoundation.dwaraapi.storage.storagetask;


import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.ProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
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
	private ProcessingtaskDao processingtaskDao;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	
	@Override
	public StorageJob buildStorageJob(Job job) throws Exception{
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
		
		Job writeJobToBeVerified = null;
		
		List<Integer> dependencies = job.getDependencies();
		if(dependencies != null) {
			for (Integer nthPreReqJobId : dependencies) {
				Job preReqJobRef  = jobDao.findById(nthPreReqJobId).get();
				if(preReqJobRef != null && preReqJobRef.getStoragetaskActionId() == Action.write) {
					writeJobToBeVerified = preReqJobRef;
				}
			}
		}		
		
		if(writeJobToBeVerified == null)
			throw new Exception("Action write is a prerequisite for verify. Please ensure the dependency is mapped in flowelement.dependencies");
		
		storageJob.setVolume(writeJobToBeVerified.getVolume());
		
		String artifactclassId = job.getRequest().getDetails().getArtifactclassId();
		List<Integer> preReqJobIdsOfWriteJobToBeVerified = writeJobToBeVerified.getDependencies();
		if(preReqJobIdsOfWriteJobToBeVerified != null) {
			// TODO : Assuming only one dependency
			String processingtaskId = jobDao.findById(preReqJobIdsOfWriteJobToBeVerified.get(0)).get().getProcessingtaskId();  
			Processingtask processingtask = processingtaskDao.findById(processingtaskId).get();
			String outputArtifactclassSuffix = processingtask.getOutputArtifactclassSuffix();
			artifactclassId = artifactclassId + outputArtifactclassSuffix;
		}	
		
		Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
		Domain domain = artifactclass.getDomain();

		Integer inputArtifactId = job.getInputArtifactId();
		Artifact artifact = domainUtil.getDomainSpecificArtifact(domain, inputArtifactId);
		storageJob.setArtifact(artifact);			

		/** lazy loading other details once the job is selected for processing - Refer AbstractStoragetypeJobProcessor.verify()**/
		
		return storageJob;
	}
}
