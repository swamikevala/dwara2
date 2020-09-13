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
		Request userRequest = null;
		try {		
			Job jobToBeRequeued = jobDao.findById(jobId).get();
			if(jobToBeRequeued.getStatus() != Status.completed_failures && jobToBeRequeued.getStatus() != Status.failed)
				throw new DwaraException("Job cannot be requeued. Only failed or a job completed with some failures can be rerun. Any extra protection needed for avoiding written content getting requeued again?"); //

	    	long jobRunCount = jobRunDao.countByJobId(jobId);
	    	int requeueId = (int) (jobRunCount + 1);
	    	
			userRequest = new Request();
	    	userRequest.setType(RequestType.user);
			userRequest.setActionId(Action.requeue);
	    	User user = getUserObjFromContext();
	    	String requestedBy = user.getName();
	    	userRequest.setRequestedBy(user);
			userRequest.setRequestedAt(LocalDateTime.now());
			RequestDetails details = new RequestDetails();
			ObjectMapper mapper = new ObjectMapper();
	
			HashMap<String, Object> data = new HashMap<String, Object>();
	    	data.put("jobId", jobId);
	    	data.put("requeueId", requeueId);
	    	
	    	String jsonAsString = mapper.writeValueAsString(data);
			JsonNode postBodyJson = mapper.readValue(jsonAsString, JsonNode.class);
			details.setBody(postBodyJson);
			userRequest.setDetails(details);
			
	    	userRequest = requestDao.save(userRequest);
	    	int userRequestId = userRequest.getId();
	    	logger.info(DwaraConstants.USER_REQUEST + userRequestId);
	    	
	    	// update the reference table    	
	    	JobRun jobRun = new JobRun();
	    	jobRun.setId(new JobRunKey(jobId, requeueId));
	    	jobRun.setStartedAt(jobToBeRequeued.getStartedAt());
	    	jobRun.setCompletedAt(jobToBeRequeued.getCompletedAt());
	    	jobRun.setStatus(jobToBeRequeued.getStatus());
	    	jobRun.setMessage(jobToBeRequeued.getMessage());
	    	jobRun.setDevice(jobToBeRequeued.getDevice());
	    	jobRun.setVolume(jobToBeRequeued.getVolume());
	    	jobRunDao.save(jobRun);
	    	logger.debug("JobRun record created successfully " + jobId + ":" + requeueId);
	    	
	    	if(jobToBeRequeued.getProcessingtaskId() != null) {
				List<ProcessingFailure> processingFailureList = processingFailureDao.findAllByJobId(jobId);
				processingFailureDao.deleteAll(processingFailureList);
		    	logger.debug("Processing failure records cleaned up " + jobId);
	    	}
			
			jobToBeRequeued.setStatus(Status.queued);
			jobToBeRequeued.setMessage(null);
			jobToBeRequeued = jobDao.save(jobToBeRequeued);
			logger.info("Job queued successfully " + jobId);
			
			userRequest.setStatus(Status.completed);
			userRequest = requestDao.save(userRequest);
			
			return frameJobResponse(jobToBeRequeued);
		}catch (Exception e) {
			if(userRequest != null && userRequest.getId() != 0) {
				userRequest.setStatus(Status.failed);
				userRequest = requestDao.save(userRequest);
			}
			throw e;
		}
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

