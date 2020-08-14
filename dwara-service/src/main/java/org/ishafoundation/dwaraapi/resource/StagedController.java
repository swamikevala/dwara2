package org.ishafoundation.dwaraapi.resource;

import java.util.List;

import org.ishafoundation.dwaraapi.api.req.ingest.UserRequest;
import org.ishafoundation.dwaraapi.api.resp.staged.FileDetails;
import org.ishafoundation.dwaraapi.service.StagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public class StagedController {

	private static final Logger logger = LoggerFactory.getLogger(StagedController.class);
	
	@Autowired
	private StagedService stagedService;
	

	@ApiOperation(value = "Scans the selected libraryclass passed and lists all candidate folders from across all users to ingest", response = List.class)
	@ApiResponses(value = { 
		    @ApiResponse(code = 200, message = "Ok"),
		    @ApiResponse(code = 404, message = "Not Found")
	})
	@GetMapping(value="/staged", produces = "application/json")
    public ResponseEntity<List<FileDetails>> getAllIngestableFiles(@RequestParam("artifactclass") String artifactclassId){
		List<FileDetails> ingestFileList = stagedService.getAllIngestableFiles(artifactclassId);
		
		if (ingestFileList.size() > 0) {
			return ResponseEntity.ok(ingestFileList);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}
	
	@ApiOperation(value = "Ingest comment goes here")
	@ApiResponses(value = { 
		    @ApiResponse(code = 202, message = "Request submitted and queued up"),
		    @ApiResponse(code = 400, message = "Error")
	})
	@PostMapping(value="/artifact/staging/ingest", produces = "application/json")
    public ResponseEntity<String> ingest(@RequestBody UserRequest userRequest){
		stagedService.ingest(userRequest);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Done");
	}
	
}	