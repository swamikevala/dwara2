package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req.initialize.InitializeUserRequest;
import org.ishafoundation.dwaraapi.api.resp.initialize.InitializeResponse;
import org.ishafoundation.dwaraapi.api.resp.initialize.SystemRequestForInitializeResponse;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.TapeStoragesubtype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.storage.storagesubtype.AbstractStoragesubtype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class VolumeInitializer {

	private static final Logger logger = LoggerFactory.getLogger(VolumeInitializer.class);

	@Autowired
	private RequestDao requestDao;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private VolumeDao volumeDao;
	
	@Autowired
	private JobCreator jobCreator;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private Map<String, AbstractStoragesubtype> storagesubtypeMap;
	
	/*
	Volume ids should not be in use
	Volume id sequence numbers should be contiguous
	Volume id validations required by the storagesubtype (e.g. L7 suffix for LTO-7 tapes)
	Volume blocksize should be multiple of 64KiB
	
	// TODO How to let UI know the following...
	Volume Group should be defined (db)
	Storagesubtype should be defined (enum)
	*/
	public void validateInitializeUserRequest(List<InitializeUserRequest> initializeRequestList) {
		// Caching the volume Groups so can be accessed in the for loop below
		List<Volume> volumeGroupList = volumeDao.findAllByType(Volumetype.group); 
		Map<String, Volume> volumeGroupId_Volume_Map = new HashMap<String, Volume>();
		for (Volume volume : volumeGroupList) {
			volumeGroupId_Volume_Map.put(volume.getId(), volume);
		}

		// Ordering the initializeRequests by sequence Number
		Map<String, Map<Integer, InitializeUserRequest>> volumeGroup_volumeNumericSequence_InitializeRequest = new HashMap<String, Map<Integer, InitializeUserRequest>>();
		for (InitializeUserRequest nthInitializeRequest : initializeRequestList) {
			String volumeId = nthInitializeRequest.getVolume();
			String volumeGroupId = nthInitializeRequest.getVolumeGroup();
			int sequenceOnLabel = getSequenceUsedOnVolumeLabel(volumeId, null);
			Map<Integer, InitializeUserRequest> volumeNumericSequence_InitializeRequest = volumeGroup_volumeNumericSequence_InitializeRequest.get(volumeGroupId);
			if(volumeNumericSequence_InitializeRequest == null) {
				volumeNumericSequence_InitializeRequest = new HashMap<Integer, InitializeUserRequest>();
				volumeGroup_volumeNumericSequence_InitializeRequest.put(volumeGroupId, volumeNumericSequence_InitializeRequest);
			}
			volumeNumericSequence_InitializeRequest.put(sequenceOnLabel, nthInitializeRequest);
		}
		
		// iterating through the volumegroup related tapes
		Set<String> volumeGroupSet = volumeGroup_volumeNumericSequence_InitializeRequest.keySet();
		for (String nthVolumeGroup : volumeGroupSet) {
			Map<Integer, InitializeUserRequest> volumeNumericSequence_InitializeRequest = volumeGroup_volumeNumericSequence_InitializeRequest.get(nthVolumeGroup);

			Set<Integer> volumeNumericSequenceSet = volumeNumericSequence_InitializeRequest.keySet();
			List<Integer> volumeNumericSequenceList = new ArrayList<Integer>(volumeNumericSequenceSet) ;        //set -> list
			//Sort the list
			Collections.sort(volumeNumericSequenceList);

			int cnt = 0;
			for (Integer volumeNumericSequence : volumeNumericSequenceList) {
				cnt = cnt + 1;
				InitializeUserRequest nthInitializeRequest = volumeNumericSequence_InitializeRequest.get(volumeNumericSequence);
				String volumeId = nthInitializeRequest.getVolume();
				if(nthInitializeRequest.getForce()) {
					if(!configuration.isAllowForceOptionForTesting())
						throw new DwaraException("Force option not supported just yet. Volume " + volumeId, null);
				}
				
				// #1 - Volume ids should not be in use
				try {
					Volume volume = volumeDao.findById(volumeId).get(); // TODO: if force=true, means we are trying to reinitialize an existing tape. How about that??? Means what happens to the existing artifact/file_volume entries???
					throw new DwaraException("Volume " + volumeId + " already in use" , null);
				}
				catch (Exception e) {
					
				}

				// #5 - Volume Group should be defined (db)
				String volumeGroupId = nthInitializeRequest.getVolumeGroup();
				Volume volumeGroup = volumeGroupId_Volume_Map.get(volumeGroupId);
				if(volumeGroup == null)
					throw new DwaraException("Volume Group " + volumeGroupId + " doesnt exist" , null);


				// #2 - Volume id sequence numbers should be contiguous
				int currentNumber = volumeGroup.getSequence().getCurrrentNumber(); // Dont call incrementNumber here as Sequence associated with Volume gets the updated currentnumber saved as well even before init
				int expectedSequenceOnLabel = currentNumber + cnt;

				String prefix = volumeGroup.getSequence().getPrefix();
				int sequenceOnLabel = getSequenceUsedOnVolumeLabel(volumeId, prefix);
				if(sequenceOnLabel != expectedSequenceOnLabel)
					throw new DwaraException("Sequence number for Volume " + volumeId + " with " + sequenceOnLabel + " is not contiguous. Expected numeric sequence - " + expectedSequenceOnLabel, null);

				// #6 - Storagesubtype should be defined (enum)
				String storagesubtypeStr = nthInitializeRequest.getStoragesubtype();
				TapeStoragesubtype storagesubtype = TapeStoragesubtype.getStoragesubtype(storagesubtypeStr);
				if(storagesubtype == null)
					throw new DwaraException("Storagesubtype " + storagesubtypeStr + " not supported" , null);
				
				// #3 - Volume id validations required by the storagesubtype (e.g. L7 suffix for LTO-7 tapes)
				storagesubtypeMap.get(storagesubtypeStr).validateVolumeId(volumeId);
				
				// #4 - Volume blocksize should be multiple of 64KiB
				int divisorInBytes = 65536; // 64 * 1024
				Integer volumeBlocksize = nthInitializeRequest.getVolumeBlocksize();
				if(volumeBlocksize != null && volumeBlocksize%divisorInBytes != 0) {
					throw new DwaraException("Volume " + volumeId + " blocksize is not in multiple of 64KiB" , null);
				}
				
				logger.trace("All validation good for " + volumeId);
			}	
		}
	}

	private int getSequenceUsedOnVolumeLabel(String volumeId, String prefix){
		String regEx = "([0-9]*)";
		if(prefix != null)
			regEx = prefix + regEx;
		Pattern regExPattern = Pattern.compile(regEx);
		Matcher regExMatcher = regExPattern.matcher(volumeId);
		while(regExMatcher.find()) {
			String numericSequence = regExMatcher.group(1);
			if(StringUtils.isNotBlank(numericSequence))
				return Integer.parseInt(numericSequence);
		}
		return 0;
	}

	
	public InitializeResponse initialize(String userName, List<InitializeUserRequest> initializeRequestList) throws Exception{
		if(initializeRequestList.size() == 0)
			return null;
		
		InitializeResponse initializeResponse = new InitializeResponse();
		Request userRequest = new Request();
		userRequest.setType(RequestType.user);
		userRequest.setActionId(Action.initialize);
		userRequest.setStatus(Status.queued);
		User user = userDao.findByName(userName);
		userRequest.setRequestedBy(user);
		userRequest.setRequestedAt(LocalDateTime.now());
		
		RequestDetails details = new RequestDetails();
		ObjectMapper mapper = new ObjectMapper();
    	String jsonAsString = mapper.writeValueAsString(initializeRequestList);
		JsonNode postBodyJson = mapper.readValue(jsonAsString, JsonNode.class);
		details.setBody(postBodyJson);
		userRequest.setDetails(details);

		userRequest = requestDao.save(userRequest);
		int userRequestId = userRequest.getId();
		logger.info(DwaraConstants.USER_REQUEST + userRequestId);

		List<SystemRequestForInitializeResponse> systemRequests = new ArrayList<SystemRequestForInitializeResponse>();
		for (InitializeUserRequest nthInitializeRequest : initializeRequestList) {
			Request systemrequest = new Request();
			systemrequest.setType(RequestType.system);
			systemrequest.setRequestRef(userRequest);
			systemrequest.setActionId(userRequest.getActionId());
			systemrequest.setStatus(Status.queued);
			systemrequest.setRequestedBy(userRequest.getRequestedBy());
			systemrequest.setRequestedAt(LocalDateTime.now());

			RequestDetails systemrequestDetails = getRequestDetailsForInitialize(nthInitializeRequest);
			systemrequest.setDetails(systemrequestDetails);
			systemrequest = requestDao.save(systemrequest);
			logger.info(DwaraConstants.SYSTEM_REQUEST + systemrequest.getId());

			Job job = jobCreator.createJobs(systemrequest, null).get(0); // Initialize generates just one job

			int jobId = job.getId();
			Status status = job.getStatus();

			SystemRequestForInitializeResponse systemRequestForInitializeResponse = new SystemRequestForInitializeResponse();
			systemRequestForInitializeResponse.setId(systemrequest.getId());
			systemRequestForInitializeResponse.setJobId(jobId);
			systemRequestForInitializeResponse.setStatus(status);
			systemRequestForInitializeResponse.setVolume(nthInitializeRequest.getVolume());
			
			systemRequests.add(systemRequestForInitializeResponse);
		}
		
		// Framing the response object here...
		initializeResponse.setUserRequestId(userRequestId);
		initializeResponse.setAction(userRequest.getActionId().name());
		initializeResponse.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
		initializeResponse.setRequestedBy(userRequest.getRequestedBy().getName());
		initializeResponse.setSystemRequests(systemRequests);

		return initializeResponse;
	}
		
    public RequestDetails getRequestDetailsForInitialize(InitializeUserRequest initializeUserRequest) {
        if (initializeUserRequest == null ) {
            return null;
        }

        RequestDetails requestDetails = new RequestDetails();

        if ( initializeUserRequest.getVolume() != null ) {
            requestDetails.setVolumeId( initializeUserRequest.getVolume() );
        }
        if ( initializeUserRequest.getVolumeGroup() != null ) {
            requestDetails.setVolumeGroupId( initializeUserRequest.getVolumeGroup() );
        }
        if ( initializeUserRequest.getVolumeBlocksize() != null ) {
            requestDetails.setVolumeBlocksize( initializeUserRequest.getVolumeBlocksize() );
        }
        if ( initializeUserRequest.getStoragesubtype() != null ) {
            requestDetails.setStoragesubtype( initializeUserRequest.getStoragesubtype() );
        }
        if ( initializeUserRequest.getForce() != null ) {
            requestDetails.setForce( initializeUserRequest.getForce() );
        }

        return requestDetails;
    }
    
	protected String getDateForUI(LocalDateTime _tedAt) { // requestedAt, createdAt, startedAt
		String dateForUI = null;
		if(_tedAt != null) {
			ZonedDateTime zdt = _tedAt.atZone(ZoneId.of("UTC"));
			dateForUI = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").format(zdt);
		}
		return dateForUI;
	}
}
