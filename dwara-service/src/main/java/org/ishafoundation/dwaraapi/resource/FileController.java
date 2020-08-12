package org.ishafoundation.dwaraapi.resource;

import org.ishafoundation.dwaraapi.api.req.restore.RestoreUserRequest;
import org.ishafoundation.dwaraapi.service.FileService;
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

@CrossOrigin
@RestController
public class FileController {
	private static final Logger logger = LoggerFactory.getLogger(FileController.class);
	
	@Autowired
	FileService fileService;
	
	@ApiOperation(value = "Restores the list of files requested from location into the target volume grouped under the output dir")
	@PostMapping("/file/restore")
    public ResponseEntity<String> ingest(@RequestBody RestoreUserRequest userRequest){
		fileService.restore(userRequest);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Done");
	}
	
}
