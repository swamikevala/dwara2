package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.cacheutil.RequesttypeCacheUtil;
import org.ishafoundation.dwaraapi.db.cacheutil.StatusCacheUtil;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.LibraryDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.master.User;
import org.ishafoundation.dwaraapi.db.model.master.reference.Requesttype;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.entrypoint.resource.ingest.WrappedRequestResourceList;
import org.ishafoundation.dwaraapi.entrypoint.resource.ingest.WrappedSubrequestWithJobDetailsList;
import org.ishafoundation.dwaraapi.model.WrappedRequestList;
import org.ishafoundation.dwaraapi.model.WrappedSubrequestList;
import org.ishafoundation.dwaraapi.utils.ObjectMappingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class RequestController {

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private SubrequestDao subrequestDao;

	@Autowired
	private LibraryDao libraryDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private ObjectMappingUtil objectMappingUtil;
	
	@Autowired
	private RequesttypeCacheUtil requesttypeCacheUtil;
	
	@Autowired
	private StatusCacheUtil statusCacheUtil;
	
	@Autowired
	private Configuration configuration;
	
	private int pageSize = 10; // defaulting it to 10
	
	@PostConstruct
	private void loadPageSize() {
		pageSize = configuration.getPageSize();
	}

	/*
	 * filters the request on the following
	 * 
	 * @args[0] - request type (ingest/restore - both primary and secondary)
	 * @args[1] - comma separated status (completed,partially_completed etc.,)
	 * @args[2,3] - from and to Date range in which the request was created
	 * @args[4] - pageNumber - the nth page that is needed in the resultset - defaults to 1 
	 * 
	 * resultset is by default ordered by most recent first
	 */
	@GetMapping("/request")
	public WrappedRequestResourceList getRequestList(@RequestParam(required=false) String requestType, @RequestParam(required=false) String user, @RequestParam(required=false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime fromDate, @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate, @RequestParam(required=false, defaultValue="1") Integer pageNumber) {
	// do we also need showSubrequests in here... public WrappedRequestResourceList getRequestList(@RequestParam(required=false) String requestType, @RequestParam(required=false) String user, @RequestParam(required=false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime fromDate, @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate, @RequestParam(required=false) boolean showSubrequests, @RequestParam Integer pageNumber) {
		boolean showSubrequests = false;
		
		Integer requestypeId = null;
		if(requestType != null) {
			Requesttype requesttypeObj = requesttypeCacheUtil.getRequesttype(requestType);
			requestypeId = requesttypeObj.getId();
		}
		Integer userId = null;
		if(user != null) {
			User userObj = userDao.findByName(user); // TODO : use cache util...
			userId = userObj.getId();
		}
		
		WrappedRequestList wrappedRequestList = requestDao.findAllByRequesttypeAndUserIdAndRequestedAtOrderByLatest(requestypeId, userId, fromDate, toDate, pageNumber, pageSize);
	   	List<Request> requestList = wrappedRequestList.getRequestList();
	   	
	   	List<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request> requestListForResponse = new ArrayList<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request>();
		for (Request nthRequest : requestList) {
			List<Subrequest> subrequestList = null;
			if(!showSubrequests) {
			}else {
				subrequestList = subrequestDao.findAllByRequestId(nthRequest.getId());
			}
			
			org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request requestForResponse = objectMappingUtil.frameRequestObjectForResponse(nthRequest, subrequestList);
			requestListForResponse.add(requestForResponse);		
		}
		
		WrappedRequestResourceList wrappedRequestResourceList = new WrappedRequestResourceList();
		wrappedRequestResourceList.setRequest(requestListForResponse);
		wrappedRequestResourceList.setPageNumber(pageNumber);
		wrappedRequestResourceList.setTotalNoOfRecords(wrappedRequestList.getTotalNoOfRecords());
		return wrappedRequestResourceList;
	}
	
	@GetMapping("/request/{requestId}")
	public org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request getRequestDetails(@PathVariable("requestId") int requestId, @RequestParam(defaultValue="true") boolean showSubrequests) {

		List<Subrequest> subrequestList = null;
		if(!showSubrequests) {
		}else {
			subrequestList = subrequestDao.findAllByRequestId(requestId);
		}	

		Request request = requestDao.findById(requestId).get();
		org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request requestForResponse = objectMappingUtil.frameRequestObjectForResponse(request, subrequestList);

		return requestForResponse;
	}


	/*
	 * filters using
	 * 
	 * @args[0] - request type = ingest/restore
	 * @args[1] - comma separated statusIds = the ids of completed,partially_completed etc.,
	 * @args[2] - latest - returns the most recent run's subrequest
	 * 
	 * and responds with the joblist based on the below flag
	 * @args[3] - showJobs
	 */
	@GetMapping("/subrequest")
	public org.ishafoundation.dwaraapi.entrypoint.resource.ingest.WrappedSubrequestWithJobDetailsList getSubrequestList(@RequestParam(required=false) String requestType, @RequestParam(required=false) String status, @RequestParam(required=false) boolean latest, @RequestParam(required=false) boolean showJobs, @RequestParam(required=false, defaultValue="1") Integer pageNumber) {
		List<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SubrequestWithJobDetails> subrequestListForResponse = new ArrayList<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SubrequestWithJobDetails>();
		
		Set<Integer> statusIdSet = null;
		if(status != null) { // TODO - use optional
			statusIdSet = new HashSet<Integer>();
			String[] statusArrAsString = status.split(",");
		   	
		   	for (int i = 0; i < statusArrAsString.length; i++) {
		   		statusIdSet.add(statusCacheUtil.getStatus(statusArrAsString[i]).getId());
			}
		}
		Integer requesttypeId = null;
		if(requestType != null){
		   	Requesttype requesttype = requesttypeCacheUtil.getRequesttype(requestType);
		   	requesttypeId = requesttype.getId();
		}
		
	   	WrappedSubrequestList wrappedSubrequestList = null;
    	if(latest)
    		wrappedSubrequestList = subrequestDao.findAllLatestByRequesttypeAndStatusIds(requesttypeId, statusIdSet, pageNumber, pageSize); // TODO implement using Latest
    	else
    		wrappedSubrequestList = subrequestDao.findAllByRequesttypeIdAndStatusIds(requesttypeId, statusIdSet, pageNumber, pageSize);

	   	List<Subrequest> subrequestList = wrappedSubrequestList.getSubrequestList();
	   	
		for (Subrequest nthSubrequest : subrequestList) {
			org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SubrequestWithJobDetails subrequestForResponse = getSubrequestDetails(nthSubrequest, showJobs);
			subrequestListForResponse.add(subrequestForResponse);		
		}
		
		WrappedSubrequestWithJobDetailsList wrappedSubrequestWithJobDetailsList = new WrappedSubrequestWithJobDetailsList();
		wrappedSubrequestWithJobDetailsList.setSubrequest(subrequestListForResponse);
		wrappedSubrequestWithJobDetailsList.setPageNumber(pageNumber);
		wrappedSubrequestWithJobDetailsList.setTotalNoOfRecords(wrappedSubrequestList.getTotalNoOfRecords());
		return wrappedSubrequestWithJobDetailsList;
	}
	
	@GetMapping("/subrequest/{subrequestId}")
	public org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SubrequestWithJobDetails getSubrequestDetails(@PathVariable("subrequestId") int subrequestId, @RequestParam(required=false) boolean showJobs) {
		Subrequest subrequest = subrequestDao.findById(subrequestId).get();
		return getSubrequestDetails(subrequest, showJobs);
	}
	
	private org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SubrequestWithJobDetails getSubrequestDetails(Subrequest subrequest, boolean showJobs){
		
		List<Job> jobList = null;
		if(showJobs)
			jobList = jobDao.findAllBySubrequestIdOrderById(subrequest.getId());
		
		org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SubrequestWithJobDetails subrequestForResponse = objectMappingUtil.frameSubrequestObjectWithJobDetailsForResponse(subrequest, jobList);

		return subrequestForResponse;
	}
	

}
