package org.ishafoundation.dwaraapi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.resp.job.GroupedJobResponse;
import org.ishafoundation.dwaraapi.api.resp.job.JobResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ProcessingFailureDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ArtifactVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.FileVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.JobRunDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TFileVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TTFileJobDao;
import org.ishafoundation.dwaraapi.db.keys.JobRunKey;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.ProcessingFailure;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.JobRun;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TTFileJob;
import org.ishafoundation.dwaraapi.db.utils.JobUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
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
	private TTFileJobDao tFileJobDao;

	@Autowired
	private JobRunDao jobRunDao;
	
	@Autowired
	private TFileDao tFileDao;
	
	@Autowired
	private TFileVolumeDao tFileVolumeDao;

	@Autowired
	private JobUtil jobUtil;	
	
	@Autowired
	private ArtifactDao artifactDao;

	@Autowired
	private FileDao fileDao;

	@Autowired
	private FileVolumeDao fileVolumeDao;

	@Autowired
	private ArtifactVolumeDao artifactVolumeDao;
	
	
	public List<JobResponse> getJobs(Integer systemRequestId, List<Status> statusList) {
		return getJobs(systemRequestId, statusList, true);
	}
	
	public List<JobResponse> getJobs(Integer systemRequestId, List<Status> statusList, boolean filterCancelledAndDeletedOnes) {
		List<JobResponse> jobResponseList = new ArrayList<JobResponse>();
		
		List<Job> jobList = jobDao.findAllDynamicallyBasedOnParamsOrderByLatest(systemRequestId, statusList);
		for (Job job : jobList) {
			if(job.getRequest().getStatus() == Status.cancelled) // skip jobs whose requests are cancelled
				continue;
			
			Integer artifactId = job.getInputArtifactId();
			if(artifactId != null) { 
				Artifact artifact = artifactDao.findById(artifactId).get();
				if(artifact.isDeleted()) // skip jobs related to artifacts which are deleted...
					continue;
			}
			
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
		Job job = null;
		try {		
			job = jobDao.findById(jobId).get();
			if(job.getStatus() != Status.completed_failures && job.getStatus() != Status.failed)
				throw new DwaraException("Job cannot be marked completed. Only failed or a job completed with some failures can be rerun. @TEAM - Any extra protection needed for avoiding written content getting requeued again?"); //

			HashMap<String, Object> data = new HashMap<String, Object>();
	    	data.put("jobId", jobId);
			
			userRequest = createUserRequest(Action.marked_completed, data);

			createJobRunRecord(job, reason);

			job.setMessage(reason);
			job.setStatus(Status.marked_completed);
			job.setStartedAt(LocalDateTime.now());
			job.setCompletedAt(LocalDateTime.now());
			job = jobDao.save(job);
			logger.info("Job marked as completed successfully " + jobId);

	    	if(job.getProcessingtaskId() != null) {
				List<ProcessingFailure> processingFailureList = processingFailureDao.findAllByJobId(jobId);
				processingFailureDao.deleteAll(processingFailureList);
		    	logger.debug("Processing failure records cleaned up " + jobId);
		    	
				List<TTFileJob> jobFileList = tFileJobDao.findAllByJobId(jobId); 
				if(jobFileList != null && jobFileList.size() > 0)
					tFileJobDao.deleteAll(jobFileList);
	    	}
			
			Request jobSystemRequest = job.getRequest();
			jobSystemRequest.setStatus(Status.in_progress); // NOTE: Dont be tempted to update this to marked_completed - Let the scheduler do it...
			requestDao.save(jobSystemRequest);
			
			Request jobUserRequest = jobSystemRequest.getRequestRef();
			jobUserRequest.setStatus(Status.in_progress); // Resetting the Jobs' user Request too...
			requestDao.save(jobUserRequest);
			
			userRequest.setStatus(Status.completed);
			userRequest = requestDao.save(userRequest);
		} catch (Exception e) {
			if(userRequest != null && userRequest.getId() != 0) {
				userRequest.setStatus(Status.failed);
				userRequest = requestDao.save(userRequest);
			}
			throw e;
		}

		return frameJobResponse(job);
	}

	public JobResponse markedFailedJob(int jobId, String reason) throws Exception {
		Request userRequest = null;
		Job job = null;
		try {		
			job = jobDao.findById(jobId).get();
			if(job.getStatus() != Status.completed && job.getStatus() != Status.completed_failures)
				throw new DwaraException("Job " + jobId + " cannot be marked failed. Only jobs with completed or completed_failures status can marked failed");

			// should we validate the dependent jobs and their status too
			List<Job> dependentJobList = jobUtil.getDependentJobs(job);
			for (Job nthDependentJob : dependentJobList) {
				if(nthDependentJob.getStatus() == Status.in_progress)
					throw new DwaraException("Job " + jobId + " cannot be marked failed as its dependent job " + nthDependentJob.getId() + " is still " + nthDependentJob.getStatus());
			}
			
			HashMap<String, Object> data = new HashMap<String, Object>();
	    	data.put("jobId", jobId);
			
			userRequest = createUserRequest(Action.marked_failed, data);

			createJobRunRecord(job, reason);
			updateJobStatus(job, Status.marked_failed, reason);
			if(job.getStoragetaskActionId() == Action.write)
				delete_Artifact_File_TFile_PhysicalEntityRecords(job.getInputArtifactId(), job.getGroupVolume());
			
			StringBuffer impactedJobIdsForLogging = new StringBuffer();
			impactedJobIdsForLogging.append(jobId);

			for (Job nthDependentJob : dependentJobList) {
				createJobRunRecord(nthDependentJob, reason);
				updateJobStatus(nthDependentJob, Status.marked_failed, reason);

			    impactedJobIdsForLogging.append(nthDependentJob.getId());
			    logger.trace("Job marked as failed successfully " + nthDependentJob.getId());
			}
			logger.info("Following jobs marked failed successfully " + impactedJobIdsForLogging.toString());
			
			Request jobSystemRequest = job.getRequest();
			jobSystemRequest.setStatus(Status.marked_failed);
			requestDao.save(jobSystemRequest);
			
			userRequest.setStatus(Status.completed);
			userRequest = requestDao.save(userRequest);
		} catch (Exception e) {
			logger.error("Unable to mark fail job " + job.getId() + ":" + e.getMessage(),e);
			if(userRequest != null && userRequest.getId() != 0) {
				userRequest.setStatus(Status.failed);
				userRequest = requestDao.save(userRequest);
			}
			throw e;
		}

		// TODO Should show the list of impacted jobs including  
		return frameJobResponse(job);
	}

	
	private void createJobRunRecord(Job job, String reason) {
		int jobId = job.getId();
		// update the history reference table    
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
	} 	
	
	private void updateJobStatus(Job job, Status status, String reason) {
		job.setMessage(reason);
		job.setStatus(status);
//		job.setStartedAt(LocalDateTime.now());
//		job.setCompletedAt(LocalDateTime.now());
		job = jobDao.save(job);
		logger.trace("Job " + status + " - " + job.getId());
	}
	
	private void delete_Artifact_File_TFile_PhysicalEntityRecords(int artifactId, Volume groupVolume) throws Exception {
		Volume volumeInvolved = null;
		// Deleting the artifactvolume record
		List<ArtifactVolume> artifactVolumeList = artifactVolumeDao.findAllByIdArtifactIdAndStatus(artifactId, ArtifactVolumeStatus.current);
		if(artifactVolumeList == null || artifactVolumeList.size() == 0)
			artifactVolumeList = artifactVolumeDao.findAllByIdArtifactIdAndStatus(artifactId, null);
		for (ArtifactVolume nthArtifactVolume : artifactVolumeList) {
			
			if(nthArtifactVolume.getVolume().getGroupRef().getId() ==  groupVolume.getId()) {
				volumeInvolved = nthArtifactVolume.getVolume();	
				nthArtifactVolume.setStatus(ArtifactVolumeStatus.deleted);	
				artifactVolumeDao.save(nthArtifactVolume);
				break;
			}
		}		

	    // Now deleting the file/tfilevolume records
		// softDelete Filevolume entries
		Artifact artifact = artifactDao.findById(artifactId).get();
		List<org.ishafoundation.dwaraapi.db.model.transactional.File> artifactFileList = fileDao.findAllByArtifactIdAndDeletedFalse(artifact.getId());
		List<FileVolume> toBeUpdatedFileVolumeTableEntries = new ArrayList<FileVolume>();
		for (org.ishafoundation.dwaraapi.db.model.transactional.File nthFile : artifactFileList) {
			FileVolume fileVolume = fileVolumeDao.findByIdFileIdAndIdVolumeId(nthFile.getId(), volumeInvolved.getId());
			if(fileVolume != null) {
				fileVolume.setDeleted(true);
				toBeUpdatedFileVolumeTableEntries.add(fileVolume);
			}
		}
	    if(toBeUpdatedFileVolumeTableEntries.size() > 0) {
	    	fileVolumeDao.saveAll(toBeUpdatedFileVolumeTableEntries);
	    	logger.info("All FileVolume records for " + artifact.getName() + " [" + artifactId + "] in volume " + volumeInvolved.getId() + " flagged deleted successfully");
	    }
	    
	    // softDelete TFileVolume entries
	    List<TFile> artifactTFileList  = tFileDao.findAllByArtifactId(artifactId);
		if(artifactTFileList != null) { // An artifact can be deleted even after the tape is finalized at that point no TFile entries will be there
		    List<TFileVolume> toBeUpdatedTFileVolumeTableEntries = new ArrayList<TFileVolume>();
		    for (TFile nthTFile : artifactTFileList) {
		    	TFileVolume tFileVolume = tFileVolumeDao.findByIdFileIdAndIdVolumeId(nthTFile.getId(), volumeInvolved.getId());
				if(tFileVolume != null) {
					tFileVolume.setDeleted(true);
					toBeUpdatedTFileVolumeTableEntries.add(tFileVolume);
				}
		    }
		    if(toBeUpdatedTFileVolumeTableEntries.size() > 0) {
		    	tFileVolumeDao.saveAll(toBeUpdatedTFileVolumeTableEntries);
		    	logger.info("All TFileVolume records for " + artifact.getName() + " [" + artifactId + "] in volume " + volumeInvolved.getId() + " flagged deleted successfully");
		    }
		}
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

