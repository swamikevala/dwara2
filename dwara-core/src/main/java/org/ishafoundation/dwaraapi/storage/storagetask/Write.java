package org.ishafoundation.dwaraapi.storage.storagetask;


import java.util.List;
import java.util.Optional;

import org.ishafoundation.dwaraapi.db.dao.master.ProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ArtifactclassVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.utils.ArtifactclassUtil;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.CoreFlowelement;
import org.ishafoundation.dwaraapi.enumreferences.Priority;
import org.ishafoundation.dwaraapi.enumreferences.RewriteMode;
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
	private ArtifactDao artifactDao;
	
	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private ProcessingtaskDao processingtaskDao;

	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private ArtifactclassVolumeDao artifactclassVolumeDao;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private VolumeUtil volumeUtil;
	
	@Autowired
	private Restore restore;
	
	@Autowired
	private ArtifactclassUtil artifactclassUtil;
		
	@Override
	public StorageJob buildStorageJob(Job job) throws Exception{

		Request request = job.getRequest();
		org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
		
		Artifact artifact = null;
		String artifactName = null;
		String pathPrefix = null;
		String volumegroupId = null;
		Volume volume = null;
		long artifactSize = 0L;
		int priority = Priority.normal.getPriorityValue();
		if(requestedAction == org.ishafoundation.dwaraapi.enumreferences.Action.ingest) {
			String artifactclassId = request.getDetails().getArtifactclassId();
			List<Integer> preReqJobIds = job.getDependencies();
			
			if(preReqJobIds != null) {
				String outputArtifactclassId = null;
				// Now use one of the processing jobs that too generating an output
				for (Integer preReqJobId : preReqJobIds) {
					String processingtaskId = jobDao.findById(preReqJobId).get().getProcessingtaskId();
					if(processingtaskId == null) // Is the dependency a processing job?
						continue;
					
					Processingtask processingtask = null;
					Optional<Processingtask> processingtaskOpt = processingtaskDao.findById(processingtaskId);
					if(processingtaskOpt.isPresent()) {
						processingtask = processingtaskOpt.get();
						outputArtifactclassId = processingtask.getOutputArtifactclass(); // Does the dependent processing job generate an output?
					}
					if(outputArtifactclassId != null)
						break;
				}
				if(outputArtifactclassId != null)
					artifactclassId =  outputArtifactclassId;
			}	
			logger.trace("artifactclassId for getting domain - " + artifactclassId);
			
			Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
			pathPrefix = artifactclassUtil.getPath(artifactclass);
			
			Integer inputArtifactId = job.getInputArtifactId();
			artifact = artifactDao.findById(inputArtifactId).get();
			artifactName = artifact.getName();			

			volumegroupId = job.getGroupVolume().getId(); 
			
			//String artifactpathToBeCopied = pathPrefix + java.io.File.separator + artifactName;
			artifactSize = artifact.getTotalSize();//FileUtils.sizeOf(new java.io.File(artifactpathToBeCopied)); 
			volume = volumeUtil.getToBeUsedPhysicalVolume(volumegroupId, artifactSize);
		}else if(requestedAction == Action.rewrite || requestedAction == Action.migrate) {
			
			Integer inputArtifactId = job.getInputArtifactId();
   			artifact = artifactDao.findById(inputArtifactId).get();
			artifactName = artifact.getName();			

			Integer rewriteCopy = job.getRequest().getDetails().getRewriteCopy();
			RewriteMode rewritePurpose = job.getRequest().getDetails().getMode();
			if(rewritePurpose != null) { // volume rewrite
				if(rewritePurpose == RewriteMode.replace || rewritePurpose == RewriteMode.migrate) {
					Volume volumeInQuestion = volumeDao.findById(job.getRequest().getDetails().getVolumeId()).get();
					volumegroupId = volumeInQuestion.getGroupRef().getId();
				}
			}

			if(volumegroupId == null) {
				Integer copyToBeWritten = null; 
				if(rewriteCopy != null) // artifact rewrite
					copyToBeWritten = rewriteCopy;
				else if(rewritePurpose != null && rewritePurpose == RewriteMode.copy) { // volume rewrite (additional copy)
					Integer additionalCopy = job.getRequest().getDetails().getDestinationCopy();
					copyToBeWritten = additionalCopy;
				}
				
				if(copyToBeWritten != null){
					List<ArtifactclassVolume> artifactclassVolumeList = artifactclassVolumeDao.findAllByArtifactclassIdAndActiveTrue(artifact.getArtifactclass().getId());
					for (ArtifactclassVolume artifactclassVolume : artifactclassVolumeList) {
						Volume grpVolume = artifactclassVolume.getVolume();
						if(grpVolume.getCopy().getId() == copyToBeWritten) {
							volumegroupId = grpVolume.getId();
						}
					}
				}
			}
			
			artifactSize = artifact.getTotalSize(); 
			volume = volumeUtil.getToBeUsedPhysicalVolume(volumegroupId, artifactSize);
		
			// get write job's storage dependency - can't be anything but restore, but looping for making the code generic giving some flexibility
			Job restoreJob = getGoodCopyRestoreJob(job);
			pathPrefix = restore.getArtifactRootLocation(restoreJob); 
		}

		
		StorageJob storageJob = new StorageJob();
		storageJob.setJob(job);
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

	// get the upstream restore job
	private Job getGoodCopyRestoreJob(Job job){
		return jobDao.findByRequestIdAndFlowelementId(job.getRequest().getId(), CoreFlowelement.core_rewrite_flow_good_copy_restore.getId());
		
		
//		List<Integer> dependencies = job.getDependencies();
//		for (Integer nthDependencyJobId : dependencies) {
//			Job nthDependencyJob = jobDao.findById(nthDependencyJobId).get();
//			Action storagetaskAction = nthDependencyJob.getStoragetaskActionId();
//			if(storagetaskAction != null && storagetaskAction == Action.restore) {
//				restoreJob = nthDependencyJob;
//				break;
//			}
//		}

		
		
//		List<Job> dependentJobList = jobUtil.getDependentJobs(job);
//		for (Job nthDependentJob : dependentJobList) {
//			
//			if(nthDependentJob.getStoragetaskActionId() != null && nthDependentJob.getStoragetaskActionId() == Action.restore) {
//				restoreJob = nthDependentJob;
//				break;
//			}
//		}	
	}
}
