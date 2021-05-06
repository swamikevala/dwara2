package org.ishafoundation.dwaraapi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.resp.job.GroupedJobResponse;
import org.ishafoundation.dwaraapi.api.resp.job.JobResponse;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ProcessingFailureDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.JobRunDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TTFileJobDao;
import org.ishafoundation.dwaraapi.db.keys.JobRunKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.ProcessingFailure;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.JobRun;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TTFileJob;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.job.JobManipulator;
import org.ishafoundation.dwaraapi.utils.StatusUtil;
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
	private RequestDao requestDao;
	
	@Autowired
	private ProcessingFailureDao processingFailureDao;

	@Autowired
	private JobServiceRequeueHelper jobServiceRequeueHelper;

	@Autowired
	private JobManipulator jobManipulator;
	
	@Autowired
	private JobCreator jobCreator;

	@Autowired
	private UserDao userDao;

	@Autowired
	private TTFileJobDao tFileJobDao;

	@Autowired
	private JobRunDao jobRunDao;

	public List<JobResponse> getJobs(Integer systemRequestId, List<Status> statusList) {
		List<JobResponse> jobResponseList = new ArrayList<JobResponse>();
		
		List<Job> jobList = jobDao.findAllDynamicallyBasedOnParamsOrderByLatest(systemRequestId, statusList);
		for (Job job : jobList) {
			JobResponse jobResponse = frameJobResponse(job);

			jobResponseList.add(jobResponse);
		}
		return jobResponseList;
	}
	
	public List<JobResponse> getPlaceholderJobs(int systemRequestId){
		Request systemRequest = requestDao.findById(systemRequestId).get();

		return getPlaceholderJobs(systemRequest);
	}
	
	public List<JobResponse> getPlaceholderJobs(Request systemRequest){
		List<JobResponse> jobResponseList = new ArrayList<JobResponse>();
						
		List<Job> jobList = jobManipulator.getJobs(systemRequest);
		
		for (Job job : jobList) {
			JobResponse jobResponse = frameJobResponse(job);

			jobResponseList.add(jobResponse);
		}
		return jobResponseList;
	}
	
	public List<GroupedJobResponse> getGroupedPlaceholderJobs(Request systemRequest, int sourceArtifactId){
		List<GroupedJobResponse> groupedJobResponseList = new ArrayList<GroupedJobResponse>();
		Map<String, GroupedJobResponse> columnName_GroupedJobResponse_Map = new HashMap<String, GroupedJobResponse>();
		
		List<Job> jobList = jobManipulator.getJobs(systemRequest);
	
		List<JobResponse> jobResponseList = new ArrayList<JobResponse>();
		for (Job job : jobList) {
			JobResponse jobResponse = frameJobResponse(job);
			jobResponseList.add(jobResponse);
			
			String processingTask = jobResponse.getProcessingTask();
			String storageTask = jobResponse.getStoragetaskAction();
			if(processingTask != null && (processingTask.equals("file-copy") || processingTask.equals("video-mam-update"))) {
				GroupedJobResponse groupedJobResponse = new GroupedJobResponse();
				groupedJobResponse.setColumnName(jobResponse.getProcessingTask());
				groupedJobResponse.setStatus(jobResponse.getStatus());
				groupedJobResponse.setMessage(jobResponse.getMessage());
				List<JobResponse> jobResponseArrayList = new ArrayList<JobResponse>();
				jobResponseArrayList.add(jobResponse);
				groupedJobResponse.setPlaceholderJob(jobResponseArrayList);
				groupedJobResponseList.add(groupedJobResponse);
			}else if(storageTask != null && storageTask.equals("write")) {
				GroupedJobResponse groupedJobResponse = new GroupedJobResponse();
				Integer copyId = jobResponse.getCopy();
				String columnName = getColumnName(copyId, jobResponse, sourceArtifactId);
				groupedJobResponse.setColumnName(columnName);
				
				groupedJobResponse.setStatus(jobResponse.getStatus());
				groupedJobResponse.setMessage(jobResponse.getMessage());

				List<JobResponse> jobResponseArrayList = new ArrayList<JobResponse>();
				jobResponseArrayList.add(jobResponse);
				groupedJobResponse.setPlaceholderJob(jobResponseArrayList);
				groupedJobResponseList.add(groupedJobResponse);
				columnName_GroupedJobResponse_Map.put(columnName, groupedJobResponse);
			}
		}
		
		
		for (JobResponse nthJobResponse : jobResponseList) {
			String processingTask = nthJobResponse.getProcessingTask();
			String storageTask = nthJobResponse.getStoragetaskAction();

			Integer copyId = nthJobResponse.getCopy();
			if(copyId != null) {
				String columnName = getColumnName(copyId, nthJobResponse, sourceArtifactId);
				
				if((storageTask != null && storageTask.equals("restore")) || (processingTask != null && processingTask.equals("checksum-verify"))) {
					GroupedJobResponse groupedJobResponse = columnName_GroupedJobResponse_Map.get(columnName);
					List<JobResponse> jobResponseArrayList = groupedJobResponse.getPlaceholderJob();
					jobResponseArrayList.add(nthJobResponse);
					groupedJobResponse.setPlaceholderJob(jobResponseArrayList);
					
					String groupedJobStatus = groupedJobResponse.getStatus();
					String currentJobStatus = nthJobResponse.getStatus();
					if(groupedJobStatus != null) {
						List<Status> jobStatusList = new ArrayList<Status>();
						jobStatusList.add(Status.valueOf(groupedJobStatus));
						
						if(currentJobStatus == null) // means job not created just yet...
							currentJobStatus = Status.queued.name();
						jobStatusList.add(Status.valueOf(currentJobStatus));
					
						Status status = StatusUtil.getStatus(jobStatusList);
						groupedJobResponse.setStatus(status.name());
					}
					groupedJobResponse.setMessage(StringUtils.isNotBlank(groupedJobResponse.getMessage()) ? groupedJobResponse.getMessage() : "" + nthJobResponse.getMessage());
				}
			}
		}
		return groupedJobResponseList;
	}
	
	private String getColumnName(Integer copyId, JobResponse jobResponse, int sourceArtifactId) {
		String columnName = "copy-" + copyId;
		if(jobResponse.getInputArtifactId() != null && jobResponse.getInputArtifactId() == sourceArtifactId)
			columnName = "Raw " + columnName;
		else
			columnName = "Proxy " + columnName;
		return columnName;
	}
	
	public List<JobResponse> createDependentJobs(int jobId) throws Exception{
		
		List<JobResponse> jobResponseList = new ArrayList<JobResponse>();
		Job job = jobDao.findById(jobId).get();
		List<Job> jobList = jobCreator.createDependentJobs(job);
		
		for (Job nthJob : jobList) {
			JobResponse jobResponse = frameJobResponse(nthJob);
			jobResponseList.add(jobResponse);
		}
		return jobResponseList;
	}	
	
	public JobResponse requeueJob(int jobId) throws Exception{
		return frameJobResponse(jobServiceRequeueHelper.requeueJob(jobId, getUserFromContext()));
	}

	public JobResponse markedCompletedJob(int jobId, String reason) throws Exception {
		Request userRequest = null;
		String userName = getUserFromContext();
		Job job = null;
		try {		
			job = jobDao.findById(jobId).get();
			if(job.getStatus() != Status.completed_failures && job.getStatus() != Status.failed)
				throw new DwaraException("Job cannot be marked completed. Only failed or a job completed with some failures can be rerun. @TEAM - Any extra protection needed for avoiding written content getting requeued again?"); //
	    	
			userRequest = new Request();
	    	userRequest.setType(RequestType.user);
			userRequest.setActionId(Action.marked_completed);

			User requestedByUser = userDao.findByName(userName);
	    	userRequest.setRequestedBy(requestedByUser);
			userRequest.setRequestedAt(LocalDateTime.now());
			userRequest.setCompletedAt(LocalDateTime.now());
			RequestDetails details = new RequestDetails();
			ObjectMapper mapper = new ObjectMapper();
	
			HashMap<String, Object> data = new HashMap<String, Object>();
	    	data.put("jobId", jobId);
	    	
	    	String jsonAsString = mapper.writeValueAsString(data);
			JsonNode postBodyJson = mapper.readValue(jsonAsString, JsonNode.class);
			details.setBody(postBodyJson);
			userRequest.setDetails(details);
			
	    	userRequest = requestDao.save(userRequest);
	    	int userRequestId = userRequest.getId();
	    	logger.info(DwaraConstants.USER_REQUEST + userRequestId);

			// update the reference table    
			long jobRunCount = jobRunDao.countByJobId(jobId);
	    	int nextId = (int) (jobRunCount + 1);
	    	JobRun jobRun = new JobRun();
	    	jobRun.setId(new JobRunKey(jobId, nextId));
	    	jobRun.setJob(job);
	    	jobRun.setStartedAt(job.getStartedAt());
	    	jobRun.setCompletedAt(job.getCompletedAt());
	    	jobRun.setStatus(job.getStatus());
	    	jobRun.setMessage(job.getMessage());
	    	jobRun.setDevice(job.getDevice());
	    	jobRun.setVolume(job.getVolume());
	    	jobRunDao.save(jobRun);
	    	logger.debug("JobRun record created successfully " + jobId + ":" + nextId);
	    	
	    	if(job.getProcessingtaskId() != null) {
				List<ProcessingFailure> processingFailureList = processingFailureDao.findAllByJobId(jobId);
				processingFailureDao.deleteAll(processingFailureList);
		    	logger.debug("Processing failure records cleaned up " + jobId);
	    	}
			
			job.setMessage(reason);
			job.setStatus(Status.marked_completed);
			job.setStartedAt(LocalDateTime.now());
			job.setCompletedAt(LocalDateTime.now());
			job = jobDao.save(job);
			logger.info("Job mark completed successfully " + jobId);
			
			Request jobSystemRequest = job.getRequest();
			jobSystemRequest.setStatus(Status.marked_completed);
			requestDao.save(jobSystemRequest);
			
			userRequest.setStatus(Status.completed);
			userRequest = requestDao.save(userRequest);

			List<TTFileJob> jobFileList = tFileJobDao.findAllByJobId(jobId); 
			if(jobFileList != null && jobFileList.size() > 0)
				tFileJobDao.deleteAll(jobFileList);
		} catch (Exception e) {
			if(userRequest != null && userRequest.getId() != 0) {
				userRequest.setStatus(Status.failed);
				userRequest = requestDao.save(userRequest);
			}
			throw e;
		}

		return frameJobResponse(job);
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
			jobResponse.setJobId(jobId + "");
			List<Integer> jobDependencyIdsAsIntegerList = job.getDependencies();
			if(jobDependencyIdsAsIntegerList != null) {
				List<String> jobDependencyIdsAsStringList = new ArrayList<>(jobDependencyIdsAsIntegerList.size());
				for (Integer nthJobDependencyIdAsInteger : jobDependencyIdsAsIntegerList) { 
				  jobDependencyIdsAsStringList.add(String.valueOf(nthJobDependencyIdAsInteger)); 
				}
				jobResponse.setDependencies(jobDependencyIdsAsStringList);
			}
		}
		jobResponse.setRequestId(job.getRequest().getId());
		Action storagetaskAction = job.getStoragetaskActionId();
		if(storagetaskAction != null)
			jobResponse.setStoragetaskAction(storagetaskAction.name());
		String processingtaskId = job.getProcessingtaskId();
		jobResponse.setProcessingTask(processingtaskId);
			jobResponse.setFlowelementId(job.getFlowelementId());
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

