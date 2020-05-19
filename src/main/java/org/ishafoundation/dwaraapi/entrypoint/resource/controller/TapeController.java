package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import java.time.LocalDateTime;

import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.job.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class TapeController {
	
	Logger logger = LoggerFactory.getLogger(TapeController.class);
	
	@Autowired
	private UserDao userDao;

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private SubrequestDao subrequestDao;
	
	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private JobManager jobManager;
	
	//	POST /tape/123/verify
	// TODO Question for swami - Help on this
	@ApiOperation(value = "? verify the content of the tape. For bru it will use bru difference mode")
	@PostMapping("/tape/{tapeBarcode}/verify")	
	public ResponseEntity<String> verify(@PathVariable("tapeBarcode") String tapeBarcode){
		

		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	
	//	POST /tape/V5A001/writeLabel
	@ApiOperation(value = "?")
	@PostMapping("/tape/format")
	public ResponseEntity<String> format(@RequestBody org.ishafoundation.dwaraapi.api.req.Format requestBody){
		
		Request request = new Request();
    	request.setAction(org.ishafoundation.dwaraapi.enumreferences.Action.format_tape);
    	request.setRequestedAt(LocalDateTime.now());
    	
    	String requestedBy = SecurityContextHolder.getContext().getAuthentication().getName();
    	request.setUser(userDao.findByName(requestedBy));
    	logger.debug("DB Request Creation");
    	request = requestDao.save(request);
    	int requestId = request.getId();
    	logger.debug("DB Request Creation - Success " + requestId);
		
    	// TODO requestBody needs to go in...
    	Subrequest subrequest = new Subrequest();
    	subrequest.setRequest(request);
    	subrequest.setStatus(Status.queued);
    	logger.debug("DB Subrequest Creation");
    	subrequest = subrequestDao.save(subrequest);
    	logger.debug("DB Subrequest Creation - Success " + subrequest.getId());
    	
		//Job job = jobManager.createLabelingJob(request, subrequest);
    	Job job = jobManager.createJobForLabeling(request, subrequest);
    	logger.debug("DB Job row Creation");   
    	jobDao.save(job);
    	logger.debug("DB Job row Creation - Success");
	    
		return ResponseEntity.status(HttpStatus.OK).body(requestBody.getBarcode() + " Write Label request submitted - " + requestId);
	}

	
//	POST /tape/123/readLabel
	@ApiOperation(value = "?")
	@PostMapping("/tape/{tapeBarcode}/readLabel")
	public ResponseEntity<String> readLabel(@PathVariable("tapeBarcode") String tapeBarcode){
		
//		rewind the tape
//		
//		issue the following multiple commands
//		
//		 dd if=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=80
// response will be like below
//Shambho EOF
//0+1 records in
//0+1 records out
//12 bytes (12 B) copied, 0.00298022 s, 4.0 kB/s

		
		return ResponseEntity.status(HttpStatus.OK).body("");
	}
	//	TODO this have to indicate some action - POST /tape/123?tapeset=V5A
	// Identify Unregistered tapes in the system
	// I was thinking that we could automatically format barcoded blank tapes which are kept in the library, and add them to the correct pool. 
	// Based on the barcode prefix, it will know what pool it belongs to, and therefore what archive format it will hold. 
	// Then there will be very minimal admin work - just adding new barcoded tapes to the library
	@ApiOperation(value = "?")
	@PostMapping("/tape/{tapeBarcode}")
	public ResponseEntity<String> writeLabel(@PathVariable("tapeBarcode") String tapeBarcode, @RequestParam String tapesetPrefix){
		
		
		return ResponseEntity.status(HttpStatus.OK).body("");
	}
}
