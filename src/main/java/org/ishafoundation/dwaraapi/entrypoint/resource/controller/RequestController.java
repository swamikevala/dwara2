package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ishafoundation.dwaraapi.db.cacheutil.ActionCacheUtil;
import org.ishafoundation.dwaraapi.db.cacheutil.StatusCacheUtil;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.master.User;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.entrypoint.resource.WrappedRequestResourceList;
import org.ishafoundation.dwaraapi.entrypoint.resource.WrappedSubrequestWithJobDetailsResourceList;
import org.ishafoundation.dwaraapi.enumreferences.SortOrder;
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
	private UserDao userDao;
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private ObjectMappingUtil objectMappingUtil;
	
	@Autowired
	private ActionCacheUtil actionCacheUtil;
	
	@Autowired
	private StatusCacheUtil statusCacheUtil;

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
	public WrappedRequestResourceList getRequestList(@RequestParam(required=false) String action, @RequestParam(required=false) String user, @RequestParam(required=false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime fromDate, @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate, @RequestParam(required=false, defaultValue="1") Integer page, @RequestParam(required=false, defaultValue="10") Integer pageSize, @RequestParam(required=false) String sortColumn, @RequestParam(required=false) SortOrder sortOrder) {
		boolean showSubrequests = false;
		
		Integer actionId = null;
		if(action != null) {
			Action actionObj = actionCacheUtil.getAction(action);
			actionId = actionObj.getId();
		}
		Integer userId = null;
		if(user != null) {
			User userObj = userDao.findByName(user); // TODO : use cache util...
			userId = userObj.getId();
		}
		
		WrappedRequestList wrappedRequestList = requestDao.findAllByActionAndUserIdAndRequestedAtOrderByLatest(actionId, userId, fromDate, toDate, page, pageSize);
	   	List<Request> requestList = wrappedRequestList.getRequestList();
	   	
	   	List<org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails> requestListForResponse = new ArrayList<org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails>();
		for (Request nthRequest : requestList) {
			List<Subrequest> subrequestList = null;
			if(!showSubrequests) {
			}else {
				subrequestList = subrequestDao.findAllByRequestId(nthRequest.getId());
			}
			
			org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails requestForResponse = objectMappingUtil.frameRequestObjectForResponse(nthRequest, subrequestList);
			requestListForResponse.add(requestForResponse);		
		}
		
		WrappedRequestResourceList wrappedRequestResourceList = new WrappedRequestResourceList();
		wrappedRequestResourceList.setRequest(requestListForResponse);
		wrappedRequestResourceList.setPage(page);
		wrappedRequestResourceList.setPageSize(pageSize);
		wrappedRequestResourceList.setTotal(wrappedRequestList.getTotal());
		return wrappedRequestResourceList;
	}
	
	@GetMapping("/request/{requestId}")
	public org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails getRequestDetails(@PathVariable("requestId") int requestId, @RequestParam(defaultValue="true") boolean showSubrequests) {

		List<Subrequest> subrequestList = null;
		if(!showSubrequests) {
		}else {
			subrequestList = subrequestDao.findAllByRequestId(requestId);
		}	

		Request request = requestDao.findById(requestId).get();
		org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails requestForResponse = objectMappingUtil.frameRequestObjectForResponse(request, subrequestList);

		return requestForResponse;
	}

	// TODO : to ensure for restore related search only restore relevant Request/subrequest info is shown

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
	public org.ishafoundation.dwaraapi.entrypoint.resource.WrappedSubrequestWithJobDetailsResourceList getSubrequestList(@RequestParam(required=false) String action, @RequestParam(required=false) String status, @RequestParam(required=false) String user, @RequestParam(required=false) String libraryName, @RequestParam(required=false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime fromDate, @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate, @RequestParam(required=false) boolean latest, @RequestParam(required=false) boolean showJobs, @RequestParam(required=false, defaultValue="1") Integer page, @RequestParam(required=false, defaultValue="10") Integer pageSize, @RequestParam(required=false) String sortColumn, @RequestParam(required=false) SortOrder sortOrder) {
		List<org.ishafoundation.dwaraapi.entrypoint.resource.SubrequestWithJobDetails> subrequestListForResponse = new ArrayList<org.ishafoundation.dwaraapi.entrypoint.resource.SubrequestWithJobDetails>();
		
		Set<Integer> statusIdSet = null;
		if(status != null) { // TODO - use optional
			statusIdSet = new HashSet<Integer>();
			String[] statusArrAsString = status.split(",");
		   	
		   	for (int i = 0; i < statusArrAsString.length; i++) {
		   		statusIdSet.add(statusCacheUtil.getStatus(statusArrAsString[i]).getId());
			}
		}
		Integer actionId = null;
		if(action != null){
		   	Action actionObj = actionCacheUtil.getAction(action);
		   	actionId = actionObj.getId();
		}
		
	   	WrappedSubrequestList wrappedSubrequestList = null;
    	if(latest)
    		wrappedSubrequestList = subrequestDao.findAllLatestByActionAndStatusIds(actionId, statusIdSet, page, pageSize); // TODO implement using Latest
    	else
    		wrappedSubrequestList = subrequestDao.findAllByActionIdAndStatusIds(actionId, statusIdSet, page, pageSize);

	   	List<Subrequest> subrequestList = wrappedSubrequestList.getSubrequestList();
	   	
		for (Subrequest nthSubrequest : subrequestList) {
			org.ishafoundation.dwaraapi.entrypoint.resource.SubrequestWithJobDetails subrequestForResponse = getSubrequestDetails(nthSubrequest, showJobs);
			subrequestListForResponse.add(subrequestForResponse);		
		}
		
		WrappedSubrequestWithJobDetailsResourceList wrappedSubrequestWithJobDetailsList = new WrappedSubrequestWithJobDetailsResourceList();
		wrappedSubrequestWithJobDetailsList.setSubrequest(subrequestListForResponse);
		wrappedSubrequestWithJobDetailsList.setPage(page);
		wrappedSubrequestWithJobDetailsList.setPageSize(pageSize);
		wrappedSubrequestWithJobDetailsList.setTotal(wrappedSubrequestList.getTotal());
		return wrappedSubrequestWithJobDetailsList;
	}
	
	@GetMapping("/subrequest/{subrequestId}")
	public org.ishafoundation.dwaraapi.entrypoint.resource.SubrequestWithJobDetails getSubrequestDetails(@PathVariable("subrequestId") int subrequestId, @RequestParam(required=false) boolean showJobs) {
		Subrequest subrequest = subrequestDao.findById(subrequestId).get();
		return getSubrequestDetails(subrequest, showJobs);
	}
	
	private org.ishafoundation.dwaraapi.entrypoint.resource.SubrequestWithJobDetails getSubrequestDetails(Subrequest subrequest, boolean showJobs){
		
		List<Job> jobList = null;
		if(showJobs)
			jobList = jobDao.findAllBySubrequestIdOrderById(subrequest.getId());
		
		org.ishafoundation.dwaraapi.entrypoint.resource.SubrequestWithJobDetails subrequestForResponse = objectMappingUtil.frameSubrequestObjectWithJobDetailsForResponse(subrequest, jobList);

		return subrequestForResponse;
	}
	

}
