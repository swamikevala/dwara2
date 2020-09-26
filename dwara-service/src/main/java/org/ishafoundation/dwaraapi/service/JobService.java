package org.ishafoundation.dwaraapi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.resp.job.JobResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ProcessingFailureDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.JobRunDao;
import org.ishafoundation.dwaraapi.db.keys.JobRunKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Flowelement;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.ProcessingFailure;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.JobRun;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JobService extends DwaraService{

	private static final Logger logger = LoggerFactory.getLogger(JobService.class);

	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private JobRunDao jobRunDao;
	
	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private ProcessingFailureDao processingFailureDao;

	@Autowired
	private JobServiceRequeueHelper jobServiceRequeueHelper;
	
	public List<JobResponse> getAllJobs(){
		List<JobResponse> jobResponseList = new ArrayList<JobResponse>();
		
		List<Job> jobList = (List<Job>) jobDao.findAll();
		for (Job job : jobList) {
			JobResponse jobResponse = frameJobResponse(job);

			jobResponseList.add(jobResponse);
		}
		return jobResponseList;
	}
	
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
		
		List<Job> jobList = jobDao.findAllByRequestId(systemRequestId);
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
		jobResponse.setId(jobId);
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
		jobResponse.setCreatedAt(getDateForUI(job.getCreatedAt()));
		jobResponse.setStartedAt(getDateForUI(job.getStartedAt()));
		jobResponse.setCompletedAt(getDateForUI(job.getCompletedAt()));
		if(job.getStatus() != null)
			jobResponse.setStatus(job.getStatus().name());
		
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

