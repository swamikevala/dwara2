package org.ishafoundation.dwaraapi.resource;

import java.util.List;

import org.ishafoundation.dwaraapi.api.req._import.BulkImportRequest;
import org.ishafoundation.dwaraapi.api.req._import.ImportRequest;
import org.ishafoundation.dwaraapi.api.resp._import.ImportResponse;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.ImportService;
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
	@PostMapping(value = "/import", produces = "application/json")
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
}
