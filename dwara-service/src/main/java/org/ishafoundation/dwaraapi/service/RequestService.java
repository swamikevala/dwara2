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
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private VolumeService volumeService;
	
	public List<RequestResponse> getRequests(RequestType requestType, List<Action> action, List<Status> statusList){
		List<RequestResponse> requestResponseList = new ArrayList<RequestResponse>();
		
//		List<Status> statusList = new ArrayList<Status>();
//		statusList.add(Status.queued);
//		statusList.add(Status.in_progress);
//		statusList.add(Status.failed);
//		
//		List<Request> systemRequestList = requestDao.findAllByTypeAndActionIdAndStatusInOrderByIdDesc(RequestType.system, Action.ingest, statusList);
		
		//List<Request> systemRequestList = requestDao.findAllByTypeAndActionIdAndStatusInOrderByIdDesc(requestType, action, statusList);
		
		String user = null;
		LocalDateTime fromDate = null;
		LocalDateTime toDate = null;
		int pageNumber = 0;
		int pageSize = 0;

		List<Request> requestList = requestDao.findAllDynamicallyBasedOnParamsOrderByLatest(requestType, action, statusList, user, fromDate, toDate, pageNumber, pageSize);

		for (Request request : requestList) {
			logger.trace("Getting details for " + request.getId());
			RequestResponse requestResponse = frameRequestResponse(request, requestType);

			requestResponseList.add(requestResponse);
		}
		return requestResponseList;
	}
	
	
	public RequestResponse cancelRequest(int requestId) throws Exception{
		Request userRequest = null;
		try {
			List<Job> jobList = jobDao.findAllByRequestId(requestId);
			for (Job job : jobList) {
				if(job.getStatus() == Status.queued)
					job.setStatus(Status.cancelled);
				else
					throw new DwaraException(requestId + " request cannot be cancelled. All jobs should be in queued status");
			}
			
			userRequest = new Request();
	    	userRequest.setType(RequestType.user);
			userRequest.setActionId(Action.cancel);
			userRequest.setStatus(Status.in_progress);
			User user = getUserObjFromContext();
	    	String requestedBy = user.getName();
	    	userRequest.setRequestedBy(user);
			userRequest.setRequestedAt(LocalDateTime.now());
			RequestDetails details = new RequestDetails();
			ObjectMapper mapper = new ObjectMapper();
	
			HashMap<String, Object> data = new HashMap<String, Object>();
	    	data.put("requestId", requestId);
	    	
	    	String jsonAsString = mapper.writeValueAsString(data);
			JsonNode postBodyJson = mapper.readValue(jsonAsString, JsonNode.class);
			details.setBody(postBodyJson);
			userRequest.setDetails(details);
			
	    	userRequest = requestDao.save(userRequest);
	    	int userRequestId = userRequest.getId();
	    	logger.info(DwaraConstants.USER_REQUEST + userRequestId);
	
			
			jobDao.saveAll(jobList);
			
			Request requestToBeCancelled = requestDao.findById(requestId).get();
			requestToBeCancelled.setStatus(Status.cancelled);
			requestDao.save(requestToBeCancelled);
			
			if(requestToBeCancelled.getActionId() == Action.ingest) {
				
				Domain domain = domainUtil.getDomain(requestToBeCancelled);
				ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
				Artifact artifact = artifactRepository.findByWriteRequestId(requestToBeCancelled.getId()); 
	
				String destRootLocation = requestToBeCancelled.getDetails().getStagedFilepath();
				if(destRootLocation != null) {
					try {
						java.io.File srcFile = FileUtils.getFile(artifact.getArtifactclass().getPathPrefix(), artifact.getName());
						java.io.File destFile = FileUtils.getFile(destRootLocation, Status.cancelled.name(), artifact.getName());
	
						if(srcFile.isFile())
							Files.createDirectories(Paths.get(FilenameUtils.getFullPathNoEndSeparator(destFile.getAbsolutePath())));		
						else
							Files.createDirectories(destFile.toPath());
		
						Files.move(srcFile.toPath(), destFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
					}
					catch (Exception e) {
						logger.error("Unable to move file "  + e.getMessage());
					}
				}
			}
			
			userRequest.setStatus(Status.completed);
			userRequest = requestDao.save(userRequest);
			
			return frameRequestResponse(requestToBeCancelled, RequestType.system);
		}
		catch (Exception e) {
			if(userRequest != null && userRequest.getId() != 0) {
				userRequest.setStatus(Status.failed);
				userRequest = requestDao.save(userRequest);
			}
			throw e;
		}
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
				Domain domain = domainUtil.getDomain(request);
				ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);

				Artifact systemArtifact = artifactRepository.findByWriteRequestId(requestId); 
				if(systemArtifact != null) {
					org.ishafoundation.dwaraapi.api.resp.request.Artifact artifactForResponse = new org.ishafoundation.dwaraapi.api.resp.request.Artifact();
					artifactForResponse.setArtifactclass(systemArtifact.getArtifactclass().getId());
					artifactForResponse.setPrevSequenceCode(systemArtifact.getPrevSequenceCode());
					artifactForResponse.setRerunNo(request.getDetails().getRerunNo());
					artifactForResponse.setSkipActionElements(request.getDetails().getSkipActionelements());
					artifactForResponse.setStagedFilename(request.getDetails().getStagedFilename());
					artifactForResponse.setStagedFilepath(request.getDetails().getStagedFilepath());
					
					requestResponse.setArtifact(artifactForResponse);
				}
			} 
			else if(requestAction == Action.restore) {
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
			}
			else if(requestAction == Action.initialize || requestAction == Action.finalize) {
				requestResponse.setVolume(volumeService.getVolume(request.getDetails().getVolumeId()));
			}
		}
		return requestResponse;
	}
}

