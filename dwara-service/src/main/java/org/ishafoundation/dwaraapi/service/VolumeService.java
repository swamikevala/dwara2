package org.ishafoundation.dwaraapi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req.initialize.InitializeUserRequest;
import org.ishafoundation.dwaraapi.api.resp.initialize.InitializeResponse;
import org.ishafoundation.dwaraapi.api.resp.initialize.SystemRequestForInitializeResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.Details;
import org.ishafoundation.dwaraapi.api.resp.volume.VolumeResponse;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.resource.mapper.RequestToEntityObjectMapper;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.VolumeFinalizer;
import org.ishafoundation.dwaraapi.utils.VolumeUtil;
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
	private DomainUtil domainUtil;
	
	@Autowired
	private VolumeUtil volumeUtil;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
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
				volumeResponseList.add(getVolume_Internal(volume));
			}
		return volumeResponseList;
	}

	public VolumeResponse getVolume(String volumeId) {
		Optional<Volume> volumeEntity = volumeDao.findById(volumeId);
		if(volumeEntity.isPresent()) {
			Volume volume = volumeEntity.get();
			return getVolume_Internal(volume);
		}else
			return null;
	}
	
	private VolumeResponse getVolume_Internal(Volume volume) {
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

		if(volume.getType() == Volumetype.group) {
			// get all not finalized physical volume in it
			
//			
//			
//		   	Domain[] domains = Domain.values();
//		   	Domain domain = null;
//			for (Domain nthDomain : domains) {
//			    ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(nthDomain);
//			    int artifactVolumeCount = domainSpecificArtifactVolumeRepository.countByIdVolumeId(volume.getId());
//				if(artifactVolumeCount > 0) {
//					domain = nthDomain;
//					break;
//				}
//			}
//			
//			domain == null
//					domainUtil.getDefaultDomain
//					
//					
//			iterate all physical volume from the group and sum up 
//			
//			volumeUtil.getVolumeUnusedCapacity(domain, volume)
//			volumeUtil.getVolumeUsedCapacity(domain, volume)

			
		}
		
		/* TODO
		volResp.setTotalCapacity(totalCapacity);
		volResp.setUsedCapacity(usedCapacity);
		volResp.setUnusedCapacity(unusedCapacity);
		volResp.setMaxPhysicalUnusedCapacity(maxPhysicalUnusedCapacity);
		volResp.setSizeUnit(sizeUnit);
		*/
		if(volume.getLocation() != null)
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
		return volResp;

	}
	public InitializeResponse initialize(List<InitializeUserRequest> initializeRequestList) throws Exception{	
		InitializeResponse initializeResponse = new InitializeResponse();
		Action requestedBusinessAction = Action.initialize;
		org.ishafoundation.dwaraapi.db.model.master.reference.Action action = configurationTablesUtil.getAction(requestedBusinessAction);
		if(action == null)
			throw new Exception("Action for " + requestedBusinessAction.name() + " not configured in DB properly. Please set it first");

		
		Request request = new Request();
		request.setType(RequestType.user);
		request.setActionId(requestedBusinessAction);
		request.setStatus(Status.queued);
    	User user = getUserObjFromContext();
    	String requestedBy = user.getName();

    	LocalDateTime requestedAt = LocalDateTime.now();
		request.setRequestedAt(requestedAt);
		request.setRequestedBy(user);
		RequestDetails details = new RequestDetails();
		JsonNode postBodyJson = getRequestDetails(initializeRequestList); 
		details.setBody(postBodyJson);
		request.setDetails(details);

		request = requestDao.save(request);
		int requestId = request.getId();
		logger.info(DwaraConstants.USER_REQUEST + requestId);
		
		List<SystemRequestForInitializeResponse> systemRequests = new ArrayList<SystemRequestForInitializeResponse>();
		
		for (InitializeUserRequest nthInitializeRequest : initializeRequestList) {
			Request systemrequest = new Request();
			systemrequest.setType(RequestType.system);
			systemrequest.setRequestRef(request);
			systemrequest.setActionId(request.getActionId());
			systemrequest.setStatus(Status.queued);
			systemrequest.setRequestedBy(request.getRequestedBy());
			systemrequest.setRequestedAt(LocalDateTime.now());

			RequestDetails systemrequestDetails = requestToEntityObjectMapper.getRequestDetailsForInitialize(nthInitializeRequest);

			systemrequest.setDetails(systemrequestDetails);
			systemrequest = requestDao.save(systemrequest);
			logger.info(DwaraConstants.SYSTEM_REQUEST + systemrequest.getId());

			Job job = jobCreator.createJobs(systemrequest, null).get(0); // Initialize generates just one job
			int jobId = job.getId();
			Status status = job.getStatus();
			
//			The updated sequence no for a volume group in initialize is saved along with User Request...
//			Volume volumeGroup = volumeDao.findById(nthInitializeRequest.getVolumeGroup());
//			Sequence sequence = volumeGroup.getSequence();
//			sequence.incrementCurrentNumber();
//			sequenceDao.save(sequence);
//			logger.info("Sequence for - " + volumeGroup.getId() + " updated to " + sequence.getCurrrentNumber());
			
			SystemRequestForInitializeResponse systemRequestForInitializeResponse = new SystemRequestForInitializeResponse();
			systemRequestForInitializeResponse.setId(systemrequest.getId());
			systemRequestForInitializeResponse.setJobId(jobId);
			systemRequestForInitializeResponse.setStatus(status);
			systemRequestForInitializeResponse.setVolume(nthInitializeRequest.getVolume());
			
			systemRequests.add(systemRequestForInitializeResponse);
		}
		
		// Framing the response object here...
		initializeResponse.setUserRequestId(requestId);
		initializeResponse.setAction(request.getActionId().name());
		initializeResponse.setRequestedAt(getDateForUI(requestedAt));
		initializeResponse.setRequestedBy(requestedBy);
		initializeResponse.setSystemRequests(systemRequests);
		return initializeResponse;	
	}
	
	public String finalize(String volumeId) throws Exception{
		return volumeFinalizer.finalize(volumeId, getUserObjFromContext());
	}
}

