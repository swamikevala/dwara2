package org.ishafoundation.dwaraapi.resource;

import org.ishafoundation.dwaraapi.api.req.format.FormatRequest;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.service.VolumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public class VolumeController {
	private static final Logger logger = LoggerFactory.getLogger(VolumeController.class);
	
	@Autowired
	VolumeService volumeService;
	
	@ApiOperation(value = "Ingest comment goes here")
	@ApiResponses(value = { 
		    @ApiResponse(code = 202, message = "Request submitted and queued up"),
		    @ApiResponse(code = 400, message = "Error")
	})
	@PostMapping("/volume/format")
    public ResponseEntity<String> ingest(@RequestBody FormatRequest formatRequest){
		volumeService.format(formatRequest);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Done");
	}
	
	@PostMapping("/volume/finalize")
	public ResponseEntity<String> finalize(@RequestParam String volumeUid,@RequestParam Domain domain){
		volumeService.finalize(volumeUid, domain);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Done");
	}
}
