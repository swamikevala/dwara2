package org.ishafoundation.dwaraapi.utils;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.entrypoint.resource.ingest.IngestFile;
import org.ishafoundation.dwaraapi.entrypoint.resource.mapper.MiscObjectMapper;
import org.ishafoundation.dwaraapi.entrypoint.resource.mapper.Subrequest_EntityToWithJobDetailsResource_Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// TODO _ Check on the 
// http://eloquentdeveloper.com/2016/09/28/automatically-mapping-java-objects/
// https://www.baeldung.com/mapstruct
// https://www.baeldung.com/java-performance-mapping-frameworks
@Component
public class ObjectMappingUtil {
	
    @Autowired
    Subrequest_EntityToWithJobDetailsResource_Mapper subrequest_EntityToWithJobDetailsResource_Mapper;
    
    @Autowired
    MiscObjectMapper miscObjectMapper; 
    
	
	public IngestFile frameIngestFileObject(LibraryParams libraryParams) {
		IngestFile ingestFile = miscObjectMapper.libraryParamsToIngestFile(libraryParams);
		return ingestFile;
	}
	
	public org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request frameRequestObjectForResponse(Request request){
		org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request requestForResponse = new org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Request();
		
		requestForResponse.setRequestId(request.getId());
		requestForResponse.setAction(request.getAction().toString());
		requestForResponse.setRequestedAt(request.getRequestedAt());
		requestForResponse.setRequestedBy(request.getUser().getName());
		Libraryclass libraryclass = request.getLibraryclass();
		requestForResponse.setLibraryclassName(libraryclass != null ? libraryclass.getName() : null);	

		
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
	
	public org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Subrequest frameSubrequestObjectForResponse(Subrequest subrequest){
		
		org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Subrequest subRequestForResponse = frameSubrequestObjectWithJobDetailsForResponse(subrequest, null);
		
		return subRequestForResponse;		
	}
	
	public org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SubrequestWithJobDetails frameSubrequestObjectWithJobDetailsForResponse(Subrequest subrequest,  List<Job> jobList){

		org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SubrequestWithJobDetails subRequestForResponse = subrequest_EntityToWithJobDetailsResource_Mapper.entityToResource(subrequest);
		
		if(jobList != null) {
			List<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Job> jobListForResponse = new ArrayList<org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Job>();
			for (Job job : jobList) {
				org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Job jobForResponse = frameJobObjectForResponse(job);
				jobListForResponse.add(jobForResponse);
			}		
			subRequestForResponse.setJobList(jobListForResponse);
		}
		return subRequestForResponse;		
	}	
	
	public org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Job frameJobObjectForResponse(Job job){
		org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Job jobForResponse = new org.ishafoundation.dwaraapi.entrypoint.resource.ingest.Job();
		jobForResponse.setId(job.getId());
		jobForResponse.setInputLibraryId(job.getInputLibrary() != null ? job.getInputLibrary().getId() : null);
		jobForResponse.setOutputLibraryId(job.getOutputLibrary() != null ? job.getOutputLibrary().getId() : null);
		jobForResponse.setStartedAt(job.getStartedAt());
		jobForResponse.setStatus(job.getStatus().toString());
		jobForResponse.setCreatedAt(job.getCreatedAt());
		jobForResponse.setCompletedAt(job.getCompletedAt());
		jobForResponse.setSubrequestId(job.getSubrequest().getId());
		jobForResponse.setTaskName(job.getTask().getName());
		return jobForResponse;
	}
}
