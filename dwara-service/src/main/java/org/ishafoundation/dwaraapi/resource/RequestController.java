package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.request.RequestResponse;
import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.ActionAttributeConverter;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
		
	/**
	 * Assuming all params are mandatory to be passed...
	 * 
	 * @param type
	 * @param action
	 * @param status
	 * @return
	 */
	@GetMapping(value = "/request", produces = "application/json")
	public ResponseEntity<List<RequestResponse>> getRequests(@RequestParam(value="type", required=false) String type, @RequestParam(required=false) String action, @RequestParam(required=false) String status){
		logger.info("/request?type=" + type + "&action=" + action + "&status=" + status);
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
			   		Status statusEnum = Status.valueOf(statusArrAsString[i]);
			   		statusList.add(statusEnum);
				}
			}
			
			requestResponseList = requestService.getRequests(requestType, actionEnumList, statusList);
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
			String errorMsg = "Unable to get Request - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
    	return ResponseEntity.status(HttpStatus.OK).body(requestResponse);
    }
    
    @PostMapping("/request/{requestId}/release")
    public ResponseEntity<RequestResponse> releaseRequest(@PathVariable("requestId") int requestId) {
    	logger.info("/request/" + requestId + "/release");
    	RequestResponse requestResponse = null;
    	try {
    		requestResponse = requestService.releaseRequest(requestId);
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
	
	@PostMapping("/request/release")
    public ResponseEntity<List<RequestResponse>> releaseListRequest(@RequestBody List<Integer> listRequestId) {
    	List<RequestResponse> requestResponse = new ArrayList<RequestResponse>();
    	try {
			for (Integer requestId : listRequestId) {
				requestResponse.add(requestService.releaseRequest(requestId));	
			}
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
}
