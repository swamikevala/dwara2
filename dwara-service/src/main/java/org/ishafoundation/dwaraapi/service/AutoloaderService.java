package org.ishafoundation.dwaraapi.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.resp.mapdrives.MapDrivesResponse;
import org.ishafoundation.dwaraapi.api.resp.mapdrives.SystemRequestForMapDriveResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AutoloaderService extends DwaraService{

	private static final Logger logger = LoggerFactory.getLogger(AutoloaderService.class);
	
	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private JobCreator jobCreator;
	
    public MapDrivesResponse mapDrives(String autoloaderId) throws Exception{	
    	MapDrivesResponse mapDrivesResponse = new MapDrivesResponse();

//		Request userRequest = new Request();
//    	userRequest.setType(RequestType.user);
//		userRequest.setActionId(Action.map_tapedrives);
//		userRequest.setStatus(Status.queued);
//    	User user = getUserObjFromContext();
//    	String requestedBy = user.getName();
//    	userRequest.setRequestedBy(user);
//		userRequest.setRequestedAt(LocalDateTime.now());
//		
//		RequestDetails details = new RequestDetails();
//		ObjectMapper mapper = new ObjectMapper();
//		HashMap<String, Object> data = new HashMap<String, Object>();
//    	data.put("autoloaderId", autoloaderId);
//    	String jsonAsString = mapper.writeValueAsString(data);
//		JsonNode postBodyJson = mapper.readValue(jsonAsString, JsonNode.class);
//		details.setBody(postBodyJson);
//		userRequest.setDetails(details);
//		
//    	userRequest = requestDao.save(userRequest);
//    	int userRequestId = userRequest.getId();
//    	logger.info(DwaraConstants.USER_REQUEST + userRequestId);

		HashMap<String, Object> data = new HashMap<String, Object>();
    	data.put("autoloaderId", autoloaderId);
    	Request userRequest = createUserRequest(Action.map_tapedrives, data);
    	int userRequestId = userRequest.getId();
	
		Request systemRequest = new Request();
		systemRequest.setType(RequestType.system);
		systemRequest.setStatus(Status.queued);
		systemRequest.setRequestRef(userRequest);
		systemRequest.setActionId(userRequest.getActionId());
		systemRequest.setRequestedBy(userRequest.getRequestedBy());
		systemRequest.setRequestedAt(LocalDateTime.now());
		
		RequestDetails systemrequestDetails = new RequestDetails();
		systemrequestDetails.setAutoloaderId(autoloaderId);
		
		systemRequest.setDetails(systemrequestDetails);
		systemRequest = requestDao.save(systemRequest);
		logger.info(DwaraConstants.SYSTEM_REQUEST + systemRequest.getId());
		
		
		List<Job> jobList = jobCreator.createJobs(systemRequest, null);
			
    	mapDrivesResponse.setUserRequestId(userRequestId);
    	mapDrivesResponse.setAction(userRequest.getActionId().name());
    	mapDrivesResponse.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
    	mapDrivesResponse.setRequestedBy(userRequest.getRequestedBy().getName());
    	SystemRequestForMapDriveResponse systemRequestForMapDriveResponse = new SystemRequestForMapDriveResponse();
    	systemRequestForMapDriveResponse.setId(systemRequest.getId());
    	systemRequestForMapDriveResponse.setJobId(jobList.get(0).getId());
    	systemRequestForMapDriveResponse.setStatus(jobList.get(0).getStatus());
    	systemRequestForMapDriveResponse.setAutoloaderId(autoloaderId);
    	mapDrivesResponse.setSystemRequest(systemRequestForMapDriveResponse);
    	
    	return mapDrivesResponse;
    }
}

