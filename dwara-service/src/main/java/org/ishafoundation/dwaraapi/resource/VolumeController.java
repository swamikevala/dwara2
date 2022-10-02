package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.api.req.GenerateMezzanineProxiesRequest;
import org.ishafoundation.dwaraapi.api.req.RewriteRequest;
import org.ishafoundation.dwaraapi.api.req.initialize.InitializeUserRequest;
import org.ishafoundation.dwaraapi.api.req.volume.MarkVolumeStatusRequest;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Tape;
import org.ishafoundation.dwaraapi.api.resp.initialize.InitializeResponse;
import org.ishafoundation.dwaraapi.api.resp.request.RequestResponse;
import org.ishafoundation.dwaraapi.api.resp.request.RestoreTapeAndMoveItToCPServerResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.MarkVolumeStatusResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.VolumeResponse;
import org.ishafoundation.dwaraapi.enumreferences.Action;
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
	
	
	@GetMapping(value = "/tape/summary", produces = "application/json")
	public ResponseEntity<List<Tape>> handleTapes(){
		logger.info("/tape/summary");
		List<Tape> handleTapeList = null;
		try {
			handleTapeList = volumeService.handleTapes();
		}catch (Exception e) {
			String errorMsg = "Unable to get Tape summary - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(handleTapeList);
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
	public ResponseEntity<InitializeResponse> finalize(@RequestParam String volume){
		
		InitializeResponse finalizeResponse = new InitializeResponse();
		try {
			volumeService.finalize(volume);
			finalizeResponse.setAction(Action.finalize.name());
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
	public ResponseEntity<InitializeResponse> rewriteArtifact(@RequestBody RewriteRequest rewriteRequest, @PathVariable("volumeId") String volumeId) {
		logger.info("/volume/" + volumeId + "/rewrite");
		InitializeResponse rewriteResponse = new InitializeResponse();
		try {
			volumeService.rewriteVolume(volumeId, rewriteRequest);
			rewriteResponse.setAction(Action.rewrite.name());
		}catch (Exception e) {
			String errorMsg = "Unable to rewrite volume - " + e.getMessage();
			logger.error(errorMsg, e);

			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(rewriteResponse);
	}
	
	@ApiOperation(value = "Create mezzanine proxies for all artifacts on the volume")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok")
	})
	@PostMapping(value = "/volume/{volumeId}/generateMezzanineProxies", produces = "application/json")
	public ResponseEntity<RequestResponse> generateMezzanineProxies(@RequestBody GenerateMezzanineProxiesRequest generateMezzanineProxiesRequest, @PathVariable("volumeId") String volumeId) {
		logger.info("/volume/" + volumeId + "/generateMezzanineProxies");
		RequestResponse generateMezzanineProxiesResponse = new RequestResponse();
		try {
			generateMezzanineProxiesResponse = volumeService.generateMezzanineProxies(volumeId, generateMezzanineProxiesRequest);
			generateMezzanineProxiesResponse.setAction(Action.generate_mezzanine_proxies.name());
		}catch (Exception e) {
			String errorMsg = "Unable to generate mezzanine proxies - " + e.getMessage();
			logger.error(errorMsg, e);

			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(generateMezzanineProxiesResponse);
	}

	@ApiOperation(value = "Restore on Ingest and Create mezzanine proxies for all artifacts on the volume on CP server")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok")
	})
	@PostMapping(value = "/volume/{volumeId}/restoreTapeAndMoveItToCPServer", produces = "application/json")
	public ResponseEntity<List<RestoreTapeAndMoveItToCPServerResponse>> restoreTapeAndMoveItToCPServer(@RequestBody GenerateMezzanineProxiesRequest generateMezzanineProxiesRequest, @PathVariable("volumeId") String volumeId) {
		logger.info("/volume/" + volumeId + "/restoreTapeAndMoveItToCPServer");
		List<RestoreTapeAndMoveItToCPServerResponse> response = null;
		try {			
			response = volumeService.restoreTapeAndMoveItToCPServer(volumeId, generateMezzanineProxiesRequest);			
		}catch (Exception e) {
			String errorMsg = "Unable to retoreTapeAndMoveItToCPServer - " + e.getMessage();
			logger.error(errorMsg, e);

			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@ApiOperation(value = "Marks a volume's healthstatus suspect|defective|normal")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok")
	})
	@PostMapping(value = "/volume/{volumeId}/healthstatus/{healthstatus}", produces = "application/json")
	public ResponseEntity<MarkVolumeStatusResponse> markVolumeHealthstatus(@RequestBody MarkVolumeStatusRequest markVolumeStatusRequest, @PathVariable("volumeId") String volumeId, @PathVariable("healthstatus") String healthstatus) {
		logger.info("/volume/" + volumeId + "/healthstatus/" + healthstatus);
		
		MarkVolumeStatusResponse markVolumeStatusResponse = null;
		try {
			markVolumeStatusResponse = volumeService.markVolumeHealthstatus(volumeId, healthstatus, markVolumeStatusRequest);
		}catch (Exception e) {
			String errorMsg = "Unable to mark volume healthstatus - " + e.getMessage();
			logger.error(errorMsg, e);

			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(markVolumeStatusResponse);
		
	}
	
	@ApiOperation(value = "Marks a volume's lifecyclestage active|retired|purged")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok")
	})
	@PostMapping(value = "/volume/{volumeId}/lifecyclestage/{lifecyclestage}", produces = "application/json")
	public ResponseEntity<MarkVolumeStatusResponse> markVolumeLifecyclestage(@RequestBody MarkVolumeStatusRequest markVolumeStatusRequest, @PathVariable("volumeId") String volumeId, @PathVariable("lifecyclestage") String lifecyclestage) {
		logger.info("/volume/" + volumeId + "/lifecyclestage/" + lifecyclestage);
		
		MarkVolumeStatusResponse markVolumeStatusResponse = null;
		try {
			markVolumeStatusResponse = volumeService.markVolumeLifecyclestage(volumeId, lifecyclestage, markVolumeStatusRequest);
		}catch (Exception e) {
			String errorMsg = "Unable to mark volume lifecyclestage - " + e.getMessage();
			logger.error(errorMsg, e);

			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(markVolumeStatusResponse);
		
	}
}
