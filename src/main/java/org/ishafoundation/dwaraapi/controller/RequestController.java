package org.ishafoundation.dwaraapi.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ishafoundation.dwaraapi.db.cacheutil.RequesttypeCacheUtil;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.LibraryDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.master.reference.Requesttype;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.utils.RequestResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestController {

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private SubrequestDao subrequestDao;

	@Autowired
	private LibraryDao libraryDao;
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private RequestResponseUtils requestUtils;
	
	@Autowired
	private RequesttypeCacheUtil requesttypeCacheUtil;
	
	//	TODO filter using request type = ingest/restore & status = completed,partially_completed etc.,
	/*
	 * 
	 * @args[0] - request type Id
	 * @args[1] - comma separated statusIds
	 */
//	@GetMapping("/request")
//	public List<org.ishafoundation.dwaraapi.api.resp.ingest.Request> getRequestList(@RequestParam int type, @RequestParam String statusId) {
//		List<org.ishafoundation.dwaraapi.api.resp.ingest.Request> subrequestListForResponse = new ArrayList<org.ishafoundation.dwaraapi.api.resp.ingest.Request>();
//		List<Subrequest> subrequest = subrequestDao.findCompleted();
//		for (Subrequest nthSubrequest : subrequest) {
//			int libraryId = nthSubrequest.getLibraryId();
//			Library library = libraryDao.findById(libraryId).get();
//				
//			org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest subrequestForResponse = requestUtils.frameSubrequestObjectForResponse(nthSubrequest, library);
//			subrequestListForResponse.add(subrequestForResponse);
//		}
//		return subrequestListForResponse;
//	}
	
	@GetMapping("/request/{requestId}")
	public org.ishafoundation.dwaraapi.api.resp.ingest.Request getRequestDetails(@PathVariable("requestId") int requestId, @RequestParam(required=false) boolean showSubrequests) {
	//ONce id is passed requesttype is useless so commenting out - public org.ishafoundation.dwaraapi.api.resp.ingest.Request getRequestDetails(@PathVariable("requestId") int requestId, @RequestParam(required=false) String requestType, @RequestParam(required=false) boolean showSubrequests) {
		

		List<Subrequest> subrequestList = null;
		if(showSubrequests) {
			subrequestList = subrequestDao.findAllByRequestId(requestId);
		}	

		Request request = requestDao.findById(requestId).get();
		org.ishafoundation.dwaraapi.api.resp.ingest.Request requestForResponse = requestUtils.frameRequestObjectForResponse(request, subrequestList);

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
	public List<org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestWithJobDetails> getSubrequestList(@RequestParam(required=false) String requestType, @RequestParam(required=false) String statusIds, @RequestParam(required=false) boolean latest, @RequestParam(required=false) boolean showJobs) {
		List<org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestWithJobDetails> subrequestListForResponse = new ArrayList<org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestWithJobDetails>();
		
		String[] statusIdArrAsString = statusIds.split(",");
		
	   	Set<Integer> statusIdSet = new HashSet<Integer>();
	   	for (int i = 0; i < statusIdArrAsString.length; i++) {
	   		statusIdSet.add(Integer.parseInt(statusIdArrAsString[i]));
		}
    	
	   	Requesttype requesttype = requesttypeCacheUtil.getRequesttype(requestType);
    	List<Subrequest> subrequestList = null;
    	if(latest)
    		subrequestList = subrequestDao.findAllLatestByRequesttypeAndStatusIds(requesttype.getId(), statusIdSet);
    	else
    		subrequestList = subrequestDao.findAllByRequesttypeAndStatusIds(requesttype.getId(), statusIdSet);
    	
		//subrequestList = subrequestDao.findCompleted();
		for (Subrequest nthSubrequest : subrequestList) {
			/*
			int libraryId = nthSubrequest.getLibraryId();
			Library library = libraryDao.findById(libraryId).get();
			*/	
			/*
			Library library = nthSubrequest.getLibrary();
			
			org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest subrequestForResponse = requestUtils.frameSubrequestObjectForResponse(nthSubrequest, library);
			*/
			org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestWithJobDetails subrequestForResponse = getSubrequestDetails(nthSubrequest, showJobs);
			subrequestListForResponse.add(subrequestForResponse);		
		}
		return subrequestListForResponse;
	}
	
	@GetMapping("/subrequest/{subrequestId}")
	public org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestWithJobDetails getSubrequestDetails(@PathVariable("subrequestId") int subrequestId, @RequestParam(required=false) boolean showJobs) {
		Subrequest subrequest = subrequestDao.findById(subrequestId).get();
		return getSubrequestDetails(subrequest, showJobs);
	}
	
	private org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestWithJobDetails getSubrequestDetails(Subrequest subrequest, boolean showJobs){
		/*
		int libraryId = subrequest.getLibrary().getId();
		Library library = libraryDao.findById(libraryId).get();
		*/
		
		Library library = subrequest.getLibrary();
		
		List<Job> jobList = null;
		if(showJobs)
			jobList = jobDao.findAllBySubrequestIdOrderById(subrequest.getId());
		
		org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestWithJobDetails subrequestForResponse = requestUtils.frameSubrequestObjectWithJobDetailsForResponse(subrequest, jobList);

		return subrequestForResponse;
	}
	

}
