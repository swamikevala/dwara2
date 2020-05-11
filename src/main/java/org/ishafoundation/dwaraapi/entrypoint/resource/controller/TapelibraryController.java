package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;

import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeDriveMapperThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class TapelibraryController {
	
	Logger logger = LoggerFactory.getLogger(TapelibraryController.class);

	@Autowired
	private UserDao userDao;

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private SubrequestDao subrequestDao;

	@Autowired
	private ApplicationContext applicationContext;

	
	/*
	 * REQUIREMENT :
	 * 
	 * We need to have a drive-mapper script that queries and stores the mapping between drive device_ids and the element addresses. 
	 * This should be rerun any time there is any library reconfiguration done, drives added/removed etc. 
	 * This would do the following steps:
		Empty all drives
		Load test tape into drive with library address 0
		See which device id has a tape loaded
		Map the address with the device id
		Repeat for all library addresses
	 * 
	 */
	@ApiOperation(value = "?")
	@PostMapping("/tapelibrary/mapDrives")
	public ResponseEntity<String> mapDrives(){
		// QUESTIONS :
		// synchronous or asynchronous - Nature of the request is such, we might need to wait for busy drives to be available. So asynchronous should be the way with an initial acknowledgement...
		// Job or no job - Task is neither processing nor storage hence no job.
		// Should user need to the new mapping on library vs drives details? - (s)he should check in the DB as against another endpoint for admin showing the mapping. 
		
		/*
		Req subreq and no job but not synchronous/asynchronous
			separate Thread - wait until there arent any inprogress tape jobs and then continue... 
			
		Block all storage jobs when there is a queued map drive request... If a subrequest action type is mapdrive and status is queued or inprogress
		*/
    	Request request = new Request();
    	request.setAction(org.ishafoundation.dwaraapi.constants.Action.tapedrivemapping);
    	request.setRequestedAt(LocalDateTime.now());
    	
    	String requestedBy = SecurityContextHolder.getContext().getAuthentication().getName();
    	request.setUser(userDao.findByName(requestedBy));
    	logger.debug("DB Request Creation");
    	request = requestDao.save(request);
    	int requestId = request.getId();
    	logger.debug("DB Request Creation - Success " + requestId);
		
    	Subrequest subrequest = new Subrequest();
    	subrequest.setRequest(request);
    	subrequest.setStatus(Status.queued);
    	logger.debug("DB Subrequest Creation");
    	subrequest = subrequestDao.save(subrequest);
    	logger.debug("DB Subrequest Creation - Success " + subrequest.getId());
    	
    	TapeDriveMapperThread tdmt = applicationContext.getBean(TapeDriveMapperThread.class);
    	tdmt.setSubrequest(subrequest);
    	Executors.newSingleThreadExecutor().execute(tdmt);
		
		// TODO Table entries Request-->Subrequest-->Job here
		// TODO: We should also ensure that no storage jobs are in queue or need to block all jobs until this is over...			
		// frame the response
		
		return ResponseEntity.status(HttpStatus.OK).body("Map drive request queued up successfully");
	}

}
