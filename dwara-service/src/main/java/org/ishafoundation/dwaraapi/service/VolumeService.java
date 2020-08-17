package org.ishafoundation.dwaraapi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.format.FormatUserRequest;
import org.ishafoundation.dwaraapi.api.resp.format.FormatResponse;
import org.ishafoundation.dwaraapi.api.resp.format.SystemRequestsForFormatResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.Details;
import org.ishafoundation.dwaraapi.api.resp.volume.VolumeResponse;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.resource.mapper.RequestToEntityObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class VolumeService extends DwaraService {

	private static final Logger logger = LoggerFactory.getLogger(VolumeService.class);
	
	@Autowired
	private VolumeDao volumeDao;

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private JobCreator jobCreator;

	@Autowired
	private RequestToEntityObjectMapper requestToEntityObjectMapper; 

	public List<VolumeResponse> getVolumeByVolumetype(String volumetype){
		List<VolumeResponse> volumeResponseList = null;
			// validate
			Volumetype neededVolumetype = Volumetype.valueOf(volumetype);
			
			volumeResponseList = new ArrayList<VolumeResponse>();
			List<Volume> volumeGroupList = volumeDao.findAllByVolumetype(neededVolumetype);
			for (Volume volume : volumeGroupList) {
				VolumeResponse volResp = new VolumeResponse();
				volResp.setId(volume.getId());
				volResp.setVolumetype(volume.getVolumetype().name());
				volResp.setStoragetype(volume.getStoragetype().name());
				volResp.setStoragelevel(volume.getStoragelevel().name());
				//volResp.setVolumeRef(volumeRef);
				//volResp.setChecksumtype(checksumtype);
				//volResp.setFormattedAt(formattedAt);
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
				volResp.setLocation(volume.getLocation().getName());
				
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

	public FormatResponse format(List<FormatUserRequest> formatRequestList) throws Exception{	
		FormatResponse formatResponse = new FormatResponse();
		Request request = new Request();
		request.setActionId(Action.format);
    	
    	User user = getUserObjFromContext();
    	String requestedBy = user.getName();

    	LocalDateTime requestedAt = LocalDateTime.now();
		request.setRequestedAt(requestedAt);
		
		RequestDetails details = new RequestDetails();
		JsonNode postBodyJson = getRequestDetails(formatRequestList); 
		details.setBody(postBodyJson);
		request.setDetails(details);

		request = requestDao.save(request);
		int requestId = request.getId();
		logger.info("Request - " + requestId);
		
		List<SystemRequestsForFormatResponse> systemRequests = new ArrayList<SystemRequestsForFormatResponse>();
		
		for (FormatUserRequest nthFormatRequest : formatRequestList) {
			Request systemrequest = new Request();
			systemrequest.setRequestRef(request);
			systemrequest.setActionId(request.getActionId());
			systemrequest.setUser(request.getUser());
			systemrequest.setRequestedAt(LocalDateTime.now());
			systemrequest.setDomain(request.getDomain());


			RequestDetails systemrequestDetails = requestToEntityObjectMapper.getRequestDetailsForFormat(nthFormatRequest);

			systemrequest.setDetails(systemrequestDetails);
			systemrequest = requestDao.save(systemrequest);
			logger.info("System request - " + systemrequest.getId());

			Job job = jobCreator.createJobs(systemrequest, null).get(0);
			int jobId = job.getId();
			Status status = job.getStatus();
			
			SystemRequestsForFormatResponse systemRequestsForFormatResponse = new SystemRequestsForFormatResponse();
			systemRequestsForFormatResponse.setId(systemrequest.getId());
			systemRequestsForFormatResponse.setJobId(jobId);
			systemRequestsForFormatResponse.setStatus(status);
			systemRequestsForFormatResponse.setVolume(nthFormatRequest.getVolume());
			
			systemRequests.add(systemRequestsForFormatResponse);
		}
		
		// Framing the response object here...
		formatResponse.setUserRequestId(requestId);
		formatResponse.setAction(request.getActionId().name());
		formatResponse.setRequestedAt(getDateForUI(requestedAt));
		formatResponse.setRequestedBy(requestedBy);
		formatResponse.setSystemRequests(systemRequests);
		return formatResponse;	
	}
	
	// TODO : Why domain needed? Filevolume and Artifactvolume needed for generating the index are domain-ed
	public ResponseEntity<String> finalize(String volumeId, Domain domain){

		try {

			Request request = new Request();
			request.setActionId(Action.finalize);
			request.setUser(getUserObjFromContext());
			request.setRequestedAt(LocalDateTime.now());
			request.setDomain(domain);
			RequestDetails details = new RequestDetails();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode postBodyJson = mapper.readValue("{\"volume_id\":\""+volumeId+"\"}", JsonNode.class);
			details.setBody(postBodyJson);
			request.setDetails(details);

			request = requestDao.save(request);
			int requestId = request.getId();
			logger.info("Request - " + requestId);


			Request systemrequest = new Request();
			systemrequest.setRequestRef(request);
			systemrequest.setActionId(request.getActionId());
			systemrequest.setUser(request.getUser());
			systemrequest.setRequestedAt(LocalDateTime.now());
			systemrequest.setDomain(request.getDomain());

			RequestDetails systemrequestDetails = new RequestDetails();
			systemrequestDetails.setVolume_id(volumeId);
			systemrequest.setDetails(systemrequestDetails);
			systemrequest = requestDao.save(systemrequest);
			logger.info("System request - " + systemrequest.getId());


			jobCreator.createJobs(systemrequest, null);
		}catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Done");
	}
}
