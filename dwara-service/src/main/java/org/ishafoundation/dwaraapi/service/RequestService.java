package org.ishafoundation.dwaraapi.service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.resp.request.RequestResponse;
import org.ishafoundation.dwaraapi.api.resp.restore.File;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.CoreFlow;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
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
	private VolumeService volumeService;
	
	@Autowired
	private ArtifactDeleter artifactDeleter; 
	
	public List<RequestResponse> getRequests(RequestType requestType, List<Action> action, List<Status> statusList){
		List<RequestResponse> requestResponseList = new ArrayList<RequestResponse>();
		logger.info("Retrieving requests " + requestType.name() + ":" + action + ":" + statusList);
		
		String user = null;
		LocalDateTime fromDate = null;
		LocalDateTime toDate = null;
		int pageNumber = 0;
		int pageSize = 0;

		List<Request> requestList = requestDao.findAllDynamicallyBasedOnParamsOrderByLatest(requestType, action, statusList, user, fromDate, toDate, pageNumber, pageSize);
		for (Request request : requestList) {
			RequestResponse requestResponse = frameRequestResponse(request, requestType);
//			List<JobResponse> jobList = jobService.getPlaceholderJobs(request.getId());
//			requestResponse.setJobList(jobList);
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
    
    public RequestResponse cancelRequest(int requestId) throws Exception{
    	Request requestToBeCancelled = requestDao.findById(requestId).get();
    	if(requestToBeCancelled.getActionId() == Action.ingest) { // TODO should this behaviour be same for digi and video-pub or should we not move video-pub to cancelled dir...
    		return cancelAndCleanupRequest(requestToBeCancelled);
    	} else {
    		return cancelRequestOnlyIfAllJobsQueued(requestToBeCancelled);
    	}
    }

	private RequestResponse cancelRequestOnlyIfAllJobsQueued(Request requestToBeCancelled) throws Exception{
		int requestId = requestToBeCancelled.getId();
		logger.info("Cancelling request " + requestId);
		Request userRequest = null;
		try {
			List<Job> jobList = jobDao.findAllByRequestId(requestId);
			for (Job job : jobList) {
				if(job.getStatus() == Status.queued)
					job.setStatus(Status.cancelled);
				else
					throw new DwaraException(requestId + " request cannot be cancelled. All jobs should be in queued status");
			}
			
			HashMap<String, Object> data = new HashMap<String, Object>();
	    	data.put("requestId", requestId);
			userRequest = createUserRequest(Action.cancel, Status.in_progress, data);
	    	int userRequestId = userRequest.getId();
	    	logger.info(DwaraConstants.USER_REQUEST + userRequestId);
	
			
			jobDao.saveAll(jobList);
			
			requestToBeCancelled.setStatus(Status.cancelled);
			requestDao.save(requestToBeCancelled);
			
			userRequest.setStatus(Status.completed);
			userRequest = requestDao.save(userRequest);
			
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
			
			artifactDeleter.cleanUp(requestToBeCancelled, domain, artifactRepository);

			requestToBeCancelled.setStatus(Status.cancelled);
	    	requestDao.save(requestToBeCancelled);
	    	logger.info(requestToBeCancelled.getId() + " - Cancelled");
			
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
	
	private RequestResponse frameRequestResponse(Request request, RequestType requestType){
		RequestResponse requestResponse = new RequestResponse();
		int requestId = request.getId();
		
		requestResponse.setId(requestId);
		requestResponse.setType(request.getType().name());
		if(requestType == RequestType.system)
			requestResponse.setUserRequestId(request.getRequestRef().getId());
		requestResponse.setRequestedAt(getDateForUI(request.getRequestedAt()));
		requestResponse.setRequestedBy(request.getRequestedBy().getName());
		
		requestResponse.setStatus(request.getStatus().name());
		Action requestAction = request.getActionId();
		requestResponse.setAction(requestAction.name());
		if(requestType == RequestType.system) {
			if(requestAction == Action.ingest) {
				String artifactclassId = request.getDetails().getArtifactclassId();
				Domain domain = domainUtil.getDomain(artifactclassId);
				ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);

				Artifact systemArtifact = artifactRepository.findTopByWriteRequestIdOrderByIdAsc(requestId); 
				if(systemArtifact != null) {
					org.ishafoundation.dwaraapi.api.resp.request.Artifact artifactForResponse = new org.ishafoundation.dwaraapi.api.resp.request.Artifact();
					artifactForResponse.setId(systemArtifact.getId());
					artifactForResponse.setName(systemArtifact.getName());
					artifactForResponse.setDeleted(systemArtifact.isDeleted());
					artifactForResponse.setArtifactclass(systemArtifact.getArtifactclass().getId());
					artifactForResponse.setPrevSequenceCode(systemArtifact.getPrevSequenceCode());
					artifactForResponse.setRerunNo(request.getDetails().getRerunNo());
					artifactForResponse.setSkipActionElements(request.getDetails().getSkipActionelements());
					artifactForResponse.setStagedFilename(request.getDetails().getStagedFilename());
					artifactForResponse.setStagedFilepath(request.getDetails().getStagedFilepath());
					
					requestResponse.setArtifact(artifactForResponse);
				}
			} 
			else if(requestAction == Action.restore || (requestAction == Action.restore_process && CoreFlow.core_restore_checksumverify_flow.getFlowName().equals(request.getDetails().getFlowId()))) {
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

