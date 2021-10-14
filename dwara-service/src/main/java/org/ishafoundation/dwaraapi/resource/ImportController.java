package org.ishafoundation.dwaraapi.resource;

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
	@PostMapping(value = "/import", produces = "application/json")
	public ResponseEntity<ImportResponse> importCatalog(@RequestBody ImportRequest importRequest) throws Exception {
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
		
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(importResponse);
	}
}
