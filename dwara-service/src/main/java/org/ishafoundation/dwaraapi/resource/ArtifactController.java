package org.ishafoundation.dwaraapi.resource;

import org.ishafoundation.dwaraapi.api.resp.artifact.ArtifactResponse;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.ArtifactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public class ArtifactController {


	@Autowired
	private ArtifactService artifactservice;


	private static final Logger logger = LoggerFactory.getLogger(ArtifactController.class);


	@ApiOperation(value = "Soft deletes the artifact")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok")
	})
	@PostMapping(value = "/artifact/{artifactId}/delete", produces = "application/json")
	public ResponseEntity<ArtifactResponse> deleteArtifact(@PathVariable("artifactId") int artifactId) {
		logger.info("/artifact/" + artifactId + "/delete");
		ArtifactResponse deleteArtifactResponse = null;
		try {
			deleteArtifactResponse = artifactservice.deleteArtifact(artifactId);
		}catch (Exception e) {
			String errorMsg = "Unable to delete artifact - " + e.getMessage();
			logger.error(errorMsg, e);

			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(deleteArtifactResponse);
	}
}	