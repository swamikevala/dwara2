package org.ishafoundation.dwaraapi.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.resp.job.JobResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ProcessingFailureDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Flowelement;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.ProcessingFailure;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.job.JobManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobService extends DwaraService{

	private static final Logger logger = LoggerFactory.getLogger(JobService.class);

	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private ProcessingFailureDao processingFailureDao;

	@Autowired
	private JobServiceRequeueHelper jobServiceRequeueHelper;

	@Autowired
	private JobManipulator jobManipulator;

	public List<JobResponse> getJobs(Integer systemRequestId, List<Status> statusList) {
		List<JobResponse> jobResponseList = new ArrayList<JobResponse>();
		
		List<Job> jobList = jobDao.findAllDynamicallyBasedOnParamsOrderByLatest(systemRequestId, statusList);
		for (Job job : jobList) {
			JobResponse jobResponse = frameJobResponse(job);

			jobResponseList.add(jobResponse);
		}
		return jobResponseList;
	}
	
	public List<JobResponse> getJobs(int systemRequestId){
		List<JobResponse> jobResponseList = new ArrayList<JobResponse>();
		
		Request request = requestDao.findById(systemRequestId).get();
		Action requestAction = request.getActionId();
		
		List<Job> jobList = null;
		if(requestAction == Action.ingest) {
			jobList = jobManipulator.getJobs(request);
		} else 
			jobList = jobDao.findAllByRequestId(systemRequestId);
		
		for (Job job : jobList) {
			JobResponse jobResponse = frameJobResponse(job);

			jobResponseList.add(jobResponse);
		}
		return jobResponseList;
	}
	
	public JobResponse requeueJob(int jobId) throws Exception{
		return frameJobResponse(jobServiceRequeueHelper.requeueJob(jobId, getUserFromContext()));
	}
	

	private JobResponse frameJobResponse(Job job) {
		JobResponse jobResponse = new JobResponse();
		int jobId = job.getId();
		String uId = job.getuId();
		
		if(uId != null) {
			jobResponse.setId(uId);
			jobResponse.setJobId(jobId + "");
			jobResponse.setDependencies(job.getuIdDependencies());
		}else {
			jobResponse.setId(jobId + "");
			List<Integer> jobDependencyIdsAsIntegerList = job.getDependencies();
			List<String> jobDependencyIdsAsStringList = new ArrayList<>(jobDependencyIdsAsIntegerList.size());
			for (Integer nthJobDependencyIdAsInteger : jobDependencyIdsAsIntegerList) { 
			  jobDependencyIdsAsStringList.add(String.valueOf(nthJobDependencyIdAsInteger)); 
			}
			jobResponse.setDependencies(jobDependencyIdsAsStringList);
		}
		jobResponse.setRequestId(job.getRequest().getId());
		Action storagetaskAction = job.getStoragetaskActionId();
		if(storagetaskAction != null)
			jobResponse.setStoragetaskAction(storagetaskAction.name());
		String processingtaskId = job.getProcessingtaskId();
		jobResponse.setProcessingTask(processingtaskId);
		Flowelement flowelement = job.getFlowelement();
		if(flowelement != null)
			jobResponse.setFlowelementId(flowelement.getId());
		jobResponse.setInputArtifactId(job.getInputArtifactId());
		jobResponse.setOutputArtifactId(job.getOutputArtifactId());
		
		
		
		if(jobId > 0) { // For yet to be created jobs Job id is 0
			jobResponse.setCreatedAt(getDateForUI(job.getCreatedAt()));
			jobResponse.setStartedAt(getDateForUI(job.getStartedAt()));
			jobResponse.setCompletedAt(getDateForUI(job.getCompletedAt()));
			if(job.getStatus() != null)
				jobResponse.setStatus(job.getStatus().name());
		}
		
		Volume volume = job.getVolume();
		if(volume != null)
			jobResponse.setVolume(volume.getId());
		
		Volume groupVolume = job.getGroupVolume();
		if(groupVolume != null) {
			jobResponse.setCopy(groupVolume.getCopy().getId());
		}
		else {
			if(volume != null && volume.getType() == Volumetype.provisioned) {
				jobResponse.setCopy(volume.getCopy().getId());
			}
		}
		
		String jobMsg = job.getMessage();
		if(StringUtils.isNotBlank(jobMsg));
			jobResponse.setMessage(jobMsg);
		
		List<String> fileFailures = new ArrayList<String>();	
		if(StringUtils.isNotBlank(processingtaskId)) {
			// Check if any processing failures are there for this job
			List<ProcessingFailure> processingFailureList = processingFailureDao.findAllByJobId(jobId);
			for (ProcessingFailure nthProcessingFailure : processingFailureList) {
				fileFailures.add("File " + nthProcessingFailure.getFileId() + " " + nthProcessingFailure.getReason());
			}
		}
		if(fileFailures.size() > 0)
			jobResponse.setFileFailures(fileFailures);
		
		return jobResponse;
	}
}

