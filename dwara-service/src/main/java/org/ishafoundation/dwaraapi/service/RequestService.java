package org.ishafoundation.dwaraapi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.ishafoundation.dwaraapi.api.resp.request.RequestResponse;
import org.ishafoundation.dwaraapi.api.resp.restore.File;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
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
	private DomainUtil domainUtil;

	@Autowired
	private VolumeService volumeService;
	
	public List<RequestResponse> getRequests(RequestType requestType, Action action, List<Status> statusList){
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
				
					
	//				String artifactclassId = systemRequest.getDetails().getArtifactclassId();
	//				Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
	//				Domain domain = artifactclass.getDomain();
					
					Domain domain = request.getDomain();
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
					Domain domain = request.getDomain();
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
			requestResponseList.add(requestResponse);
		}
		return requestResponseList;
	}
	
}

