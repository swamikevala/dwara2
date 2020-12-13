package org.ishafoundation.dwaraapi.process.thread;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.process.request.Artifact;
import org.ishafoundation.dwaraapi.process.request.Artifactclass;
import org.ishafoundation.dwaraapi.process.request.Job;
import org.ishafoundation.dwaraapi.process.request.Volume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobEntityToJobForProcessConverter {

	@Autowired
	private JobDao jobDao;
	
//	@Autowired
//	private DomainUtil domainUtil;

	Job getJobForProcess(org.ishafoundation.dwaraapi.db.model.transactional.Job jobEntity){
		Job jobForProcess = new Job();
		
		
		jobForProcess.setId(jobEntity.getId());
		Action action = jobEntity.getStoragetaskActionId();
		if(action != null)
			jobForProcess.setStoragetaskActionId(action.name());
		jobForProcess.setProcessingtaskId(jobEntity.getProcessingtaskId());
		jobForProcess.setFlowelementId(jobEntity.getFlowelementId());
		
		List<Job> dependencyJobList = new ArrayList<Job>();
		List<Integer> dependencyJobIdList = jobEntity.getDependencies();
		if(dependencyJobIdList != null) {
			for (Integer dependencyJobId : dependencyJobIdList) {
				dependencyJobList.add(getJobForProcess(jobDao.findById(dependencyJobId).get()));
			}
			jobForProcess.setDependencies(dependencyJobList);
		}
		
		Integer inputArtifactId = jobEntity.getInputArtifactId();
		//org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact inputArtifactFromDB = domainUtil.getDomainSpecificArtifact(inputArtifactId);

		Artifact inputArtifact = new Artifact(); // can contain one of the domain artifacts id
		inputArtifact.setId(inputArtifactId);
//		inputArtifact.setName(inputArtifactFromDB.getName());
//		inputArtifact.setPath(path);
		Artifactclass inputArtifactclass = new Artifactclass();
//		artifactclass.setId(id);
//		artifactclass.setDomain(domain);
//		artifactclass.setCategory(category);
		inputArtifact.setArtifactclass(inputArtifactclass);
		jobForProcess.setInputArtifact(inputArtifact);
		
		Integer outputArtifactId = jobEntity.getOutputArtifactId();
		Artifact outputArtifact = new Artifact();
		outputArtifact.setId(outputArtifactId);
		Artifactclass outputArtifactclass = new Artifactclass();
		outputArtifact.setArtifactclass(outputArtifactclass);
		jobForProcess.setOutputArtifact(outputArtifact);
		
		
		Volume volume = new Volume();
		if(jobEntity.getVolume() != null)
			volume.setId(jobEntity.getVolume().getId());
		if(jobEntity.getGroupVolume() != null)
			volume.setCopyId(jobEntity.getGroupVolume().getCopy().getId());
		jobForProcess.setVolume(volume);
		
		jobForProcess.setEncrypted(jobEntity.isEncrypted());
		return jobForProcess;
	}
}

