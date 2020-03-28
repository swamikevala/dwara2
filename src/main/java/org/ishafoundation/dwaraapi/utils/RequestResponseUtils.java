package org.ishafoundation.dwaraapi.utils;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.springframework.stereotype.Component;

@Component
public class RequestResponseUtils {

	public org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedRequest frameWrappedRequestObjectForResponse(Request request, List<org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest> subrequestRespListForResponse){
		org.ishafoundation.dwaraapi.api.resp.ingest.RequestWithWrappedSubrequest requestForResponse = new org.ishafoundation.dwaraapi.api.resp.ingest.RequestWithWrappedSubrequest();

		requestForResponse.setRequestId(request.getId());
		requestForResponse.setRequesttypeId(request.getRequesttype().getId());
		requestForResponse.setRequestedAt(request.getRequestedAt());
		requestForResponse.setRequestedBy(request.getRequestedBy());
		requestForResponse.setLibraryclassId(request.getLibraryclass().getId());		
		requestForResponse.setSubrequestResp(subrequestRespListForResponse);
		
		org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedRequest response = new org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedRequest();
		response.setResponseCode(200);
		response.setResponseMessage("Som message");
		response.setResponseType("some responseType");
		response.setRequest(requestForResponse);
		return response;
	}
	
	public org.ishafoundation.dwaraapi.api.resp.ingest.Request frameRequestObjectForResponse(Request request, List<org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest> subrequestListForResponse){
		org.ishafoundation.dwaraapi.api.resp.ingest.Request requestForResponse = new org.ishafoundation.dwaraapi.api.resp.ingest.Request();
		
		requestForResponse.setRequestId(request.getId());
		requestForResponse.setRequesttypeId(request.getRequesttype().getId());
		requestForResponse.setRequestedAt(request.getRequestedAt());
		requestForResponse.setRequestedBy(request.getRequestedBy());
		requestForResponse.setLibraryclassId(request.getLibraryclass().getId());	
		requestForResponse.setSubrequestList(subrequestListForResponse);
		
		return requestForResponse;
	}
	
	public org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest frameWrappedSubrequestObjectForResponse(Subrequest subrequest, Library library){
		// TODO _ Check on the 
		// http://eloquentdeveloper.com/2016/09/28/automatically-mapping-java-objects/
		// https://www.baeldung.com/mapstruct
		// https://www.baeldung.com/java-performance-mapping-frameworks

		org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest subRequestForResponse = frameSubrequestObjectForResponse(subrequest, library);
				
		org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest subRequestWrapperRespForResponse = new org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest();
		
		subRequestWrapperRespForResponse.setResponseCode(200);
		subRequestWrapperRespForResponse.setResponseMessage("Resp message");
		subRequestWrapperRespForResponse.setResponseType("Resp type");
		subRequestWrapperRespForResponse.setSubrequest(subRequestForResponse);
		return subRequestWrapperRespForResponse;
	}
	
	public org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest frameSubrequestObjectForResponse(Subrequest subrequest, Library library){
		
		org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest subRequestForResponse = new org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest();
		subRequestForResponse.setLibrary(library);
		subRequestForResponse.setSubrequestId(subrequest.getId());
		subRequestForResponse.setNewFilename(subrequest.getNewFilename());
		subRequestForResponse.setOldFilename(subrequest.getOldFilename());
		subRequestForResponse.setOptimizeTapeAccess(subrequest.isOptimizeTapeAccess());
		subRequestForResponse.setPrevSequenceCode(subrequest.getPrevSequenceCode());
		subRequestForResponse.setPriority(subrequest.getPriority());
		subRequestForResponse.setRequestId(subrequest.getRequest().getId());
		subRequestForResponse.setRerun(subrequest.isRerun());
		subRequestForResponse.setRerunNo(subrequest.getRerunNo());
		subRequestForResponse.setSkipTasks(subrequest.getSkipTasks());
		subRequestForResponse.setSourcePath(subrequest.getSourcePath());
		subRequestForResponse.setStatus(subrequest.getStatus().toString());
		
		return subRequestForResponse;		
	}
//	
//	public org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestWithJobDetails frameSubrequestObjectWithJobDetailsForResponse(Subrequest subrequest, Library library, List<Job> jobList){
//		
//		org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestWithJobDetails subRequestForResponse = new org.ishafoundation.dwaraapi.api.resp.ingest.SubrequestWithJobDetails();
//		
//		subRequestForResponse.setSubrequestId(subrequest.getSubrequestId());
//		subRequestForResponse.setNewFilename(subrequest.getNewFilename());
//		subRequestForResponse.setOldFilename(subrequest.getOldFilename());
//		subRequestForResponse.setOptimizeTapeAccess(subrequest.isOptimizeTapeAccess());
//		subRequestForResponse.setPrevSequenceCode(subrequest.getPrevSequenceCode());
//		subRequestForResponse.setPriority(subrequest.getPriority());
//		subRequestForResponse.setRequestId(subrequest.getRequestId());
//		subRequestForResponse.setRerun(subrequest.isRerun());
//		subRequestForResponse.setRerunNo(subrequest.getRerunNo());
//		subRequestForResponse.setSkipTasks(subrequest.getSkipTasks());
//		subRequestForResponse.setSourcePath(subrequest.getSourcePath());
//		subRequestForResponse.setStatusId(subrequest.getStatusId());
//		
//		subRequestForResponse.setLibrary(library);
//		
//		subRequestForResponse.setJobList(jobList);
//		return subRequestForResponse;		
//	}	
}
