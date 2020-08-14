package org.ishafoundation.dwaraapi.resource;

import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.artifactclass.ArtifactclassResponse;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.ArtifactclassService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public class ArtifactclassController {
	private static final Logger logger = LoggerFactory.getLogger(ArtifactclassController.class);
	
	@Autowired
	ArtifactclassService artifactclassService;
	
	@ApiOperation(value = "Gets all Aritfactclasses")
	@ApiResponses(value = { 
		    @ApiResponse(code = 200, message = "Ok"),
		    @ApiResponse(code = 400, message = "Error")
	})
	@GetMapping(value="/artifactclass", produces = "application/json")
    public ResponseEntity<List<ArtifactclassResponse>> getAllArtifactclasses(){
		
		List<ArtifactclassResponse> artifactclassResponseList = null;
		try {
			artifactclassResponseList = artifactclassService.getAllArtifactClasses();
		}catch (Exception e) {
			String errorMsg = "Unable to format - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		
		return ResponseEntity.status(HttpStatus.OK).body(artifactclassResponseList);
	}	
}
