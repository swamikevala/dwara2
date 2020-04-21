package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.tape.drive.status.DriveStatusDetails;
import org.ishafoundation.dwaraapi.tape.library.TapeLibraryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class TapeController {
	
	Logger logger = LoggerFactory.getLogger(TapeController.class);

	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@Autowired
	private TapeDriveManager tapeDriveManager;
	
	// TODO Question for swami - Should we do write and verify immediately..
	
	
	//	POST /tape/123/verify
	// TODO Question for swami - Help on this
	@ApiOperation(value = "? verify the content of the tape. For bru it will use bru difference mode")
	@PostMapping("/tape/{tapeBarcode}/verify")	
	public ResponseEntity<String> verify(@PathVariable("tapeBarcode") String tapeBarcode){
		

		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	
	//	POST /tape/123/writeLabel
	@ApiOperation(value = "?")
	@PostMapping("/tape/{tapeBarcode}/writeLabel")
	public ResponseEntity<String> writeLabel(@PathVariable("tapeBarcode") String tapeBarcode){
		
		// TODO This will be moved out of the controller here...
		// TODO do we have to create jobs for these...
		Request request = new Request();
		Subrequest subrequest = new Subrequest();
		Job job = new Job();
		
		//get a drive
		List<DriveStatusDetails> availableDrivesList = tapeLibraryManager.getAvailableDrivesList();
		DriveStatusDetails dsd = availableDrivesList.get(0);
		
		// load the tape
		try {
			tapeLibraryManager.locateAndLoadTapeOnToDrive(tapeBarcode, dsd.getTapeLibraryName(), dsd.getDriveSNo());
			//tapeDriveManager.writeLabel(tapeBarcode, dsd.getDriveSNo());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		



		
		
		return ResponseEntity.status(HttpStatus.OK).body("");
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
