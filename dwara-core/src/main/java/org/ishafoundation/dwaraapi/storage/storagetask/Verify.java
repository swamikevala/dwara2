package org.ishafoundation.dwaraapi.storage.storagetask;


import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.ProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
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
	private ArtifactRepository artifactRepo;
	
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
			String outputArtifactclassSuffix = null;
			// Now use one of the processing jobs that too generating an output
			for (Integer preReqJobId : preReqJobIdsOfWriteJobToBeVerified) {
				String processingtaskId = jobDao.findById(preReqJobId).get().getProcessingtaskId();
				if(processingtaskId == null) // Is the dependency a processing job?
					continue;
				
				Processingtask processingtask = processingtaskDao.findById(processingtaskId).get();
				outputArtifactclassSuffix = processingtask.getOutputArtifactclassSuffix(); // Does the dependent processing job generate an output?
				if(outputArtifactclassSuffix != null)
					break;
			}
			if(outputArtifactclassSuffix != null)
				artifactclassId = artifactclassId + outputArtifactclassSuffix;
		}	
		
		Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
		/*
		 * Domain domain = artifactclass.getDomain(); storageJob.setDomain(domain);
		 */
		
		Integer inputArtifactId = job.getInputArtifactId();
		Artifact artifact = artifactRepo.findById((int)inputArtifactId);
		storageJob.setArtifact(artifact); // Needed for tape job selection			

		/** lazy loading other details once the job is selected for processing - Refer AbstractStoragetypeJobProcessor.beforeVerify()**/
		
		return storageJob;
	}
}
