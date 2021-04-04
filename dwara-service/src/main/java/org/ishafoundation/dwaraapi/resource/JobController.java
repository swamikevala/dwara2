package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.job.JobResponse;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class JobController {
	
	private static final Logger logger = LoggerFactory.getLogger(JobController.class);
	
	@Autowired
	JobService jobService;

	@GetMapping(value = "/placeholderjob", produces = "application/json")
	public ResponseEntity<List<JobResponse>> getPlaceholderJobs(@RequestParam(value="requestId", required=false) Integer systemRequestId){
		logger.info("/placeholderjob?" + (systemRequestId != null ? "requestId=" + systemRequestId : ""));
		List<JobResponse> jobResponseList = null;
		try {
			jobResponseList = jobService.getPlaceholderJobs(systemRequestId);
		}catch (Exception e) {
			String errorMsg = "Unable to get Jobs - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(jobResponseList);
	}
	
	@GetMapping(value = "/job", produces = "application/json")
	public ResponseEntity<List<JobResponse>> getJobs(@RequestParam(value="requestId", required=false) Integer systemRequestId, @RequestParam(required=false) String status){
		logger.info("/job?" + (status != null ? "status=" + status : "") + (systemRequestId != null ? "requestId=" + systemRequestId : ""));
		List<JobResponse> jobResponseList = null;
		try {

			List<Status> statusList = null;
			if(status != null) { // TODO - use optional
				statusList = new ArrayList<Status>();
				String[] statusArrAsString = status.split(",");
			   	
			   	for (int i = 0; i < statusArrAsString.length; i++) {
			   		Status statusEnum = Status.valueOf(statusArrAsString[i]);
			   		statusList.add(statusEnum);
				}
			}
			
			jobResponseList = jobService.getJobs(systemRequestId, statusList);
		}catch (Exception e) {
			String errorMsg = "Unable to get Jobs - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(jobResponseList);
	}
	
	@PostMapping("/job/{jobId}/createDependentJobs")
	public ResponseEntity<List<JobResponse>> createDependentJobs(@PathVariable("jobId") int jobId) {
    	logger.info("/job/" + jobId + "/createDependentJobs");
    	List<JobResponse> jobResponseList = null;
    	try {
    		jobResponseList = jobService.createDependentJobs(jobId);
		}catch (Exception e) {
			String errorMsg = "Unable to create dependent Jobs - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
    	return ResponseEntity.status(HttpStatus.OK).body(jobResponseList);
    }

	@PostMapping("/job/{jobId}/marked_completed")
    public ResponseEntity<JobResponse> markedCompletedJob(@PathVariable("jobId") int jobId, @RequestBody String reason) {
    	logger.info("/job/" + jobId + "/marked_completed");
    	JobResponse jobResponse = null;
    	try {
    		jobResponse = jobService.markedCompletedJob(jobId, reason);
		}catch (Exception e) {
			String errorMsg = "Unable to get Jobs - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
    	return ResponseEntity.status(HttpStatus.OK).body(jobResponse);
    }
	
	
    @PostMapping("/job/{jobId}/requeue")
    public ResponseEntity<JobResponse> requeueJob(@PathVariable("jobId") int jobId) {
    	logger.info("/job/" + jobId + "/requeue");
    	JobResponse jobResponse = null;
    	try {
    		jobResponse = jobService.requeueJob(jobId);
		}catch (Exception e) {
			String errorMsg = "Unable to get Jobs - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
    	return ResponseEntity.status(HttpStatus.OK).body(jobResponse);
    }
    
    @PostMapping("/job/multi-requeue")
    public ResponseEntity<List<JobResponse>> requeueSpecifiedFailedJobs(@RequestParam("jobIds") String jobIds) {
    	List<JobResponse> jobResponseList = new ArrayList<JobResponse>();
		List<String> jobIdsList = Arrays.asList(jobIds.split(","));
		for (String jobIdAsString : jobIdsList) {
			int jobId = Integer.parseInt(jobIdAsString);
	    	JobResponse jobResponse = null;
	    	try {
	    		jobResponse = jobService.requeueJob(jobId);
	    		jobResponseList.add(jobResponse);
			}catch (Exception e) {
				String errorMsg = "Unable to get Jobs - " + e.getMessage();
				logger.error(errorMsg, e);
				
				if(e instanceof DwaraException)
					throw (DwaraException) e;
				else
					throw new DwaraException(errorMsg, null);
			}
		}
			
    	return ResponseEntity.status(HttpStatus.OK).body(jobResponseList);
    }
}
