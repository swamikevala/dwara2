package org.ishafoundation.dwaraapi.storage.storagetask;


import java.util.List;
import java.util.Optional;

import org.ishafoundation.dwaraapi.db.dao.master.ProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
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
	private JobDao jobDao;
	
	@Autowired
	private ProcessingtaskDao processingtaskDao;
	
	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private VolumeUtil volumeUtil;
	
	@Autowired
	private Restore restore;
		
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
		int priority = 0;
		if(requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.ingest) {
			String artifactclassId = request.getDetails().getArtifactclassId();
			List<Integer> preReqJobIds = job.getDependencies();
			
			if(preReqJobIds != null) {
				String outputArtifactclassSuffix = null;
				// Now use one of the processing jobs that too generating an output
				for (Integer preReqJobId : preReqJobIds) {
					String processingtaskId = jobDao.findById(preReqJobId).get().getProcessingtaskId();
					if(processingtaskId == null) // Is the dependency a processing job?
						continue;
					
					Processingtask processingtask = null;
					Optional<Processingtask> processingtaskOpt = processingtaskDao.findById(processingtaskId);
					if(processingtaskOpt.isPresent()) {
						processingtask = processingtaskOpt.get();
						outputArtifactclassSuffix = processingtask.getOutputArtifactclassSuffix(); // Does the dependent processing job generate an output?
					}
					if(outputArtifactclassSuffix != null)
						break;
				}
				if(outputArtifactclassSuffix != null)
					artifactclassId =  artifactclassId + outputArtifactclassSuffix;
			}	
			logger.trace("artifactclassId for getting domain - " + artifactclassId);
			
			Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
			domain = artifactclass.getDomain();
			pathPrefix = artifactclass.getPath();
			
			Integer inputArtifactId = job.getInputArtifactId();
			artifact = domainUtil.getDomainSpecificArtifact(domain, inputArtifactId);
			artifactName = artifact.getName();			

			volumegroupId = job.getGroupVolume().getId(); 
			
			//String artifactpathToBeCopied = pathPrefix + java.io.File.separator + artifactName;
			artifactSize = artifact.getTotalSize();//FileUtils.sizeOf(new java.io.File(artifactpathToBeCopied)); 
			volume = volumeUtil.getToBeUsedPhysicalVolume(domain, volumegroupId, artifactSize);
		}else if(requestedAction == Action.rewrite || requestedAction == Action.migrate) {
			
			Integer inputArtifactId = job.getInputArtifactId();
			Domain[] domains = Domain.values();
   		
    		for (Domain nthDomain : domains) {
    			artifact = domainUtil.getDomainSpecificArtifact(nthDomain, inputArtifactId);
    			if(artifact != null) {
    				domain = nthDomain;
    				break;
    			}
			}
			artifactName = artifact.getName();			

			volumegroupId = job.getGroupVolume().getId(); 
			
			artifactSize = artifact.getTotalSize(); 
			volume = volumeUtil.getToBeUsedPhysicalVolume(domain, volumegroupId, artifactSize);
		
			// get write job's dependency - can't be anything but restore, but looping for making the code generic giving some flexibility
			List<Integer> dependencies = job.getDependencies();
			Job restoreJob = null;
			for (Integer nthDependencyJobId : dependencies) {
				Job nthDependencyJob = jobDao.findById(nthDependencyJobId).get();
				Action storagetaskAction = nthDependencyJob.getStoragetaskActionId();
				if(storagetaskAction != null && storagetaskAction == Action.restore) {
					restoreJob = nthDependencyJob;
					break;
				}
			}
			
			
			pathPrefix = restore.getRestoreLocation(restoreJob); 
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
		if(volume != null) {
			Integer copyId = volume.getGroupRef().getCopy().getId();
			priority = copyId * 100;
		}
		storageJob.setPriority(priority);
		return storageJob;
	
	}

}
