package org.ishafoundation.dwaraapi.resource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.ishafoundation.dwaraapi.api.req.artifact.ArtifactRenameFile;
import org.ishafoundation.dwaraapi.api.resp.artifact.ArtifactResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.Artifact1Dao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.File1Dao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepositoryUtil;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.ArtifactService;
import org.ishafoundation.dwaraapi.staged.StagedFileOperations;
import org.ishafoundation.dwaraapi.staged.scan.Error;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact1;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File1;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

// Importing the Dao 

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

	// ---------------- HARD Renaming Stuff ----------------------------------------------
	// Changing the filename, folder name 
	@PostMapping(value = "/artifact/{artifactId}/rename", produces = "application/json")
	public ResponseEntity<ArtifactResponse> renameHeldFile(@RequestBody ArtifactRenameFile  artifactRenameFile ,@PathVariable("artifactId") int artifactId ){
		// Things to do - Exits; catches; Response entries ; validation 
		ArtifactResponse renameArtifactResponse = null;
    	String artifactNewName = artifactRenameFile.getNewName();		 
		// Set the domain for the artifact
    	logger.info("/artifact/" + artifactId + "/Rename");		
		try {
			renameArtifactResponse = artifactservice.hardSoftrenameArtifact(artifactId, artifactNewName);
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