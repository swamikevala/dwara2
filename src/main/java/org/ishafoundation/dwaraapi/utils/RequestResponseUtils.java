package org.ishafoundation.dwaraapi.utils;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.springframework.stereotype.Component;

// TODO _ Check on the 
// http://eloquentdeveloper.com/2016/09/28/automatically-mapping-java-objects/
// https://www.baeldung.com/mapstruct
// https://www.baeldung.com/java-performance-mapping-frameworks
@Component
public class RequestResponseUtils {

//	public org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedRequest frameWrappedRequestObjectForResponse(Request request, List<org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest> subrequestRespListForResponse){
//		org.ishafoundation.dwaraapi.api.resp.ingest.RequestWithWrappedSubrequest requestForResponse = new org.ishafoundation.dwaraapi.api.resp.ingest.RequestWithWrappedSubrequest();
//
//		requestForResponse.setRequestId(request.getId());
//		requestForResponse.setActionId(request.getAction().getId());
//		requestForResponse.setRequestedAt(request.getRequestedAt());
//		requestForResponse.setRequestedBy(request.getRequestedBy());
//		requestForResponse.setLibraryclassId(request.getLibraryclass().getId());		
//		requestForResponse.setSubrequestResp(subrequestRespListForResponse);
//		
//		org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedRequest response = new org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedRequest();
//		response.setResponseCode(200);
//		response.setResponseMessage("Som message");
//		response.setResponseType("some responseType");
//		response.setRequest(requestForResponse);
//		return response;
//	}
	
	public org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request frameRequestObjectForResponse(Request request){
		org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request requestForResponse = new org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request();
		
		requestForResponse.setRequestId(request.getId());
		requestForResponse.setAction(request.getAction().toString());
		requestForResponse.setRequestedAt(request.getRequestedAt());
		requestForResponse.setRequestedBy(request.getUser().getName());
		requestForResponse.setLibraryclassName(request.getLibraryclass().getName());	

		
		return requestForResponse;
	}
	
	public org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request frameRequestObjectForResponse(Request request, List<Subrequest> subrequestList){
		
		org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request requestForResponse = frameRequestObjectForResponse(request);
		
		if(subrequestList != null) {
			List<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Subrequest> subrequestListForResponse = new ArrayList<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Subrequest>();
			for (Subrequest nthSubrequest : subrequestList) {
				org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Subrequest subrequestForResponse = frameSubrequestObjectForResponse(nthSubrequest);
				subrequestListForResponse.add(subrequestForResponse);
			}
	
			requestForResponse.setSubrequestList(subrequestListForResponse);
		}
		return requestForResponse;
	}
	
//	public org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest frameWrappedSubrequestObjectForResponse(Subrequest subrequest, Library library){
//
//
//		org.ishafoundation.dwaraapi.api.resp.ingest.Subrequest subRequestForResponse = frameSubrequestObjectForResponse(subrequest, library);
//				
//		org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest subRequestWrapperRespForResponse = new org.ishafoundation.dwaraapi.api.resp.ingest.ResponseHeaderWrappedSubrequest();
//		
//		subRequestWrapperRespForResponse.setResponseCode(200);
//		subRequestWrapperRespForResponse.setResponseMessage("Resp message");
//		subRequestWrapperRespForResponse.setResponseType("Resp type");
//		subRequestWrapperRespForResponse.setSubrequest(subRequestForResponse);
//		return subRequestWrapperRespForResponse;
//	}
	
	public org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Subrequest frameSubrequestObjectForResponse(Subrequest subrequest){
		
		org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Subrequest subRequestForResponse = frameSubrequestObjectWithJobDetailsForResponse(subrequest, null);
		
		return subRequestForResponse;		
	}
	
	public org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SubrequestWithJobDetails frameSubrequestObjectWithJobDetailsForResponse(Subrequest subrequest,  List<Job> jobList){

		org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SubrequestWithJobDetails subRequestForResponse = new org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SubrequestWithJobDetails();


		
//		Library library = subrequest.getLibrary();
//		subRequestForResponse.setLibrary(library);
		
		subRequestForResponse.setId(subrequest.getId());
		subRequestForResponse.setNewFilename(subrequest.getNewFilename());
		subRequestForResponse.setOldFilename(subrequest.getOldFilename());
		//subRequestForResponse.setOptimizeTapeAccess(subrequest.isOptimizeTapeAccess());
		subRequestForResponse.setPrevSequenceCode(subrequest.getPrevSequenceCode());
		subRequestForResponse.setPriority(subrequest.getPriority());
		subRequestForResponse.setRequestId(subrequest.getRequest().getId());
		subRequestForResponse.setRerun(subrequest.isRerun());
		subRequestForResponse.setRerunNo(subrequest.getRerunNo());
		subRequestForResponse.setSkipTasks(subrequest.getSkipTasks());
		subRequestForResponse.setSourcePath(subrequest.getSourcePath());
		subRequestForResponse.setStatus(subrequest.getStatus().toString());

		if(jobList != null) {
			List<org.ishafoundation.dwaraapi.api.resp.ingest.Job> jobListForResponse = new ArrayList<org.ishafoundation.dwaraapi.api.resp.ingest.Job>();
			for (Job job : jobList) {
				org.ishafoundation.dwaraapi.api.resp.ingest.Job jobForResponse = frameJobObjectForResponse(job);
				jobListForResponse.add(jobForResponse);
			}		
			subRequestForResponse.setJobList(jobListForResponse);
		}
		return subRequestForResponse;		
	}	
	
	public org.ishafoundation.dwaraapi.api.resp.ingest.Job frameJobObjectForResponse(Job job){
		org.ishafoundation.dwaraapi.api.resp.ingest.Job jobForResponse = new org.ishafoundation.dwaraapi.api.resp.ingest.Job();
		jobForResponse.setId(job.getId());
		jobForResponse.setInputLibraryId(job.getInputLibrary() != null ? job.getInputLibrary().getId() : null);
		jobForResponse.setOutputLibraryId(job.getOutputLibrary() != null ? job.getOutputLibrary().getId() : null);
		jobForResponse.setStartedAt(job.getStartedAt());
		jobForResponse.setStatus(job.getStatus().toString());
		jobForResponse.setCreatedAt(job.getCreatedAt());
		jobForResponse.setCompletedAt(job.getCompletedAt());
		jobForResponse.setSubrequestId(job.getSubrequest().getId());
		jobForResponse.setTaskId(job.getTask().getId());
		return jobForResponse;
	}
}
