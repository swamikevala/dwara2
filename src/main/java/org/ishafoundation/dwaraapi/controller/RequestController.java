package org.ishafoundation.dwaraapi.controller;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestController {

//	@Autowired
//	private RequestDao requestDao;
//	
//	@Autowired
//	private SubrequestDao subrequestDao;
//
//	@Autowired
//	private LibraryDao libraryDao;
//	
//	@Autowired
//	private JobDao jobDao;	
//	
//	@Autowired
//	private ResponseRequestUtils requestUtils;
//	
//	
//	@GetMapping("/request/{requestId}")
//	public org.ishafoundation.dwaraapi.api.resp.ingest.Request getRequestDetails(@PathVariable("requestId") int requestId) {
//
//		List<org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest> subrequestListForResponse = new ArrayList<org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest>();
//		List<Subrequest> subrequestList = subrequestDao.findAllByRequestId(requestId);
//		for (Subrequest nthSubrequest : subrequestList) {
//			int libraryId = nthSubrequest.getLibraryId();
//			Library library = libraryDao.findById(libraryId).get();
//			
//			org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest subrequestForResponse = requestUtils.frameSubrequestObjectForResponse(nthSubrequest, library);
//			subrequestListForResponse.add(subrequestForResponse);
//		}
//		
//		Request request = requestDao.findById(requestId).get();
//		org.ishafoundation.dwaraapi.api.resp.ingest.Request requestForResponse = requestUtils.frameRequestObjectForResponse(request, subrequestListForResponse);
//		
//		return requestForResponse;
//	}
//	
//	@GetMapping("/subrequest/{subrequestId}")
//	public org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestWithJobDetails getSubrequestDetails(@PathVariable("subrequestId") int subrequestId) {
//		Subrequest subrequest = subrequestDao.findById(subrequestId).get();
//		int libraryId = subrequest.getLibraryId();
//		Library library = libraryDao.findById(libraryId).get();
//		
//		List<Job> jobList = jobDao.findAllBySubrequestIdOrderByJobId(subrequest.getSubrequestId());
//		org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestWithJobDetails subrequestForResponse = requestUtils.frameSubrequestObjectWithJobDetailsForResponse(subrequest, library, jobList);
//
//		return subrequestForResponse;
//	}
//	
////	TODO filter using request type = ingest/restore & status = completed,partially_completed etc.,
//	/*
//	 * 
//	 * @args[0] - request type Id
//	 * @args[1] - comma separated statusIds
//	 */
//	@GetMapping("/subrequest")
//	public List<org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest> getSubrequestList(@RequestParam int type, @RequestParam String statusId) {
//		List<org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest> subrequestListForResponse = new ArrayList<org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest>();
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
}
