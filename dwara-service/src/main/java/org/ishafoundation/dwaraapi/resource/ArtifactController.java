package org.ishafoundation.dwaraapi.resource;

import org.ishafoundation.dwaraapi.api.req.ingest.UserRequest;
import org.ishafoundation.dwaraapi.service.ArtifactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public class ArtifactController {
	private static final Logger logger = LoggerFactory.getLogger(ArtifactController.class);
	
	@Autowired
	ArtifactService artifactService;
	
	@ApiOperation(value = "Ingest comment goes here")
	@ApiResponses(value = { 
		    @ApiResponse(code = 202, message = "Request submitted and queued up"),
		    @ApiResponse(code = 400, message = "Error")
	})
	@PostMapping("/library/staging/ingest")
    public ResponseEntity<String> ingest(@RequestBody UserRequest userRequest){
		artifactService.ingest(userRequest);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Done");
	}
	
}
