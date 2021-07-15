package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.job.JobResponse;
import org.ishafoundation.dwaraapi.api.resp.request.RequestResponse;
import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.ActionAttributeConverter;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.JobDetailsType;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class RequestController {
	
	private static final Logger logger = LoggerFactory.getLogger(RequestController.class);
	
	@Autowired
	RequestService requestService;

	@Autowired
	private UserDao userDao;
		
	@GetMapping("/request/{requestId}")
    public ResponseEntity<RequestResponse> getRequest(@PathVariable("requestId") int requestId) {
    	logger.info("/request/" + requestId);
    	RequestResponse requestResponse = null;
    	try {
    		requestResponse = requestService.getRequest(requestId);
		}catch (Exception e) {
			String errorMsg = "Unable to get Request - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
    	return ResponseEntity.status(HttpStatus.OK).body(requestResponse);
    }

    
	/**
	 * Assuming all params are mandatory to be passed...
	 * 
	 * @param type
	 * @param action
	 * @param status
	 * @return
	 */
	@GetMapping(value = "/request", produces = "application/json")
	// Artifactname option commented out public ResponseEntity<List<RequestResponse>> getRequests(@RequestParam(value="type", required=false) String type, @RequestParam(required=false) String action, @RequestParam(required=false) String status, @RequestParam(required=false) String requestedBy, @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date requestedFrom,  @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date requestedTo, @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date completedFrom,  @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date completedTo, @RequestParam(required=false) String artifactName, @RequestParam(required=false) String artifactclass, @RequestParam(required=false) JobDetailsType jobDetailsType){
	public ResponseEntity<List<RequestResponse>> getRequests(@RequestParam(value="type", required=false) String type, @RequestParam(required=false) String action, @RequestParam(required=false) String status, @RequestParam(required=false) String requestedBy, @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date requestedFrom,  @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date requestedTo, @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date completedFrom,  @RequestParam(required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date completedTo, @RequestParam(required=false) String artifactclass, @RequestParam(required=false) JobDetailsType jobDetailsType){
		String artifactName = null;
		logger.info("/request?type=" + type + "&action=" + action + "&status=" + status + "&requestedBy=" + requestedBy + "&requestedFrom=" + requestedFrom + "&requestedTo=" + requestedTo + "&completedFrom=" + completedFrom + "&completedTo=" + completedTo + "&artifactName=" + artifactName + "&artifactclass=" + artifactclass + "&jobDetailsType=" + jobDetailsType);
		List<RequestResponse> requestResponseList = null;
		try {
			
			RequestType requestType = null;
			if(type != null)
				requestType = RequestType.valueOf(type);

			List<Action> actionEnumList = null;
			if(action != null) { // TODO - use optional
				actionEnumList = new ArrayList<Action>();
				String[] actionArrAsString = action.split(",");
			   	
			   	for (int i = 0; i < actionArrAsString.length; i++) {
			   		if(actionArrAsString[i].equalsIgnoreCase("all")) // Weird UI sends the value "all" also, filter it here regardless of UI is fixed or not
			   			continue;
					ActionAttributeConverter actionAttributeConverter = new ActionAttributeConverter();
					Action actionEnum = actionAttributeConverter.convertToEntityAttribute(actionArrAsString[i]);
			   		actionEnumList.add(actionEnum);
				}
			}
			
			List<Status> statusList = null;
			if(status != null) { // TODO - use optional
				statusList = new ArrayList<Status>();
				String[] statusArrAsString = status.split(",");
			   	
			   	for (int i = 0; i < statusArrAsString.length; i++) {
			   		if(statusArrAsString[i].equalsIgnoreCase("all")) // Weird UI sends the value "all" also, filter it here regardless of UI is fixed or not
			   			continue;
			   		Status statusEnum = Status.valueOf(statusArrAsString[i]);
			   		statusList.add(statusEnum);
				}
			}
			
			List<User> requestedByList = null;
			if(requestedBy != null) { 
				requestedByList = new ArrayList<User>();
				String[] requestedByListAsArr = requestedBy.split(",");
			   	
				HashMap<String, User> userName_User_Map = new HashMap<String, User>(); 
				Iterable<User> usersFromDB = userDao.findAll();
				for (User nthuser : usersFromDB) {
					userName_User_Map.put(nthuser.getName(), nthuser);
				}
			   	for (int i = 0; i < requestedByListAsArr.length; i++) {
			   		if(requestedByListAsArr[i].equalsIgnoreCase("all")) // Weird UI sends the value "all" also, filter it here regardless of UI is fixed or not
			   			continue;
			   		
			   		requestedByList.add(userName_User_Map.get(requestedByListAsArr[i]));
				}
			}
			
			List<String> artifactclassList = null;
			if(artifactclass != null) { 
				artifactclassList = new ArrayList<String>();
				String[] artifactclassListAsArr = artifactclass.split(",");
			   	
			   	for (int i = 0; i < artifactclassListAsArr.length; i++) {
			   		if(artifactclassListAsArr[i].equalsIgnoreCase("all")) // Weird UI sends the value "all" also, filter it here regardless of UI is fixed or not
			   			continue;
			   		artifactclassList.add(artifactclassListAsArr[i]);
				}
			}
			
			requestResponseList = requestService.getRequests(requestType, actionEnumList, statusList, requestedByList, requestedFrom, requestedTo, completedFrom, completedTo, artifactName, artifactclassList, jobDetailsType);
		}catch (Exception e) {
			String errorMsg = "Unable to get Request details - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(requestResponseList);
	}
	
    @PostMapping("/request/{requestId}/cancel")
    public ResponseEntity<RequestResponse> cancelRequest(@PathVariable("requestId") int requestId) {
    	logger.info("/request/" + requestId + "/cancel");
    	RequestResponse requestResponse = null;
    	try {
    		requestResponse = requestService.cancelRequest(requestId);
		}catch (Exception e) {
			String errorMsg = "Unable to cancel Request - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
    	return ResponseEntity.status(HttpStatus.OK).body(requestResponse);
    }
    
    @PostMapping("/request/multi-cancel")
    public ResponseEntity<List<RequestResponse>> cancelMultipleRequests(@RequestParam("requestIds") String systemRequestIds) {
    	List<RequestResponse> requestResponseList = new ArrayList<RequestResponse>();
		List<String> systemRequestIdsList = Arrays.asList(systemRequestIds.split(","));
		for (String systemRequestIdAsString : systemRequestIdsList) {
			int requestId = Integer.parseInt(systemRequestIdAsString);
			RequestResponse requestResponse = null;
	    	try {
	    		requestResponse = requestService.cancelRequest(requestId);
	    		requestResponseList.add(requestResponse);
			}catch (Exception e) {
				String errorMsg = "Unable to cancel Requests - " + e.getMessage();
				logger.error(errorMsg, e);
				
				if(e instanceof DwaraException)
					throw (DwaraException) e;
				else
					throw new DwaraException(errorMsg, null);
			}
		}
			
    	return ResponseEntity.status(HttpStatus.OK).body(requestResponseList);
    }

    
//    @PostMapping("/request/{requestId}/delete")
//    public ResponseEntity<RequestResponse> deleteRequest(@PathVariable("requestId") int requestId) {
//    	logger.info("/request/" + requestId + "/delete");
//    	RequestResponse requestResponse = null;
//    	try {
//    		requestResponse = requestService.deleteRequest(requestId);
//		}catch (Exception e) {
//			String errorMsg = "Unable to delete Request - " + e.getMessage();
//			logger.error(errorMsg, e);
//			
//			if(e instanceof DwaraException)
//				throw (DwaraException) e;
//			else
//				throw new DwaraException(errorMsg, null);
//		}
//    	return ResponseEntity.status(HttpStatus.OK).body(requestResponse);
//    }
    
    @PostMapping("/request/{requestId}/release")
    public ResponseEntity<RequestResponse> releaseRequest(@PathVariable("requestId") int requestId) {
    	logger.info("/request/" + requestId + "/release");
    	RequestResponse requestResponse = null;
    	try {
    		requestResponse = requestService.releaseRequest(requestId);
		}catch (Exception e) {
			String errorMsg = "Unable to release Request - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
    	return ResponseEntity.status(HttpStatus.OK).body(requestResponse);
	}
	
	@PostMapping("/request/release")
    public ResponseEntity<RequestResponse> releaseRequest(@RequestBody List<Integer> listRequestId) {
    	logger.info("/request/release " + listRequestId);
    	RequestResponse requestResponse = null;
    	try {
    		requestResponse = requestService.releaseRequest(listRequestId);	
		}catch (Exception e) {
			String errorMsg = "Unable to release Requests - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
    	return ResponseEntity.status(HttpStatus.OK).body(requestResponse);
    }
}
