package org.ishafoundation.dwaraapi.service;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.job.JobResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Actionelement;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobService extends DwaraService{

	private static final Logger logger = LoggerFactory.getLogger(JobService.class);

	@Autowired
	protected JobDao jobDao;
	
	@Autowired
	protected DomainUtil domainUtil;

	public List<JobResponse> getJobs(int systemRequestId){
		List<JobResponse> jobResponseList = new ArrayList<JobResponse>();
		
		List<Job> jobList = jobDao.findAllByRequestId(systemRequestId);
		for (Job job : jobList) {
			JobResponse jobResponse = new JobResponse();
			jobResponse.setId(job.getId());
			jobResponse.setRequestId(job.getRequest().getId());
			Action storagetaskAction = job.getStoragetaskActionId();
			if(storagetaskAction != null)
				jobResponse.setStoragetaskAction(storagetaskAction.name());
			jobResponse.setProcessingTask(job.getProcessingtaskId());
			Actionelement actionelement = job.getActionelement();
			if(actionelement != null)
				jobResponse.setActionelementId(actionelement.getId());
			jobResponse.setInputArtifactId(job.getInputArtifactId());
			jobResponse.setOutputArtifactId(job.getOutputArtifactId());
			jobResponse.setCreatedAt(getDateForUI(job.getCreatedAt()));
			jobResponse.setStartedAt(getDateForUI(job.getStartedAt()));
			jobResponse.setCompletedAt(getDateForUI(job.getCompletedAt()));
			if(job.getStatus() != null)
				jobResponse.setStatus(job.getStatus().name());
			
			Volume groupVolume = job.getGroupVolume();
			
			if(groupVolume != null) {
				jobResponse.setCopyNumber(groupVolume.getCopyNumber());
			}
			else {
				Volume volume = job.getVolume();
				if(volume != null && volume.getType() == Volumetype.provisioned) {
					jobResponse.setCopyNumber(volume.getCopyNumber());
				}
			}				
			jobResponseList.add(jobResponse);
		}
		return jobResponseList;
	}
	
}

