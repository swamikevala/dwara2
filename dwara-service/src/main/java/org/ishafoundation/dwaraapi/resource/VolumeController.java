package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.req.initialize.InitializeUserRequest;
import org.ishafoundation.dwaraapi.api.resp.initialize.InitializeResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.VolumeResponse;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.enumreferences.TapeStoragesubtype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.VolumeService;
import org.ishafoundation.dwaraapi.storage.storagesubtype.AbstractStoragesubtype;
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
	private VolumeDao volumeDao;
	
	@Autowired
	VolumeService volumeService;
	
	@Autowired
	Configuration configuration;
	
	@Autowired
	private Map<String, AbstractStoragesubtype> storagesubtypeMap;
	
	@ApiOperation(value = "Initialization comment goes here")
	@ApiResponses(value = { 
		    @ApiResponse(code = 202, message = "Request submitted and queued up"),
		    @ApiResponse(code = 400, message = "Error")
	})
	@PostMapping(value = "/tape/initialize", produces = "application/json") // TODO API URL shouldnt be having tape but volume
    public ResponseEntity<InitializeResponse> initialize(@RequestBody List<InitializeUserRequest> initializeRequestList){
		
		InitializeResponse initializeResponse = null;
		try {
			validateUserRequest(initializeRequestList); // throws exception...
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
	

	/*
	Volume ids should not be in use
	Volume id sequence numbers should be contiguous
	Volume id validations required by the storagesubtype (e.g. L7 suffix for LTO-7 tapes)
	Volume blocksize should be multiple of 64KiB
	
	// TODO How to let UI know the following...
	Volume Group should be defined (db)
	Storagesubtype should be defined (enum)
	*/
	private void validateUserRequest(List<InitializeUserRequest> initializeRequestList) {
		// Caching the volume Groups so can be accessed in the for loop below
		List<Volume> volumeGroupList = volumeDao.findAllByType(Volumetype.group); 
		Map<String, Volume> volumeGroupId_Volume_Map = new HashMap<String, Volume>();
		for (Volume volume : volumeGroupList) {
			volumeGroupId_Volume_Map.put(volume.getId(), volume);
		}

		// Ordering the initializeRequests by sequence Number
		Map<String, Map<Integer, InitializeUserRequest>> volumeGroup_volumeNumericSequence_InitializeRequest = new HashMap<String, Map<Integer, InitializeUserRequest>>();
		for (InitializeUserRequest nthInitializeRequest : initializeRequestList) {
			String volumeId = nthInitializeRequest.getVolume();
			String volumeGroupId = nthInitializeRequest.getVolumeGroup();
			int sequenceOnLabel = getSequenceUsedOnVolumeLabel(volumeId, null);
			Map<Integer, InitializeUserRequest> volumeNumericSequence_InitializeRequest = volumeGroup_volumeNumericSequence_InitializeRequest.get(volumeGroupId);
			if(volumeNumericSequence_InitializeRequest == null) {
				volumeNumericSequence_InitializeRequest = new HashMap<Integer, InitializeUserRequest>();
				volumeGroup_volumeNumericSequence_InitializeRequest.put(volumeGroupId, volumeNumericSequence_InitializeRequest);
			}
			volumeNumericSequence_InitializeRequest.put(sequenceOnLabel, nthInitializeRequest);
		}
		
		// iterating through the volumegroup related tapes
		Set<String> volumeGroupSet = volumeGroup_volumeNumericSequence_InitializeRequest.keySet();
		for (String nthVolumeGroup : volumeGroupSet) {
			Map<Integer, InitializeUserRequest> volumeNumericSequence_InitializeRequest = volumeGroup_volumeNumericSequence_InitializeRequest.get(nthVolumeGroup);

			Set<Integer> volumeNumericSequenceSet = volumeNumericSequence_InitializeRequest.keySet();
			List<Integer> volumeNumericSequenceList = new ArrayList<Integer>(volumeNumericSequenceSet) ;        //set -> list
			//Sort the list
			Collections.sort(volumeNumericSequenceList);

			for (Integer volumeNumericSequence : volumeNumericSequenceList) {
				InitializeUserRequest nthInitializeRequest = volumeNumericSequence_InitializeRequest.get(volumeNumericSequence);
				String volumeId = nthInitializeRequest.getVolume();
				if(nthInitializeRequest.getForce()) {
					if(!configuration.isAllowForceOptionForTesting())
						throw new DwaraException("Force option not supported just yet. Volume " + volumeId, null);
				}
					
				
				// #1 - Volume ids should not be in use
				try {
					Volume volume = volumeDao.findById(volumeId).get(); // TODO: if force=true, means we are trying to reinitialize an existing tape. How about that??? Means what happens to the existing artifact/file_volume entries???
					throw new DwaraException("Volume " + volumeId + " already in use" , null);
				}
				catch (Exception e) {
					
				}
					

				// #5 - Volume Group should be defined (db)
				String volumeGroupId = nthInitializeRequest.getVolumeGroup();
				Volume volumeGroup = volumeGroupId_Volume_Map.get(volumeGroupId);
				if(volumeGroup == null)
					throw new DwaraException("Volume Group " + volumeGroupId + " doesnt exist" , null);


				// #2 - Volume id sequence numbers should be contiguous
				int currentNumber = volumeGroup.getSequence().getCurrrentNumber();
				int expectedSequenceOnLabel = volumeGroup.getSequence().incrementCurrentNumber();
				String prefix = volumeGroup.getSequence().getPrefix();
				int sequenceOnLabel = getSequenceUsedOnVolumeLabel(volumeId, prefix);
				if(sequenceOnLabel != expectedSequenceOnLabel)
					throw new DwaraException("Sequence number for Volume " + volumeId + " with " + sequenceOnLabel + " is not contiguous. Expected numeric sequence - " + expectedSequenceOnLabel, null);

				// #6 - Storagesubtype should be defined (enum)
				String storagesubtypeStr = nthInitializeRequest.getStoragesubtype();
				TapeStoragesubtype storagesubtype = TapeStoragesubtype.getStoragesubtype(storagesubtypeStr);
				if(storagesubtype == null)
					throw new DwaraException("Storagesubtype " + storagesubtypeStr + " not supported" , null);
				
				// #3 - Volume id validations required by the storagesubtype (e.g. L7 suffix for LTO-7 tapes)
				storagesubtypeMap.get(storagesubtypeStr).validateVolumeId(volumeId);
				
				
				// #4 - Volume blocksize should be multiple of 64KiB
				int divisorInBytes = 65536; // 64 * 1024
				Integer volumeBlocksize = nthInitializeRequest.getVolumeBlocksize();
				if(volumeBlocksize%divisorInBytes != 0) {
					throw new DwaraException("Volume " + volumeId + " blocksize is not in multiple of 64KiB" , null);
				}
				
				logger.trace("All validation good for " + volumeId);
			}	
		}
	}

	private int getSequenceUsedOnVolumeLabel(String volumeId, String prefix){
		String regEx = "([0-9]*)";
		if(prefix != null)
			regEx = prefix + regEx;
		Pattern regExPattern = Pattern.compile(regEx);
		Matcher regExMatcher = regExPattern.matcher(volumeId);
		while(regExMatcher.find()) {
			String numericSequence = regExMatcher.group(1);
			if(StringUtils.isNotBlank(numericSequence))
				return Integer.parseInt(numericSequence);
		}
		return 0;
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
}
