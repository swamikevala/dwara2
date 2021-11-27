package org.ishafoundation.dwaraapi.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.resp.job.JobResponse;
import org.ishafoundation.dwaraapi.api.resp.request.RequestResponse;
import org.ishafoundation.dwaraapi.api.resp.request.RestoreFile;
import org.ishafoundation.dwaraapi.api.resp.request.RestoreResponse;
import org.ishafoundation.dwaraapi.api.resp.request.Tape;
import org.ishafoundation.dwaraapi.api.resp.restore.File;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ArtifactVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.FileVolumeDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Tag;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileVolume;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.ishafoundation.dwaraapi.enumreferences.JobDetailsType;
import org.ishafoundation.dwaraapi.enumreferences.Priority;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.common.ArtifactDeleter;
import org.ishafoundation.dwaraapi.storage.storagetask.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class RequestService extends DwaraService{

	private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

	@Autowired
	private RequestDao requestDao;

	@Autowired
	private JobDao jobDao;

	@Autowired
	private ArtifactDao artifactDao;

	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;

	@Autowired
	private JobService jobService;
	
	@Autowired
	private VolumeService volumeService;
	
	@Autowired
	private ArtifactDeleter artifactDeleter;

	@Autowired
	private ArtifactVolumeDao artifactVolumeDao;
	
	@Autowired
	private FileVolumeDao fileVolumeDao;
	
	@Autowired
	private Restore restoreTaskAction;
	

	
    public RequestResponse getRequest(int requestId) throws Exception{
    	Optional<Request> requestRecord = requestDao.findById(requestId);
    	if(!requestRecord.isPresent())
    		throw new Exception(requestId + " Request Id not found in the system");
    	
    	Request request = requestRecord.get();
    	RequestType requestType = request.getType();
		return frameRequestResponse(request, requestType, requestType == RequestType.user ? request.getId() : request.getRequestRef().getId(), null);
	}

	@Override
	protected JsonNode getRequestDetails(Object payload) {
		return super.getRequestDetails(payload);
	}

	public List<RequestResponse> getRequests(RequestType requestType, List<Action> actionList, List<Status> statusList, List<User> requestedByList, Date requestedFrom, Date requestedTo, Date completedFrom, Date completedTo, String artifactName, List<String> artifactclassList, JobDetailsType jobDetailsType){
		List<RequestResponse> requestResponseList = new ArrayList<RequestResponse>();
		logger.info("Retrieving requests " + (requestType != null ? requestType.name() : null) + ":" + actionList + ":" + statusList);

		LocalDateTime requestedAtStart = requestedFrom != null ? requestedFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
		LocalDateTime requestedAtEnd = requestedTo != null ? requestedTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
		// requested to hardcode...
		if(requestedAtEnd != null) {
			requestedAtEnd = requestedAtEnd.plusHours(23);
			requestedAtEnd = requestedAtEnd.plusMinutes(59);
			requestedAtEnd = requestedAtEnd.plusSeconds(59);
		}
		
		LocalDateTime completedAtStart = completedFrom != null ? completedFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
		LocalDateTime completedAtEnd = completedTo != null ? completedTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
		// requested to hardcode...
		if(completedAtEnd != null) {
			completedAtEnd = completedAtEnd.plusHours(23);
			completedAtEnd = completedAtEnd.plusMinutes(59);
			completedAtEnd = completedAtEnd.plusSeconds(59);
		}
		
		int pageNumber = 0;
		int pageSize = 0;

		List<Request> requestList = requestDao.findAllDynamicallyBasedOnParamsOrderByLatest(requestType, actionList, statusList, requestedByList, requestedAtStart, requestedAtEnd, completedAtStart, completedAtEnd, artifactName, artifactclassList, pageNumber, pageSize);
		for (Request request : requestList) {
			logger.trace("Now processing " + request.getId());
			RequestResponse requestResponse = frameRequestResponse(request, requestType, requestType == RequestType.user ? request.getId() : request.getRequestRef().getId(), jobDetailsType);
			requestResponseList.add(requestResponse);
		}
		return requestResponseList;
	}
	
//	public RequestResponse deleteRequest(int requestId) throws Exception{
//		logger.info("Deleting request " + requestId);
//		Request userRequest = null;
//		try {
//			userRequest = artifactService.cancelRequest(requestId);
//			
//			return frameRequestResponse(userRequest, RequestType.user);
//		}
//		catch (Exception e) {
//			if(userRequest != null && userRequest.getId() != 0) {
//				userRequest.setStatus(Status.failed);
//				userRequest = requestDao.save(userRequest);
//			}
//			throw e;
//		}
//	}
    
    public RequestResponse cancelRequest(int requestId, String reason) throws Exception{
    	Request requestToBeCancelled = requestDao.findById(requestId).get();
    	if(requestToBeCancelled.getActionId() == Action.ingest) { // TODO should this behaviour be same for digi and video-pub or should we not move video-pub to cancelled dir...
    		return cancelAndCleanupRequest(requestToBeCancelled, reason);
    	} 
    	else if(requestToBeCancelled.getActionId() == Action.restore || requestToBeCancelled.getActionId() == Action.restore_process) {
    		return cancelRequest(requestToBeCancelled, reason, true);
    	}
    	else {
    		return cancelRequestOnlyIfAllJobsQueued(requestToBeCancelled, reason);
    	}
    }
    
    private RequestResponse cancelRequestOnlyIfAllJobsQueued(Request requestToBeCancelled, String reason) throws Exception{
    	return cancelRequest(requestToBeCancelled, reason, false);
    }

	private RequestResponse cancelRequest(Request requestToBeCancelled, String reason, boolean force) throws Exception{
		int requestId = requestToBeCancelled.getId();
		logger.info("Cancelling request " + requestId);
		Request userRequest = null;
		try {
			List<Job> jobList = jobDao.findAllByRequestId(requestId);
			for (Job job : jobList) {
				if(force) { // irrespective of the status of job just mark it cancelled... - TODO: Only static status or allow dynamically transitioning statuses too
					job.setStatus(Status.cancelled);
				}
				else {
					if(job.getStatus() == Status.queued)
						job.setStatus(Status.cancelled);
					else
						throw new DwaraException(requestId + " request cannot be cancelled. All jobs should be in queued status");
				}
			}
			
			HashMap<String, Object> data = new HashMap<String, Object>();
	    	data.put("requestId", requestId);
			userRequest = createUserRequest(Action.cancel, Status.in_progress, data);
	    	int userRequestId = userRequest.getId();
	    	logger.info(DwaraConstants.USER_REQUEST + userRequestId);
	
			
			jobDao.saveAll(jobList);
			
			requestToBeCancelled.setStatus(Status.cancelled);
			requestDao.save(requestToBeCancelled);

//			Request userRequestToBeCancelled = requestToBeCancelled.getRequestRef();
//			userRequestToBeCancelled.setStatus(Status.cancelled);
//			requestDao.save(userRequestToBeCancelled);
			
			userRequest.setStatus(Status.completed);
			userRequest.setCompletedAt(LocalDateTime.now());
			userRequest.setMessage(reason);
			userRequest = requestDao.save(userRequest);
			
	        return frameRequestResponse(userRequest, RequestType.user);
		}
		catch (Exception e) {
			if(userRequest != null && userRequest.getId() != 0) {
				userRequest.setStatus(Status.failed);
				userRequest = requestDao.save(userRequest);
			}
			throw e;
		}
	}
    
    private RequestResponse cancelAndCleanupRequest(Request requestToBeCancelled, String reason) throws Exception{
		Request userRequest = null;
		try {
	    	String artifactclassId = requestToBeCancelled.getDetails().getArtifactclassId();
	    	artifactDeleter.validateArtifactclass(artifactclassId);
	    	
	    	artifactDeleter.validateRequest(requestToBeCancelled);
	    	
	    	artifactDeleter.validateJobsAndUpdateStatus(requestToBeCancelled);
	    	
			// Step 1 - Create User Request
			HashMap<String, Object> data = new HashMap<String, Object>();
	    	data.put("requestId", requestToBeCancelled.getId());
	    	userRequest = createUserRequest(Action.cancel, Status.in_progress, data);
	    	
			
			Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
			
			artifactDeleter.cleanUp(userRequest, requestToBeCancelled);

			requestToBeCancelled.setStatus(Status.cancelled);
	    	requestDao.save(requestToBeCancelled);
	    	logger.info(requestToBeCancelled.getId() + " - Cancelled");
			
	    	userRequest.setRequestRef(requestToBeCancelled);
	    	userRequest.setStatus(Status.completed);
			userRequest.setCompletedAt(LocalDateTime.now());
			userRequest.setMessage(reason);
	        requestDao.save(userRequest);
	        logger.info(userRequest.getId() + " - Completed");
	        
	        return frameRequestResponse(userRequest, RequestType.user);
	        //return frameRequestResponse(requestToBeCancelled, RequestType.system);
		}
		catch (Exception e) {
			if(userRequest != null && userRequest.getId() != 0) {
				userRequest.setStatus(Status.failed);
				userRequest = requestDao.save(userRequest);
			}
			throw e;
		}
    }
    


	public RequestResponse releaseRequest(List<Integer> requestIdList)  throws Exception {
		Request userRequest = null;
		try {
			for (Integer requestId : requestIdList) {
				validateReleaseRequest(requestId);
			}
			
			HashMap<String, Object> data = new HashMap<String, Object>();
	    	data.put("requestIds", requestIdList);
			userRequest = createUserRequest(Action.release, Status.in_progress, data);
	    	int userRequestId = userRequest.getId();
	    	logger.info(DwaraConstants.USER_REQUEST + userRequestId);

	    	for (Integer requestId : requestIdList) {
	    		releaseJobsAndSystemRequest(requestId);
			}
	    	
			userRequest.setStatus(Status.completed);
			userRequest = requestDao.save(userRequest);

	    	return frameRequestResponse(userRequest, RequestType.user);	
		}catch (Exception e) {
			if(userRequest != null && userRequest.getId() != 0) {
				userRequest.setStatus(Status.failed);
				userRequest = requestDao.save(userRequest);
			}
			throw e;
		}
		
	}
	
	public RequestResponse releaseRequest(int requestId) throws Exception{
		logger.info("Releasing request " + requestId);
		Request userRequest = null;
		try {
			validateReleaseRequest(requestId);

			HashMap<String, Object> data = new HashMap<String, Object>();
	    	data.put("requestId", requestId);
			userRequest = createUserRequest(Action.release, Status.in_progress, data);
	    	int userRequestId = userRequest.getId();
	    	logger.info(DwaraConstants.USER_REQUEST + userRequestId);
	
	    	releaseJobsAndSystemRequest(requestId);
	    	
			userRequest.setStatus(Status.completed);
			userRequest = requestDao.save(userRequest);
			
			return frameRequestResponse(userRequest, RequestType.user);
		}
		catch (Exception e) {
			if(userRequest != null && userRequest.getId() != 0) {
				userRequest.setStatus(Status.failed);
				userRequest = requestDao.save(userRequest);
			}
			throw e;
		}
	}
	
	public RequestResponse changePriority(int requestId, String reqPriority) throws Exception{
		
		Priority priority = null;
		try {
			priority = Priority.valueOf(reqPriority);
			if(priority == null)
				throw new Exception(reqPriority + " not supported");
				
		}catch (Exception e) {
			throw new Exception(reqPriority + " not supported");
		}
		
		Optional<Request> requestOptional = requestDao.findById(requestId);
		if(!requestOptional.isPresent())
			throw new Exception(requestId + " not in system");
		
		Request request = requestOptional.get();
		if(request.getStatus() == Status.completed)
			throw new Exception(requestId + " already completed. Changing priority doesnt make sense");
		
		Priority oldPriority = request.getPriority();
		
		if(oldPriority != null & oldPriority == priority)
			throw new Exception(requestId + " hasnt got priority changed. Old priority = New - " + priority);
		
		request.setPriority(priority);
		requestDao.save(request);
		
		if(request.getType() == RequestType.user) {
			List<Request> systemRequestList = requestDao.findAllByRequestRefId(requestId);

			for (Request nthSystemRequest : systemRequestList) {
				nthSystemRequest.setPriority(priority);
			}
			
			if(systemRequestList.size() > 0)
				requestDao.saveAll(systemRequestList);
		}

		logger.info(requestId + " priority changed from " + oldPriority + " to " + priority);
		return frameRequestResponse(request, request.getType());
	}
	
	private void validateReleaseRequest(int requestId){
		boolean anyToBeReleased = false;
		List<Job> jobList = jobDao.findAllByRequestId(requestId);
		for (Job job : jobList) {
			if(job.getStatus() == Status.on_hold) {
				anyToBeReleased = true;
			}
		}

		if(!anyToBeReleased)
			throw new DwaraException(requestId + " has no jobs in on_hold status to be released");
	}
	
	private void releaseJobsAndSystemRequest(int requestId){
		List<Job> jobList = jobDao.findAllByRequestId(requestId);
		for (Job job : jobList) {
			if(job.getStatus() == Status.on_hold) {
				job.setStatus(Status.queued);
			}
		}
		jobDao.saveAll(jobList);
		
		Request requestToBeReleased = requestDao.findById(requestId).get();
		requestToBeReleased.setStatus(Status.queued);
		requestDao.save(requestToBeReleased);
	}
	
	public RequestResponse frameRequestResponse(Request request, RequestType requestType){
		return frameRequestResponse(request, requestType, null, null);
	}
	
	public RequestResponse frameRequestResponse(Request request, RequestType requestType, Integer userReqId, JobDetailsType jobDetailsType){
		RequestResponse requestResponse = new RequestResponse();
		int requestId = request.getId();
		
		requestResponse.setId(requestId);
		requestResponse.setType(request.getType().name());
		if(requestType == RequestType.system) {
			if(userReqId == null)
				userReqId = request.getRequestRef().getId(); // This would make a DB call to request table. For user requestType calls when on loop on systemRequests this would unnecessarily make DB call to get the already available userReqId 
			requestResponse.setUserRequestId(userReqId);
		}
		requestResponse.setRequestedAt(getDateForUI(request.getRequestedAt()));
		requestResponse.setCompletedAt(getDateForUI(request.getCompletedAt()));
		requestResponse.setRequestedBy(request.getRequestedBy().getName());
				
		requestResponse.setStatus(request.getStatus().name());
		Action requestAction = request.getActionId();
		requestResponse.setAction(requestAction.name());
		if(requestType == RequestType.user) {
			if(requestAction == Action.ingest || requestAction == Action.restore || requestAction == Action.restore_process) {
				List<RequestResponse> systemRequestResponseList = new ArrayList<RequestResponse>();
				List<Request> systemRequestList = requestDao.findAllByRequestRefId(requestId);

				for (Request nthSystemRequest : systemRequestList) {
					systemRequestResponseList.add(frameRequestResponse(nthSystemRequest, RequestType.system, userReqId, jobDetailsType));
				}
				requestResponse.setRequest(systemRequestResponseList);
			}
		}
		if(requestType == RequestType.system) {		
			if(requestAction == Action.ingest) {
				String artifactclassId = request.getDetails().getArtifactclassId();
				Artifact systemArtifact = artifactDao.findTopByWriteRequestIdOrderByIdAsc(requestId); // TODO use Artifactclass().isSource() instead of orderBy
				if(systemArtifact != null) {
					org.ishafoundation.dwaraapi.api.resp.request.Artifact artifactForResponse = new org.ishafoundation.dwaraapi.api.resp.request.Artifact();
					artifactForResponse.setId(systemArtifact.getId());
					artifactForResponse.setName(systemArtifact.getName());
					artifactForResponse.setDisplayName(StringUtils.substringAfter(systemArtifact.getName(),"_"));
					artifactForResponse.setDeleted(systemArtifact.isDeleted());
					artifactForResponse.setArtifactclass(systemArtifact.getArtifactclass().getId());
					artifactForResponse.setPrevSequenceCode(systemArtifact.getPrevSequenceCode());
					artifactForResponse.setSequenceCode(systemArtifact.getSequenceCode());
					artifactForResponse.setRerunNo(request.getDetails().getRerunNo());
					artifactForResponse.setSkipActionElements(request.getDetails().getSkipActionelements());
					artifactForResponse.setStagedFilename(request.getDetails().getStagedFilename());
					artifactForResponse.setStagedFilepath(request.getDetails().getStagedFilepath());
					artifactForResponse.setSize(systemArtifact.getTotalSize());
					//tag
					if(systemArtifact instanceof Artifact) {
						Artifact a1 = (Artifact) systemArtifact;
						List<Tag> tags = new ArrayList<Tag>();
						if(a1.getTags() != null)
							tags = new ArrayList<Tag>(a1.getTags());
						List<String> listTags = new ArrayList<String>();
						for (Tag tag : tags) {
							listTags.add(tag.getTag());
						}
						artifactForResponse.setTags(listTags);
					}

					requestResponse.setArtifact(artifactForResponse);
				}
				if(jobDetailsType != null) {
					if(jobDetailsType == JobDetailsType.vanilla)
						requestResponse.setJob(jobService.getJobs(request.getId(), null));
					else if(jobDetailsType == JobDetailsType.placeholder)
						requestResponse.setJob(jobService.getPlaceholderJobs(request));
					else if(jobDetailsType == JobDetailsType.grouped_placeholder)
						requestResponse.setGroupedJob(jobService.getGroupedPlaceholderJobs(request, systemArtifact.getId()));
				}
				
				if(request.getStatus() == Status.failed || request.getStatus() == Status.completed_failures || request.getStatus() == Status.marked_failed) { // update the failure message for failed requests
					
					StringBuffer msgBfr = new StringBuffer();
					List<Status> statusList = new ArrayList<Status>();
					statusList.add(Status.failed);
					statusList.add(Status.completed_failures);
					statusList.add(Status.marked_failed);
					
					List<JobResponse> jobResponseList = jobService.getJobs(request.getId(), statusList);
					int cnt = 1;
					for (JobResponse jobResponse : jobResponseList) {
						if(cnt > 1)
							msgBfr.append(",");
						msgBfr.append((jobResponse.getProcessingTask() != null ? jobResponse.getProcessingTask() : jobResponse.getStoragetaskAction()) + "(" + jobResponse.getJobId() + ")-" + jobResponse.getMessage());
						cnt = cnt + 1;
					}
					
					requestResponse.setMessage(msgBfr.toString());
				}
			} 
			else if(requestAction == Action.restore || requestAction == Action.restore_process) {
				Optional<org.ishafoundation.dwaraapi.db.model.transactional.File> fileFromDBOptional = fileDao.findById(request.getDetails().getFileId());
				if(fileFromDBOptional.isPresent()) {// For re-import failed[failed as in size/filecount mismatch etc] imports we might have to remove files that are even requested for restore already ...
					org.ishafoundation.dwaraapi.db.model.transactional.File fileFromDB = fileFromDBOptional.get();
					File fileForRestoreResponse = new File();
					byte[] checksum = fileFromDB.getChecksum();
					if(checksum != null)
						fileForRestoreResponse.setChecksum(Hex.encodeHexString(checksum));
					
					fileForRestoreResponse.setId(fileFromDB.getId());
					fileForRestoreResponse.setPathname(fileFromDB.getPathname());
					fileForRestoreResponse.setSize(fileFromDB.getSize());
					fileForRestoreResponse.setSystemRequestId(request.getId());
					requestResponse.setFile(fileForRestoreResponse);
				}else {
					logger.error("File deleted for re-import " + request.getDetails().getFileId());
				}
				requestResponse.setCopyId(request.getDetails().getCopyId());
				requestResponse.setDestinationPath(request.getDetails().getDestinationPath());
				requestResponse.setOutputFolder(request.getDetails().getOutputFolder());
			}
			else if(requestAction == Action.initialize || requestAction == Action.finalize) {
				requestResponse.setVolume(volumeService.getVolume(request.getDetails().getVolumeId()));
			}
		}
		return requestResponse;
	}

	public List<RestoreResponse> getRestoreRequests(RequestType requestType, List<Status> statusList, Date requestedFrom, Date requestedTo, Date completedFrom, Date completedTo){
		List<RestoreResponse> restoreResponses = new ArrayList<>();

		List<Action> actionList = new ArrayList<>();
		actionList.add(Action.restore);
		actionList.add(Action.restore_process);
		
		//List<Request> userRequests = requestDao.findAllByActionIdInAndStatusInAndTypeAndRequestedByIdNotNullOrderByRequestedAtDesc(actionList, statusList, requestType);
		
		LocalDateTime requestedAtStart = requestedFrom != null ? requestedFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
		LocalDateTime requestedAtEnd = requestedTo != null ? requestedTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
		// requested to hardcode...
		if(requestedAtEnd != null) {
			requestedAtEnd = requestedAtEnd.plusHours(23);
			requestedAtEnd = requestedAtEnd.plusMinutes(59);
			requestedAtEnd = requestedAtEnd.plusSeconds(59);
		}
		
		LocalDateTime completedAtStart = completedFrom != null ? completedFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
		LocalDateTime completedAtEnd = completedTo != null ? completedTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
		// requested to hardcode...
		if(completedAtEnd != null) {
			completedAtEnd = completedAtEnd.plusHours(23);
			completedAtEnd = completedAtEnd.plusMinutes(59);
			completedAtEnd = completedAtEnd.plusSeconds(59);
		}
		
		int pageNumber = 0;
		int pageSize = 0;
		List<Request> userRequests = requestDao.findAllDynamicallyBasedOnParamsOrderByLatest(requestType, actionList, statusList, null, requestedAtStart, requestedAtEnd, completedAtStart, completedAtEnd, null, null, pageNumber, pageSize);

		
		logger.trace("User requests length before enthry: " + userRequests.size());
		for (Request request : userRequests) {
			List<Tape> tapes = new ArrayList<>();
			long userRequestEta = 0;
			boolean allTapesLoaded = true;
			logger.trace("User requests length: " + userRequests.size());
			RestoreResponse restoreResponse = new RestoreResponse();
			restoreResponse.setName(request.getDetails().getBody().get("outputFolder").textValue());
			restoreResponse.setUserRequestId(request.getId());
			logger.trace(request.getDetails().getBody().get("destinationPath").textValue());
			restoreResponse.setDestinationPath(request.getDetails().getBody().get("destinationPath").textValue());
			restoreResponse.setRequestedAt(request.getRequestedAt());
			restoreResponse.setRequestedBy(request.getRequestedBy().getName());
			restoreResponse.setStatus(String.valueOf(request.getStatus()));

			restoreResponse.setPriority(String.valueOf(request.getPriority()));

			List<RestoreFile> files = new ArrayList();
			List<Request> systemRequests = requestDao.findAllByRequestRefId(request.getId());
			long size = 0;
			long restoredSize = 0;
			long movConversionRate = 79; // For MOV
			long restorationRate = 10; // FOR RESTORATION
			for (Request systemRequest : systemRequests) {
				RestoreFile file = new RestoreFile();
				file.setSystemRequestId(systemRequest.getId());
				Optional<org.ishafoundation.dwaraapi.db.model.transactional.File> fileFromDBOptional = fileDao.findById(systemRequest.getDetails().getFileId());
				if(!fileFromDBOptional.isPresent())
					continue;				
				org.ishafoundation.dwaraapi.db.model.transactional.File fileFromDB = fileFromDBOptional.get();

				file.setName(fileFromDB.getPathname());
				file.setSize(fileFromDB.getSize());
				List<Job> fileJobs = jobDao.findAllByRequestId(systemRequest.getId());
				file.setStatus(String.valueOf(systemRequest.getStatus()));
				long startTime = 0;
				long restoreETA = 0;
				long postProcessETA = 0;
				try {
					file.setTape(getFileVolume(fileFromDB.getId(), 1).getVolume().getId());

				} catch (Exception e) {
					e.printStackTrace();
				}

				for (Job job : fileJobs) {
					file.setJobId(job.getId());
					if (job.getStoragetaskActionId() == Action.restore) {
						// If the task is restore task
						logger.debug("checking for file");

						if (job.getStatus().equals(Status.in_progress)) {
							startTime = job.getStartedAt().toEpochSecond(ZoneOffset.of("+05:30"));
							restoreETA = fileETARestoreCalculator(restoreTaskAction.getRestoreLocation(job), file, startTime, "restore");
							userRequestEta += restoreETA;
							restoredSize += getTargetSize(restoreTaskAction.getRestoreLocation(job), file);
						} else if (job.getStatus().equals(Status.queued)) {
							long expectedRestoreETA = fileETARestoreCalculator(restoreTaskAction.getRestoreLocation(job), file, startTime, "restore");
							userRequestEta += expectedRestoreETA;
						} else if (!job.getStatus().equals(Status.completed)) {

							restoreETA = 0;
							break;
						} else if (job.getStatus().equals(Status.completed)) {
							restoredSize += file.getSize();
						}

						// set the tape details
						Tape tape = new Tape();
						tape.setId(file.getTape());
						long timeElapsed = ((System.currentTimeMillis() / 1000)
								- job.getCreatedAt().toEpochSecond(ZoneOffset.of("+05:30")));
						if (job.getMessage() == null && timeElapsed >= 120)
							tape.setLoaded(true);
						else {
							tape.setLoaded(false);
							allTapesLoaded = false;
						}
						tapes.add(tape);

					} else if (job.getProcessingtaskId() == "video-digi-2020-mkv-mov-gen") {
						// If its inprogress calculate based on mov size. If its queued calculate based
						// on estimated speed 79seconds for 1GB conversion.
						// If its completed. Do nothing.
						if (job.getStatus().equals(Status.in_progress)) {
							// Get the .mov path from the target folder
							postProcessETA = fileETARestoreCalculator(restoreTaskAction.getRestoreLocation(job), file, startTime,
									"video-digi-2020-mkv-mov-gen");
							userRequestEta += postProcessETA;
						} else if (job.getStatus().equals(Status.queued)) {
							postProcessETA = ((file.getSize() / 1073741824) * restorationRate);
							userRequestEta += postProcessETA;

						}

					}

					/*
					 * if() startTime = job.getStartedAt().getSecond();
					 */

					// if (job.getStatus()!=Status.in_progress && job.getStatus()!=Status.completed)

				}

				// how to get startTime
				// Just add the restoreETA and postPRocess ETA at the end
				long totalETA = restoreETA + postProcessETA;

				file.setEta(totalETA);
				// file.setEta(getUserFromContext());
				if (file.getStatus() != String.valueOf(Status.cancelled))
					size += fileFromDB.getSize();
				files.add(file);
			}
			restoreResponse.setSize(size);
			long restoredPercentage = 0;
			logger.info(restoreResponse.getName());
			logger.info(String.valueOf(restoredSize));

			if (restoreResponse.getSize() > 0) {
				restoredPercentage = 100 * restoredSize / restoreResponse.getSize();
				restoreResponse.setPercentageRestored(restoredPercentage - 3);
				logger.info(String.valueOf(restoredPercentage));
			}

			restoreResponse.setRestoreFiles(files);
			restoreResponse.setTapes(tapes);
			if (allTapesLoaded) {
				// restoreResponse.setEta(userRequestEta);
			}
			// change this asap uncomment above one

			restoreResponse.setEta(userRequestEta);
			restoreResponse.setElapsedTime(getElapsedTime(restoreResponse.getRequestedAt()));
			restoreResponse.setElapsedTimeNumber(System.currentTimeMillis()/1000-restoreResponse.getRequestedAt().toEpochSecond(ZoneOffset.of("+05:30")));
			if (request.getStatus().equals(Status.queued))
				restoreResponse.setEta(0);
			restoreResponses.add(restoreResponse);
		}
		Collections.sort(restoreResponses);
		return restoreResponses;

	}

	private long fileETARestoreCalculator(String targetLocation, RestoreFile file, long startTime,
			String taskType) {
		long eta = 0;
		String path = targetLocation + java.io.File.separator + file.getName();
		java.io.File targetFile = new java.io.File(path);
		try {
			long movConversionRate = 79; // For MOV
			long restorationRate = 10; // FOR RESTORATION
			// EG:V27033_Ashram-Ambience-Shots_Dhyanalinga-IYC_28-Aug-2020_Drone/DCIM/100MEDIA/DJI_0018.MOV
			if (taskType == "video-digi-2020-mkv-mov-gen") {
				boolean foundMovFile = false;
				// Loop through the folder
				// destinationPath+"/"+outputFolder+"/"+".restoring/"+file.getName().parentfolder
				// VD2_I30_Isha-Fest_IYC_21-Sep-2003_Tamil_51mins-17secs_SDI-Digitized_Cam1/I030.mkv
				// BHi hei parey
				// VD2_I30_Isha-Fest_IYC_21-Sep-2003_Tamil_51mins-17secs_SDI-Digitized_Cam1/ Hei
				// parey
				// VD2_I30_Isha-Fest_IYC_21-Sep-2003_Tamil_51mins-17secs_SDI-Digitized_Cam1/I030.mov
				// Darkaar
				if (!targetFile.isDirectory()) {
					targetFile = new java.io.File(targetFile.getParent());
				}
				// Find the mov in the directory and seet it as a path
				List<java.io.File> searchTheseFiles = Arrays.asList(targetFile.listFiles());
				for (java.io.File goteyFile : searchTheseFiles) {
					if (goteyFile.getPath().endsWith(".mov")) {
						// Set this as the file path
						targetFile = goteyFile;
						foundMovFile = true;
					}
	
				}
	
				if (!foundMovFile) {
					return ((file.getSize() / 1073741824) * movConversionRate);
				}
			} // Mov gen completes here
			else if (taskType == "restore") {
				if (!targetFile.exists()) {
					return ((file.getSize() / 1073741824) * restorationRate);
				}
			}
	
			long targetSize = 0;
			targetSize = FileUtils.sizeOf(targetFile);
			long fileSize = file.getSize();
			logger.trace(String.valueOf(targetSize));
			logger.trace(String.valueOf(startTime));
			logger.trace(String.valueOf(fileSize));
			long remainingSize = (fileSize - targetSize) / (targetSize);
			logger.trace(String.valueOf(remainingSize));
			logger.trace(String.valueOf(System.currentTimeMillis() / 1000));
			eta = ((System.currentTimeMillis() / 1000) - startTime) * remainingSize;
		}catch (Exception e) {
			logger.warn("Unable to calc eta for " + path);
		}
		return eta;
	}

	private FileVolume getFileVolume(int fileIdToBeRestored, int copyNumber) throws Exception {
    	List<FileVolume> fileVolumeList = fileVolumeDao.findAllByIdFileIdAndVolumeGroupRefCopyId(fileIdToBeRestored, copyNumber);
    	FileVolume fileVolume = null;
    	if(fileVolumeList.size() > 1) {
    		
    		org.ishafoundation.dwaraapi.db.model.transactional.File file = fileDao.findById(fileIdToBeRestored).get();
    		Artifact artifact = file.getArtifact();
	    	for (FileVolume nthFileVolume : fileVolumeList) {
				ArtifactVolume artifactVolume = artifactVolumeDao.findByIdArtifactIdAndIdVolumeId(artifact.getId(), nthFileVolume.getId().getVolumeId());
				if(artifactVolume.getStatus() == ArtifactVolumeStatus.current || artifactVolume.getStatus() == null) {
					fileVolume = nthFileVolume;
					break;
				}
			}
    	}
    	else {
    		fileVolume = fileVolumeList.get(0);
    	}
    	return fileVolume;
	}

	private long getTargetSize(String targetLocation, RestoreFile file) {
		long targetSize = 0;
		String path = targetLocation + java.io.File.separator + file.getName();
		try {
			java.io.File targetFile = new java.io.File(path);
			if (targetFile.exists()) {
				targetSize = FileUtils.sizeOf(targetFile);
			}
		}catch (Exception e) {
			logger.warn("Unable to calc targetSize for " + path);
		}
		return targetSize;
	}
	private String getElapsedTime(LocalDateTime createdTime){

		long requestedAgo = System.currentTimeMillis()/1000-createdTime.toEpochSecond(ZoneOffset.of("+05:30"));
		long hours = requestedAgo/3600;
		long minutes = (requestedAgo-hours*3600)/60;
		String timeElapsed = hours+" Hours "+minutes + " Minutes ";
		return timeElapsed;
	}
}