package org.ishafoundation.dwaraapi.resource;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.api.req._import.BulkImportRequest;
import org.ishafoundation.dwaraapi.api.req._import.ImportRequest;
import org.ishafoundation.dwaraapi.api.req._import.SetSequenceImportRequest;
import org.ishafoundation.dwaraapi.api.resp._import.ImportResponse;
import org.ishafoundation.dwaraapi.artifact.ArtifactAttributes;
import org.ishafoundation.dwaraapi.artifact.ArtifactMeta;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
public class ImportController {
	private static final Logger logger = LoggerFactory.getLogger(ImportController.class);
	
	@Autowired
	private ImportService importService;

	@ApiOperation(value = "Imports a non-dwara tape's meta xml into dwara")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok")
	})
	@PostMapping(value = "/bulkImport", produces = "application/json")
	public ResponseEntity<List<ImportResponse>> bulkImport(@RequestBody BulkImportRequest importRequest) throws Exception {
		logger.info("/bulkImport " + importRequest.getStagingDir());
		List<ImportResponse> importResponseList = null;
		try {
			importResponseList = importService.bulkImport(importRequest);
		}catch (Exception e) {
			String errorMsg = "Unable to bulkImport - " + e.getMessage();
			logger.error(errorMsg, e);
	
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		// Bit tricky if there are multiple files and one has error then its not a bad request...
//		for (ImportResponse nthImportResponse : importResponseList) { // If any of the response has errors we throw 400
//			if(nthImportResponse.getErrors() != null && nthImportResponse.getErrors().size() > 0)
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(importResponseList);
//		}
		
		return ResponseEntity.status(HttpStatus.OK).body(importResponseList);
	}
	
	@ApiOperation(value = "Imports a non-dwara tape's meta xml into dwara")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok")
	})
	// use this endpoint for testing only - wont organise files like moving it to completed directory like bulkImport does @PostMapping(value = "/import", produces = "application/json")
	public ResponseEntity<ImportResponse> importCatalog(@RequestBody ImportRequest importRequest) throws Exception {
		logger.info("/import " + importRequest.getXmlPathname());
		ImportResponse importResponse = null;
		try {
			importResponse = importService.importCatalog(importRequest);
		}catch (Exception e) {
			String errorMsg = "Unable to import - " + e.getMessage();
			logger.error(errorMsg, e);
	
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		
		if(importResponse.getErrors() != null && importResponse.getErrors().size() > 0)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(importResponse);
		
		return ResponseEntity.status(HttpStatus.OK).body(importResponse);
	}
	
	@PostMapping("/import/{importId}/marked_completed")
    public ResponseEntity<ImportResponse> markedCompletedImport(@PathVariable("importId") int importId, @RequestBody (required=false) String reason) {
    	logger.info("/import/" + importId + "/marked_completed");
    	ImportResponse importResponse = null;
    	try {
    		importResponse = importService.markedCompletedImport(importId, reason);
		}catch (Exception e) {
			String errorMsg = "Unable to marked_completed Import - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
    	return ResponseEntity.status(HttpStatus.OK).body(importResponse);
    }
	
	@ApiOperation(value = "Just a temp endpoint - ToBeDeleted - Sets fed in sequence incrementally across artifacts given a xml")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok")
	})
	@PostMapping(value = "/setSequence", produces = "application/json")
	public ResponseEntity<String> setSequence(@RequestBody SetSequenceImportRequest importRequest) throws Exception {
		logger.info("/import " + importRequest.getXmlPathname() + ":" + importRequest.getStartingNumber());
		String importResponse = null;
		try {
			importResponse = importService.setSequence(importRequest);
		}catch (Exception e) {
			String errorMsg = "Unable to set sequence - " + e.getMessage();
			logger.error(errorMsg, e);
	
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		
		
		return ResponseEntity.status(HttpStatus.OK).body(importResponse);
	}
	
	@GetMapping("getArtifactAttributes")
	public ResponseEntity<ArtifactAttributes> getArtifactAttributes(@RequestParam String artifactNameProposed, @RequestParam String artifactclassId){
    	logger.info("/getArtifactAttributes");
    	ArtifactAttributes artifactAttributes = null;
    	try {
    		artifactAttributes = importService.getArtifactAttributes(artifactNameProposed, artifactclassId);
		}catch (Exception e) {
			String errorMsg = "Unable to getArtifactAttributes - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
    	return ResponseEntity.status(HttpStatus.OK).body(artifactAttributes);
		
	}
	
	@GetMapping("getArtifactMeta")
	public ResponseEntity<ArtifactMeta> getArtifactMeta(@RequestParam String artifactNameProposed, @RequestParam String artifactclassId){
    	logger.info("/getArtifactMeta");
    	ArtifactMeta artifactMeta = null;
    	try {
    		artifactMeta = importService.getArtifactMeta(artifactNameProposed, artifactclassId);
		}catch (Exception e) {
			String errorMsg = "Unable to getArtifactMeta - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
    	return ResponseEntity.status(HttpStatus.OK).body(artifactMeta);
		
	}
	
	@GetMapping("artifactListManipulator/queryByNameStartsWith")
	public ResponseEntity<String> queryByNameStartsWith(@RequestParam String artifactNameToTapeMappingFilepathname, @RequestParam String startsWith){
		StringBuffer sb = new StringBuffer();
		try {
			java.io.File artifactNameToTapeMappingFile = new java.io.File(artifactNameToTapeMappingFilepathname);
			List<String> lineList = FileUtils.readLines(artifactNameToTapeMappingFile);
			for (String nthLine : lineList) {
				logger.info(nthLine);
				if(nthLine.startsWith(startsWith))
					sb.append(nthLine+"\n");
			}
		}catch (Exception e) {
			String errorMsg = "Unable to queryByNameStartsWith - " + e.getMessage();
			logger.error(errorMsg, e);
	
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		logger.info("queryByNameStartsWith - " + sb.toString());
		return ResponseEntity.status(HttpStatus.OK).body(sb.toString());
	}

	@GetMapping("artifactListManipulator/queryByVolumeId")
	public ResponseEntity<String> queryByVolumeId(@RequestParam String artifactNameToTapeMappingFilepathname, @RequestParam String volumeId){
		StringBuffer sb = new StringBuffer();
		try {
			java.io.File artifactNameToTapeMappingFile = new java.io.File(artifactNameToTapeMappingFilepathname);
			List<String> lineList = FileUtils.readLines(artifactNameToTapeMappingFile);
			for (String nthLine : lineList) {
				String[] parts = nthLine.split("\\|");
				if(parts[1].equals(volumeId))
					sb.append(parts[0]+"\n");
			}
		}catch (Exception e) {
			String errorMsg = "Unable to queryByVolumeId - " + e.getMessage();
			logger.error(errorMsg, e);
	
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		logger.info("queryByVolumeId - " + sb.toString());
		return ResponseEntity.status(HttpStatus.OK).body(sb.toString());
	}
	
	@PostMapping("artifactListManipulator/assignArtifactclass")
	public ResponseEntity<String> assignArtifactclass(@RequestParam String artifactNameToTapeMappingFilepathname, @RequestParam String volumeId, @RequestParam String artifactclass){
		StringBuffer sb = new StringBuffer();
		try {
			java.io.File artifactNameToTapeMappingFile = new java.io.File(artifactNameToTapeMappingFilepathname);
			List<String> lineList = FileUtils.readLines(artifactNameToTapeMappingFile);
			for (String nthLine : lineList) {
				String[] parts = nthLine.split("\\|");
				if(parts[1].equals(volumeId))
					sb.append(parts[0]+"|"+parts[1]+"|"+artifactclass);
				else
					sb.append(nthLine + "\n");
			}
			FileUtils.write(artifactNameToTapeMappingFile, sb.toString());
		}catch (Exception e) {
			String errorMsg = "Unable to queryByVolumeId - " + e.getMessage();
			logger.error(errorMsg, e);
	
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		logger.info("assignArtifactclass - " + artifactNameToTapeMappingFilepathname + " updated");
		return ResponseEntity.status(HttpStatus.OK).body(artifactNameToTapeMappingFilepathname + " updated");
	}

}
