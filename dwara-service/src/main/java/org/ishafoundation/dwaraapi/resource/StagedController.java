package org.ishafoundation.dwaraapi.resource;

import java.util.List;

import org.ishafoundation.dwaraapi.api.req.staged.ingest.IngestUserRequest;
import org.ishafoundation.dwaraapi.api.req.staged.rename.StagedRenameFile;
import org.ishafoundation.dwaraapi.api.resp.staged.ingest.IngestResponse;
import org.ishafoundation.dwaraapi.api.resp.staged.rename.StagedRenameResponse;
import org.ishafoundation.dwaraapi.api.resp.staged.scan.StagedFileDetails;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.StagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public class StagedController {

	private static final Logger logger = LoggerFactory.getLogger(StagedController.class);
	
	@Autowired
	private StagedService stagedService;
	

	@ApiOperation(value = "Scans the selected libraryclass passed and lists all candidate folders from across all users to ingest", response = List.class)
	@ApiResponses(value = { 
		    @ApiResponse(code = 200, message = "Ok"),
		    @ApiResponse(code = 404, message = "Not Found")
	})
	@GetMapping(value="/staged/scan", produces = "application/json")
    public ResponseEntity<List<StagedFileDetails>> getAllIngestableFiles(@RequestParam("artifactclass") String artifactclassId){
		List<StagedFileDetails> ingestFileList = stagedService.getAllIngestableFiles(artifactclassId);
		
		if (ingestFileList.size() > 0) {
			return ResponseEntity.ok(ingestFileList);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}
	
	@ApiOperation(value = "Renames the staged file on the physcial file system (before ingesting)")
	@ApiResponses(value = { 
		    @ApiResponse(code = 200, message = "Ok, Request processed but check for any failures on rename in the response body")
	})
	@PostMapping(value = "/staged/rename", produces = "application/json")
    public ResponseEntity<StagedRenameResponse> renameStagedFile(@RequestBody List<StagedRenameFile> stagedRenameUserRequest){
		StagedRenameResponse stagedRenameResponse = stagedService.renameStagedFiles(stagedRenameUserRequest);
		return ResponseEntity.ok(stagedRenameResponse);
	}
	
	@ApiOperation(value = "Ingest comment goes here")
	@ApiResponses(value = { 
		    @ApiResponse(code = 202, message = "Request submitted and queued up"),
		    @ApiResponse(code = 400, message = "Error")
	})
	@PostMapping(value="/staged/ingest", produces = "application/json")
    public ResponseEntity<IngestResponse> ingest(@RequestBody IngestUserRequest ingestUserRequest){
		IngestResponse ingestResponse = null;
		try {
			ingestResponse = stagedService.ingest(ingestUserRequest);
		}catch (Exception e) {
			String errorMsg = "Unable to ingest - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		HttpStatus status = HttpStatus.ACCEPTED;
		if(ingestResponse.getStagedFiles() != null)
			status = HttpStatus.BAD_REQUEST;
		
		return ResponseEntity.status(status).body(ingestResponse);
	}
	
}	