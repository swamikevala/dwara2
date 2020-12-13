package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.restore.PFRestoreUserRequest;
import org.ishafoundation.dwaraapi.api.req.restore.RestoreUserRequest;
import org.ishafoundation.dwaraapi.api.resp.restore.File;
import org.ishafoundation.dwaraapi.api.resp.restore.RestoreResponse;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.FileService;
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

@CrossOrigin
@RestController
public class FileController {
	private static final Logger logger = LoggerFactory.getLogger(FileController.class);

	@Autowired
	FileService fileService;

	@ApiOperation(value = "Restores the list of files requested from location into the target volume grouped under the output dir")
	@GetMapping("/file/list")
	public ResponseEntity<List<File>> list(@RequestParam String ids){
		List<String> fileIdsList = Arrays.asList(ids.split(","));
		
		List<Integer> fileIds = new ArrayList<Integer>();
		for (String fileIdAsString : fileIdsList) {
			int fileId = Integer.parseInt(fileIdAsString);
		
			fileIds.add(fileId);
		}
		List<File> fileList = fileService.list(fileIds);
		return ResponseEntity.status(HttpStatus.OK).body(fileList);
	}

	
	@ApiOperation(value = "Restores the list of files requested from location into the target volume grouped under the output dir")
	@PostMapping("/file/restore/v2")
	public ResponseEntity<RestoreResponse> restore(@RequestBody RestoreUserRequest restoreUserRequest){
		return restore_internal(restoreUserRequest, Action.restore, null);
	}

	@ApiOperation(value = "Restores the list of files requested from location into the target volume grouped under the output dir")
	@PostMapping("/file/restore_process")
	public ResponseEntity<RestoreResponse> restoreProcess(@RequestBody RestoreUserRequest restoreUserRequest){
		return restore_internal(restoreUserRequest, Action.restore_process, restoreUserRequest.getFlow());
	}
	
	private ResponseEntity<RestoreResponse> restore_internal(RestoreUserRequest restoreUserRequest, Action action, String flow){		
		RestoreResponse restoreResponse = null;
		try {
			restoreResponse = fileService.restore(restoreUserRequest, action, flow);
		}catch (Exception e) {
			String errorMsg = "Unable to restore - " + e.getMessage();
			logger.error(errorMsg, e);
	
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(restoreResponse);
	}

	@ApiOperation(value = "Restores the list of files requested from location into the target volume grouped under the output dir")
	@PostMapping("/file/partialFileRestore")
	public ResponseEntity<RestoreResponse> partialFileRestore(@RequestBody PFRestoreUserRequest pfRestoreUserRequest){
		RestoreResponse restoreResponse = null;
		try {
			restoreResponse = fileService.partialFileRestore(pfRestoreUserRequest);
		}catch (Exception e) {
			String errorMsg = "Unable to get data for ltowala - " + e.getMessage();
			logger.error(errorMsg, e);

			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(restoreResponse);
	}
	

}
