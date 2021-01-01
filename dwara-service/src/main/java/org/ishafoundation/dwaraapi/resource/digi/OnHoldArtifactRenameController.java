package org.ishafoundation.dwaraapi.resource.digi;

import org.ishafoundation.dwaraapi.api.req.artifact.ArtifactRenameFile;
import org.ishafoundation.dwaraapi.api.resp.artifact.ArtifactResponse;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.digi.OnHoldArtifactRenameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin
@RestController
public class OnHoldArtifactRenameController {


	@Autowired
	private OnHoldArtifactRenameService onHoldArtifactRenameService;


	private static final Logger logger = LoggerFactory.getLogger(OnHoldArtifactRenameController.class);


	// ---------------- HARD Renaming Stuff ----------------------------------------------
	// Changing the filename, folder name 
	@PostMapping(value = "/artifact/{artifactId}/rename", produces = "application/json")
	public ResponseEntity<ArtifactResponse> renameHeldFile(@RequestBody ArtifactRenameFile  artifactRenameFile ,@PathVariable("artifactId") int artifactId ){
		// Things to do - Exits; catches; Response entries ; validation 
		ArtifactResponse renameArtifactResponse = null;
    	String artifactNewName = artifactRenameFile.getNewName();		 
		// Set the domain for the artifact
    	logger.info("/artifact/" + artifactId + "/rename");		
		try {
			renameArtifactResponse = onHoldArtifactRenameService.hardSoftrenameArtifact(artifactId, artifactNewName);
		}catch (Exception e) {
			String errorMsg = "Unable to rename artifact - " + e.getMessage();
			logger.error(errorMsg, e);
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(renameArtifactResponse);
 
	}
}	