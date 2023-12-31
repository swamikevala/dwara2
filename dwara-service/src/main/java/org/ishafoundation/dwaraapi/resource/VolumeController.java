package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.api.req.RewriteRequest;
import org.ishafoundation.dwaraapi.api.req.initialize.InitializeUserRequest;
import org.ishafoundation.dwaraapi.api.req.volume.MarkVolumeStatusRequest;
import org.ishafoundation.dwaraapi.api.resp.initialize.InitializeResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.MarkVolumeStatusResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.VolumeResponse;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.enumreferences.TapeStoragesubtype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.VolumeService;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.VolumeInitializer;
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
public class VolumeController {
	
	private static final Logger logger = LoggerFactory.getLogger(VolumeController.class);
	
	@Autowired
	private VolumeInitializer volumeInitializer;
	
	@Autowired
	private VolumeService volumeService;
	
	@ApiOperation(value = "Initialization comment goes here")
	@ApiResponses(value = { 
		    @ApiResponse(code = 202, message = "Request submitted and queued up"),
		    @ApiResponse(code = 400, message = "Error")
	})
	@PostMapping(value = "/tape/initialize", produces = "application/json") // TODO API URL shouldnt be having tape but volume
    public ResponseEntity<InitializeResponse> initialize(@RequestBody List<InitializeUserRequest> initializeRequestList){
		
		InitializeResponse initializeResponse = null;
		try {
			volumeInitializer.validateInitializeUserRequest(initializeRequestList); // throws exception...
			initializeResponse = volumeService.initialize(initializeRequestList);
		}catch (Exception e) {
			String errorMsg = "Unable to initialize - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(initializeResponse);
	}
	
	@GetMapping(value = "/storagesubtype", produces = "application/json")
	public ResponseEntity<Map<String, List<String>>> getAllStoragesubtypes(){
		Map<String, List<String>> storagetype_Storagesubtypes_Map = new HashMap<String, List<String>>();
		Storagetype[] storagetypes = Storagetype.values();
		for (int i = 0; i < storagetypes.length; i++) {
			Storagetype storagetype = storagetypes[i];

			List<String> storagesubtypeList = new ArrayList<String>();
			if(storagetype == Storagetype.tape) {
				for (TapeStoragesubtype nthStoragesubtype : TapeStoragesubtype.values()) {
					storagesubtypeList.add(nthStoragesubtype.getJavaStyleStoragesubtype());
				}
			}
			storagetype_Storagesubtypes_Map.put(storagetype.name(), storagesubtypeList);
		}
		return ResponseEntity.status(HttpStatus.OK).body(storagetype_Storagesubtypes_Map);
	}	
	
	@GetMapping(value = "/volume", produces = "application/json")
	public ResponseEntity<List<VolumeResponse>> getVolumeByVolumetype(@RequestParam("type") String volumetype){
		logger.info("/volume?type="+volumetype);
		List<VolumeResponse> volumeResponseList = null;
		try {
			volumeResponseList = volumeService.getVolumeByVolumetype(volumetype);
		}catch (Exception e) {
			String errorMsg = "Unable to get Volume details - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(volumeResponseList);
	}
	
	@GetMapping(value = "/volume/{id}", produces = "application/json")
	public ResponseEntity<VolumeResponse> getVolume(@PathVariable("id") String id){
		VolumeResponse volumeResponse = null;
		try {
			volumeResponse = volumeService.getVolume(id);
		}catch (Exception e) {
			String errorMsg = "Unable to get Volume details - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(volumeResponse);
	}
	
	@ApiOperation(value = "Finalization comment goes here")
	@ApiResponses(value = { 
		    @ApiResponse(code = 202, message = "Request submitted and queued up"),
		    @ApiResponse(code = 400, message = "Error")
	})
	@PostMapping(value = "/volume/finalize", produces = "application/json")
	public ResponseEntity<String> finalize(@RequestParam String volume){
		
		String finalizeResponse = null;
		try {
			finalizeResponse = volumeService.finalize(volume);
		}catch (Exception e) {
			String errorMsg = "Unable to finalize - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(finalizeResponse);
	}
	
	@ApiOperation(value = "Generates the volume index and saves it in the configured temp location. Useful in checking ???")
	@ApiResponses(value = { 
		    @ApiResponse(code = 200, message = "Saves the generated volume index in the configured temp location"),
		    @ApiResponse(code = 400, message = "Error")
	})
	@PostMapping(value = "/volume/generateVolumeindex", produces = "application/json")
	public ResponseEntity<String> generateVolumeindex(@RequestParam String volume){
		
		String response = null;
		try {
			response = volumeService.generateVolumeindex(volume);
		}catch (Exception e) {
			String errorMsg = "Unable to generate volume index - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	}
	
	@ApiOperation(value = "Rewrite the volume. To find the src copy volumes needed in library use something like where R39805L7 is the defective volume and R198% is the sourceCopy group - select distinct(volume_id) from artifact1_volume where artifact_id in (select artifact_id from artifact1_volume where volume_id= 'R39805L7') and volume_id like 'R198%';")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok")
	})
	@PostMapping(value = "/volume/{volumeId}/rewrite", produces = "application/json")
	public ResponseEntity<String> rewriteArtifact(@RequestBody RewriteRequest rewriteRequest, @PathVariable("volumeId") String volumeId) {
		logger.info("/volume/" + volumeId + "/rewrite");
		
		try {
			volumeService.rewriteVolume(volumeId, rewriteRequest);
		}catch (Exception e) {
			String errorMsg = "Unable to rewrite volume - " + e.getMessage();
			logger.error(errorMsg, e);

			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body("Done");
	}
	
	@ApiOperation(value = "Marks a volume suspect|defective|normal")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok")
	})
	@PostMapping(value = "/volume/{volumeId}/healthstatus/{status}", produces = "application/json")
	public ResponseEntity<MarkVolumeStatusResponse> markVolumeStatus(@RequestBody MarkVolumeStatusRequest markVolumeStatusRequest, @PathVariable("volumeId") String volumeId, @PathVariable("status") String status) {
		logger.info("/volume/" + volumeId + "/healthstatus/" + status);
		
		MarkVolumeStatusResponse markVolumeStatusResponse = null;
		try {
			markVolumeStatusResponse = volumeService.markVolumeStatus(volumeId, status, markVolumeStatusRequest);
		}catch (Exception e) {
			String errorMsg = "Unable to mark volume status - " + e.getMessage();
			logger.error(errorMsg, e);

			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(markVolumeStatusResponse);
		
	}
}
