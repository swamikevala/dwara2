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
import org.ishafoundation.dwaraapi.api.req.format.FormatUserRequest;
import org.ishafoundation.dwaraapi.api.resp.format.FormatResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.VolumeResponse;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
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
	private Map<String, AbstractStoragesubtype> storagesubtypeMap;
	
	@ApiOperation(value = "Ingest comment goes here")
	@ApiResponses(value = { 
		    @ApiResponse(code = 202, message = "Request submitted and queued up"),
		    @ApiResponse(code = 400, message = "Error")
	})
	@PostMapping(value = "/volume/format", produces = "application/json")
    public ResponseEntity<FormatResponse> format(@RequestBody List<FormatUserRequest> formatRequestList){
		
		FormatResponse formatResponse = null;
		try {
			validateUserRequest(formatRequestList); // throws exception...
			formatResponse = volumeService.format(formatRequestList);
		}catch (Exception e) {
			String errorMsg = "Unable to format - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(formatResponse);
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
	private void validateUserRequest(List<FormatUserRequest> formatRequestList) {
		// Caching the volume Groups so can be accessed in the for loop below
		List<Volume> volumeGroupList = volumeDao.findAllByVolumetype(Volumetype.group); 
		Map<String, Volume> volumeGroupId_Volume_Map = new HashMap<String, Volume>();
		for (Volume volume : volumeGroupList) {
			volumeGroupId_Volume_Map.put(volume.getId(), volume);
		}

		// Ordering the formatRequests by sequence Number
		Map<Integer, FormatUserRequest> volumeNumericSequence_FormatRequest = new HashMap<Integer, FormatUserRequest>();
		for (FormatUserRequest nthFormatRequest : formatRequestList) {
			String volumeId = nthFormatRequest.getVolume();
			int sequenceOnLabel = getSequenceUsedOnVolumeLabel(volumeId, null);
			volumeNumericSequence_FormatRequest.put(sequenceOnLabel, nthFormatRequest);
		}
		Set<Integer> volumeNumericSequenceSet = volumeNumericSequence_FormatRequest.keySet();
		List<Integer> volumeNumericSequenceList = new ArrayList<Integer>(volumeNumericSequenceSet) ;        //set -> list
		//Sort the list
		Collections.sort(volumeNumericSequenceList);

		int numericSequenceIncrementCounter = 1;
		for (Integer volumeNumericSequence : volumeNumericSequenceList) {
			FormatUserRequest nthFormatRequest = volumeNumericSequence_FormatRequest.get(volumeNumericSequence);
			String volumeId = nthFormatRequest.getVolume();
			if(nthFormatRequest.getForce())
				throw new DwaraException("Force option not supported just yet. Volume " + volumeId, null);
			
			// #1 - Volume ids should not be in use
			Volume volume = volumeDao.findById(volumeId); // TODO: if force=true, means we are trying to reformat an existing tape. How about that???
			if(volume != null)
				throw new DwaraException("Volume " + volumeId + " already in use" , null);

			// #5 - Volume Group should be defined (db)
			String volumeGroupId = nthFormatRequest.getVolumeGroup();
			Volume volumeGroup = volumeGroupId_Volume_Map.get(volumeGroupId);
			if(volumeGroup == null)
				throw new DwaraException("Volume Group " + volumeGroupId + " doesnt exist" , null);


			// #2 - Volume id sequence numbers should be contiguous
			int currentNumber = volumeGroup.getSequence().getCurrrentNumber();
			int expectedSequenceOnLabel = currentNumber + numericSequenceIncrementCounter;
			String prefix = volumeGroup.getSequence().getPrefix();
			int sequenceOnLabel = getSequenceUsedOnVolumeLabel(volumeId, prefix);
			if(sequenceOnLabel != expectedSequenceOnLabel)
				throw new DwaraException("Volume " + volumeId + " sequence number is not contiguous. Expected numeric sequence - " + expectedSequenceOnLabel, null);

			// #6 - Storagesubtype should be defined (enum)
			String storagesubtypeStr = nthFormatRequest.getStoragesubtype();
			TapeStoragesubtype storagesubtype = TapeStoragesubtype.getStoragesubtype(storagesubtypeStr);
			if(storagesubtype == null)
				throw new DwaraException("Storagesubtype " + storagesubtypeStr + " not supported" , null);
			
			// #3 - Volume id validations required by the storagesubtype (e.g. L7 suffix for LTO-7 tapes)
			storagesubtypeMap.get(storagesubtypeStr).validateVolumeId(volumeId);
			
			
			// #4 - Volume blocksize should be multiple of 64KiB
			int divisorInBytes = 65536; // 64 * 1024
			Integer volumeBlocksize = nthFormatRequest.getVolumeBlocksize();
			if(volumeBlocksize%divisorInBytes != 0) {
				throw new DwaraException("Volume " + volumeId + " blocksize is not in multiple of 64KiB" , null);
			}
			
			logger.trace("All validation good for " + volumeId);
			numericSequenceIncrementCounter++;
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
	
	@PostMapping(value = "/volume/finalize", produces = "application/json")
	public ResponseEntity<String> finalize(@RequestParam String volumeUid, @RequestParam Domain domain){
		volumeService.finalize(volumeUid, domain);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Done");
	}
}
