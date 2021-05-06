package org.ishafoundation.dwaraapi.resource;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.api.req.RewriteRequest;
import org.ishafoundation.dwaraapi.api.req.artifact.ArtifactChangeArtifactclassRequest;
import org.ishafoundation.dwaraapi.api.req.artifact.ArtifactSoftRenameRequest;
import org.ishafoundation.dwaraapi.api.resp.artifact.ArtifactResponse;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.ArtifactService;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.InterArtifactlabel;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.LabelManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public class ArtifactController {

	private static final Logger logger = LoggerFactory.getLogger(ArtifactController.class);
	
	@Autowired
	private ArtifactService artifactservice;
	
	@Autowired
	private LabelManagerImpl labelManager;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Value("${filesystem.temporarylocation}")
	private String filesystemTemporarylocation;

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

	@PostMapping(value = "/artifact/{artifactId}/softRename", produces = "application/json")
	public ResponseEntity<ArtifactResponse> softRename(@RequestBody ArtifactSoftRenameRequest artifactSoftRenameRequest, @PathVariable("artifactId") int artifactId, @RequestParam Boolean force){
		ArtifactResponse artifactSoftRenameResponse = null;
    	String artifactNewName = artifactSoftRenameRequest.getNewName();		 
		// Set the domain for the artifact
    	logger.info("/artifact/" + artifactId + "/softRename");		
		try {
			artifactSoftRenameResponse = artifactservice.softRenameArtifact(artifactId, artifactNewName, force);
		}catch (Exception e) {
			String errorMsg = "Unable to rename artifact - " + e.getMessage();
			logger.error(errorMsg, e);
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(artifactSoftRenameResponse);
	}
	
	@PostMapping(value = "/artifact/{artifactId}/changeArtifactclass", produces = "application/json")
	public ResponseEntity<ArtifactResponse> changeArtifactclass(@RequestBody ArtifactChangeArtifactclassRequest artifactChangeArtifactclassRequest, @PathVariable("artifactId") int artifactId, @RequestParam Boolean force){
		ArtifactResponse artifactResponse = null;
    	logger.info("/artifact/" + artifactId + "/changeArtifactclass");		
		try {
			String newArtifactclass = artifactChangeArtifactclassRequest.getArtifactclass();		
			artifactResponse = artifactservice.changeArtifactclass(artifactId, newArtifactclass, force);
		}catch (Exception e) {
			String errorMsg = "Unable to change Artifactclass for artifact " + artifactId + "- " + e.getMessage();
			logger.error(errorMsg, e);
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(artifactResponse);
	}
	
	@ApiOperation(value = "Generates a specific Artifact' Label and saves it in the configured temp location. Useful for dd-ing the artifact label manually if something goes wrong with label writing after content is written to tape")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok")
	})
	@PostMapping(value="/generateArtifactLabel", produces = "application/json")
    public ResponseEntity<String> generateArtifactLabel(@RequestParam int artifactId, @RequestParam String volumeId, @RequestParam String fileName) throws Exception{
		// TODO - Hardcoded Domain - To support all domains
		ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(Domain.ONE);
		Artifact artifact = artifactRepository.findById(artifactId).get();
		
	    ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(Domain.ONE);
	    ArtifactVolume artifactVolume = domainSpecificArtifactVolumeRepository.findByIdArtifactIdAndIdVolumeId(artifactId, volumeId);

		
	    InterArtifactlabel artifactlabel = labelManager.generateArtifactLabel(artifact, artifactVolume.getVolume(), artifactVolume.getDetails().getStartVolumeBlock(), artifactVolume.getDetails().getEndVolumeBlock());
	    String label = labelManager.labelXmlAsString(artifactlabel);
		
		File file = new File(filesystemTemporarylocation + File.separator + fileName + ".xml");
		FileUtils.writeStringToFile(file, label);
		String response = file.getAbsolutePath() + " created"; 
		logger.trace(response);

		return ResponseEntity.status(HttpStatus.OK).body(response);
		
	}
	
	@ApiOperation(value = "Rewrite the artifact")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok")
	})
	@PostMapping(value = "/artifact/{artifactId}/rewrite", produces = "application/json")
	public ResponseEntity<ArtifactResponse> rewriteArtifact(@RequestBody RewriteRequest rewriteRequest, @PathVariable("artifactId") int artifactId) {
		logger.info("/artifact/" + artifactId + "/rewrite");
		ArtifactResponse rewriteArtifactResponse = null;
		try {
			rewriteArtifactResponse = artifactservice.rewriteArtifact(artifactId, (int) rewriteRequest.getRewriteCopy(), rewriteRequest.getGoodCopy());
		}catch (Exception e) {
			String errorMsg = "Unable to rewrite artifact - " + e.getMessage();
			logger.error(errorMsg, e);

			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(rewriteArtifactResponse);
		
	}
}	