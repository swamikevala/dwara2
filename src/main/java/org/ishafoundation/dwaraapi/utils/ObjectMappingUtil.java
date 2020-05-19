package org.ishafoundation.dwaraapi.utils;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams;
import org.ishafoundation.dwaraapi.db.dao.master.ProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.dao.master.StoragetaskDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.entrypoint.resource.ingest.IngestFile;
import org.ishafoundation.dwaraapi.entrypoint.resource.mapper.EntityResourceMapper;
import org.ishafoundation.dwaraapi.entrypoint.resource.mapper.MiscObjectMapper;
import org.ishafoundation.dwaraapi.enumreferences.Tasktype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// TODO _ Check on the 
// http://eloquentdeveloper.com/2016/09/28/automatically-mapping-java-objects/
// https://www.baeldung.com/mapstruct
// https://www.baeldung.com/java-performance-mapping-frameworks
@Component
public class ObjectMappingUtil {
	
	@Autowired
	private StoragetaskDao storagetaskDao;
	
	@Autowired
	private ProcessingtaskDao processingtaskDao;
	    
    @Autowired
    private MiscObjectMapper miscObjectMapper; 
    
    @Autowired
    private EntityResourceMapper entityResourceMapper;
    
	
	public IngestFile frameIngestFileObject(LibraryParams libraryParams) {
		IngestFile ingestFile = miscObjectMapper.libraryParamsToIngestFile(libraryParams);
		return ingestFile;
	}
	
	public org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails frameRequestObjectForResponse(Request request){
		return entityResourceMapper.getRequestWithSubrequestDetailsResource(request);
	}
	
	public org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails frameRequestObjectForResponse(Request request, List<Subrequest> subrequestList){
		
		org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails requestForResponse = frameRequestObjectForResponse(request);
		
		if(subrequestList != null) {
			List<org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest> subrequestListForResponse = new ArrayList<org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest>();
			for (Subrequest nthSubrequest : subrequestList) {
				org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest subrequestForResponse = frameSubrequestObjectForResponse(nthSubrequest);
				subrequestListForResponse.add(subrequestForResponse);
			}
	
			requestForResponse.setSubrequestList(subrequestListForResponse);
		}
		return requestForResponse;
	}
	
	public org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest frameSubrequestObjectForResponse(Subrequest subrequest){
		
		org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest subRequestForResponse = frameSubrequestObjectWithJobDetailsForResponse(subrequest, null);
		
		return subRequestForResponse;		
	}
	
	public org.ishafoundation.dwaraapi.entrypoint.resource.SubrequestWithJobDetails frameSubrequestObjectWithJobDetailsForResponse(Subrequest subrequest,  List<Job> jobList){

		org.ishafoundation.dwaraapi.entrypoint.resource.SubrequestWithJobDetails subRequestForResponse = entityResourceMapper.getSubrequestWithJobDetailsResource(subrequest);
		
		if(jobList != null) {
			List<org.ishafoundation.dwaraapi.entrypoint.resource.Job> jobListForResponse = new ArrayList<org.ishafoundation.dwaraapi.entrypoint.resource.Job>();
			for (Job job : jobList) {
				org.ishafoundation.dwaraapi.entrypoint.resource.Job jobForResponse = frameJobObjectForResponse(job);
				String taskName = getTaskName(job.getTaskId(), job.getTasktype());
				jobForResponse.setTaskName(taskName);
				jobListForResponse.add(jobForResponse);
			}		
			subRequestForResponse.setJobList(jobListForResponse);
		}
		return subRequestForResponse;		
	}	
	
	private String getTaskName(int taskId, Tasktype tasktype) {
		String taskName = null;
		if(tasktype == Tasktype.storage) {
			taskName = storagetaskDao.findById(taskId).get().getName();
		}
		else if(tasktype == Tasktype.processing)
			taskName = processingtaskDao.findById(taskId).get().getName();
			
		return taskName;
	}
	
	public org.ishafoundation.dwaraapi.entrypoint.resource.Job frameJobObjectForResponse(Job job){
		return entityResourceMapper.getJobResource(job);
	}
}
