package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class TapelibraryController {
	
	Logger logger = LoggerFactory.getLogger(TapelibraryController.class);


	
	/*
	 * We need to have a drive-mapper script that queries and stores the mapping between drive device_ids and the element addresses. This should be rerun any time there is any library reconfiguration done, drives added/removed etc. This would do the following steps:
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
		// TODO: We should also ensure that no storage jobs are in queue or need to block all jobs until this is over...
		
// TODO : Drivermpper.map() will be invoked by the job processor
		
		// TODO Table entries Request-->Subrequest-->Job here
			
			// frame the response
	
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

}
