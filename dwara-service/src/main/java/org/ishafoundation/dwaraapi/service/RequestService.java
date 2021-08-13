package org.ishafoundation.dwaraapi.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.resp.job.JobResponse;
import org.ishafoundation.dwaraapi.api.resp.request.RequestResponse;
import org.ishafoundation.dwaraapi.api.resp.restore.File;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Tag;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact1;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.JobDetailsType;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.common.ArtifactDeleter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequestService extends DwaraService{

	private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

	@Autowired
	private RequestDao requestDao;

	@Autowired
	private JobDao jobDao;

	@Autowired
	private DomainUtil domainUtil;

	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;

	@Autowired
	private JobService jobService;
	
	@Autowired
	private VolumeService volumeService;
	
	@Autowired
	private ArtifactDeleter artifactDeleter; 

	
    public RequestResponse getRequest(int requestId) throws Exception{
    	Optional<Request> requestRecord = requestDao.findById(requestId);
    	if(!requestRecord.isPresent())
    		throw new Exception(requestId + " Request Id not found in the system");
    	
    	Request request = requestRecord.get();
    	RequestType requestType = request.getType();
		return frameRequestResponse(request, requestType, requestType == RequestType.user ? request.getId() : request.getRequestRef().getId(), null);
	}

	public List<RequestResponse> getRequests(RequestType requestType, List<Action> action, List<Status> statusList, List<User> requestedByList, Date requestedFrom, Date requestedTo, Date completedFrom, Date completedTo, String artifactName, List<String> artifactclassList, JobDetailsType jobDetailsType){
		List<RequestResponse> requestResponseList = new ArrayList<RequestResponse>();
		logger.info("Retrieving requests " + (requestType != null ? requestType.name() : null) + ":" + action + ":" + statusList);

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

		List<Request> requestList = requestDao.findAllDynamicallyBasedOnParamsOrderByLatest(requestType, action, statusList, requestedByList, requestedAtStart, requestedAtEnd, completedAtStart, completedAtEnd, artifactName, artifactclassList, pageNumber, pageSize);
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
    	requestToBeCancelled.setMessage(reason);
    	if(requestToBeCancelled.getActionId() == Action.ingest) { // TODO should this behaviour be same for digi and video-pub or should we not move video-pub to cancelled dir...
    		return cancelAndCleanupRequest(requestToBeCancelled);
    	} 
    	else if(requestToBeCancelled.getActionId() == Action.restore || requestToBeCancelled.getActionId() == Action.restore_process) {
    		return cancelRequest(requestToBeCancelled, true);
    	}
    	else {
    		return cancelRequestOnlyIfAllJobsQueued(requestToBeCancelled);
    	}
    }
    
    private RequestResponse cancelRequestOnlyIfAllJobsQueued(Request requestToBeCancelled) throws Exception{
    	return cancelRequest(requestToBeCancelled, false);
    }

	private RequestResponse cancelRequest(Request requestToBeCancelled, boolean force) throws Exception{
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

			Request userRequestToBeCancelled = requestToBeCancelled.getRequestRef();
			userRequestToBeCancelled.setStatus(Status.cancelled);
			requestDao.save(userRequestToBeCancelled);
			
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
    
    private RequestResponse cancelAndCleanupRequest(Request requestToBeCancelled) throws Exception{
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
			
			Domain domain = artifactclass.getDomain();
			ArtifactRepository artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
			
			artifactDeleter.cleanUp(userRequest, requestToBeCancelled, domain, artifactRepository);

			requestToBeCancelled.setStatus(Status.cancelled);
	    	requestDao.save(requestToBeCancelled);
	    	logger.info(requestToBeCancelled.getId() + " - Cancelled");
			
	    	userRequest.setRequestRef(requestToBeCancelled);
	    	userRequest.setStatus(Status.completed);
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
			if(requestAction == Action.ingest) {
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
				Domain domain = domainUtil.getDomain(artifactclassId);
				ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);

				Artifact systemArtifact = artifactRepository.findTopByWriteRequestIdOrderByIdAsc(requestId); // TODO use Artifactclass().isSource() instead of orderBy
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

					//tag
					if(systemArtifact instanceof Artifact1) {
						Artifact1 a1 = (Artifact1) systemArtifact;
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
				Domain domain = domainUtil.getDomain(request);
				if(domain == null)
					domain = domainUtil.getDefaultDomain();
				
				org.ishafoundation.dwaraapi.db.model.transactional.domain.File fileFromDB = domainUtil.getDomainSpecificFile(domain, request.getDetails().getFileId());
			
				File fileForRestoreResponse = new File();
				byte[] checksum = fileFromDB.getChecksum();
				if(checksum != null)
					fileForRestoreResponse.setChecksum(Hex.encodeHexString(checksum));
				
				fileForRestoreResponse.setId(fileFromDB.getId());
				fileForRestoreResponse.setPathname(fileFromDB.getPathname());
				fileForRestoreResponse.setSize(fileFromDB.getSize());
				fileForRestoreResponse.setSystemRequestId(request.getId());
				requestResponse.setFile(fileForRestoreResponse);
				
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

}

