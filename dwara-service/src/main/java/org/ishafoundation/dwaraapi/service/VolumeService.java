package org.ishafoundation.dwaraapi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.initialize.InitializeUserRequest;
import org.ishafoundation.dwaraapi.api.resp.initialize.InitializeResponse;
import org.ishafoundation.dwaraapi.api.resp.initialize.SystemRequestsForInitializeResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.Details;
import org.ishafoundation.dwaraapi.api.resp.volume.VolumeResponse;
import org.ishafoundation.dwaraapi.db.dao.master.SequenceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.resource.mapper.RequestToEntityObjectMapper;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.VolumeFinalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class VolumeService extends DwaraService {

	private static final Logger logger = LoggerFactory.getLogger(VolumeService.class);
	
	@Autowired
	private VolumeDao volumeDao;

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private SequenceDao sequenceDao;
	
	@Autowired
	private JobCreator jobCreator;

	@Autowired
	private VolumeFinalizer volumeFinalizer;
	
	@Autowired
	private RequestToEntityObjectMapper requestToEntityObjectMapper; 

	public List<VolumeResponse> getVolumeByVolumetype(String volumetype){
		List<VolumeResponse> volumeResponseList = null;
			// validate
			Volumetype neededVolumetype = Volumetype.valueOf(volumetype);
			
			volumeResponseList = new ArrayList<VolumeResponse>();
			List<Volume> volumeGroupList = volumeDao.findAllByType(neededVolumetype);
			for (Volume volume : volumeGroupList) {
				VolumeResponse volResp = new VolumeResponse();
				volResp.setId(volume.getId());
				volResp.setVolumetype(volume.getType().name());
				volResp.setStoragetype(volume.getStoragetype().name());
				volResp.setStoragelevel(volume.getStoragelevel().name());
				//volResp.setVolumeRef(volumeRef);
				//volResp.setChecksumtype(checksumtype);
				//volResp.setInitializedAt(initializetedAt);
				volResp.setFinalized(volume.isFinalized());
				volResp.setImported(volume.isImported());
				if(volume.getArchiveformat() != null)
					volResp.setArchiveformat(volume.getArchiveformat().getId());

				/* TODO
				volResp.setTotalCapacity(totalCapacity);
				volResp.setUsedCapacity(usedCapacity);
				volResp.setUnusedCapacity(unusedCapacity);
				volResp.setMaxPhysicalUnusedCapacity(maxPhysicalUnusedCapacity);
				volResp.setSizeUnit(sizeUnit);
				*/
				volResp.setLocation(volume.getLocation().getId());
				
				VolumeDetails volumeDetails = volume.getDetails();
				if(volumeDetails != null) {
					Details details = new Details();
					
					//details.setBarcoded(volumeDetails.isBarcoded());
					if(volumeDetails.getBlocksize() != null)
						details.setBlocksize(volumeDetails.getBlocksize()/1024);
					details.setBlocksizeUnit("KiB");
					
					details.setStoragesubtype(volume.getStoragesubtype());
					//details.setMountPoint(mountPoint);
					//details.setProvider(provider);
					//details.setRemoveAfterJob(removeAfterJob);
					volResp.setDetails(details);
				}
				volumeResponseList.add(volResp);
			}
		return volumeResponseList;
	}

	public InitializeResponse initialize(List<InitializeUserRequest> initializeRequestList) throws Exception{	
		InitializeResponse initializeResponse = new InitializeResponse();
		Request request = new Request();
		request.setType(RequestType.user);
		request.setActionId(Action.initialize);
		request.setStatus(Status.queued);
    	User user = getUserObjFromContext();
    	String requestedBy = user.getName();

    	LocalDateTime requestedAt = LocalDateTime.now();
		request.setRequestedAt(requestedAt);
		
		RequestDetails details = new RequestDetails();
		JsonNode postBodyJson = getRequestDetails(initializeRequestList); 
		details.setBody(postBodyJson);
		request.setDetails(details);

		request = requestDao.save(request);
		int requestId = request.getId();
		logger.info("Request - " + requestId);
		
		List<SystemRequestsForInitializeResponse> systemRequests = new ArrayList<SystemRequestsForInitializeResponse>();
		
		for (InitializeUserRequest nthInitializeRequest : initializeRequestList) {
			Request systemrequest = new Request();
			systemrequest.setRequestRef(request);
			systemrequest.setActionId(request.getActionId());
			systemrequest.setStatus(Status.queued);
			systemrequest.setRequestedBy(request.getRequestedBy());
			systemrequest.setRequestedAt(LocalDateTime.now());
			systemrequest.setDomain(request.getDomain());


			RequestDetails systemrequestDetails = requestToEntityObjectMapper.getRequestDetailsForInitialize(nthInitializeRequest);

			systemrequest.setDetails(systemrequestDetails);
			systemrequest = requestDao.save(systemrequest);
			logger.info("System request - " + systemrequest.getId());

			Job job = jobCreator.createJobs(systemrequest, null).get(0); // Initialize generates just one job
			int jobId = job.getId();
			Status status = job.getStatus();
			
//			The updated sequence no for a volume group in initialize is saved along with Job...
//			Volume volumeGroup = volumeDao.findById(nthInitializeRequest.getVolumeGroup());
//			Sequence sequence = volumeGroup.getSequence();
//			sequence.incrementCurrentNumber();
//			sequenceDao.save(sequence);
//			logger.info("Sequence for - " + volumeGroup.getId() + " updated to " + sequence.getCurrrentNumber());
			
			SystemRequestsForInitializeResponse systemRequestsForInitializeResponse = new SystemRequestsForInitializeResponse();
			systemRequestsForInitializeResponse.setId(systemrequest.getId());
			systemRequestsForInitializeResponse.setJobId(jobId);
			systemRequestsForInitializeResponse.setStatus(status);
			systemRequestsForInitializeResponse.setVolume(nthInitializeRequest.getVolume());
			
			systemRequests.add(systemRequestsForInitializeResponse);
		}
		
		// Framing the response object here...
		initializeResponse.setUserRequestId(requestId);
		initializeResponse.setAction(request.getActionId().name());
		initializeResponse.setRequestedAt(getDateForUI(requestedAt));
		initializeResponse.setRequestedBy(requestedBy);
		initializeResponse.setSystemRequests(systemRequests);
		return initializeResponse;	
	}
	
	// TODO : Why domain needed? ANS: Filevolume and Artifactvolume needed for generating the index are "domain-ed"
	public String finalize(String volumeId, Domain domain) throws Exception{
		return volumeFinalizer.finalize(volumeId, getUserObjFromContext(), domain);
	}
}

