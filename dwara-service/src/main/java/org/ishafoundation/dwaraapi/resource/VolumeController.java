package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ishafoundation.dwaraapi.api.req.RewriteRequest;
import org.ishafoundation.dwaraapi.api.req.initialize.InitializeUserRequest;
import org.ishafoundation.dwaraapi.api.req.volume.MarkVolumeStatusRequest;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Tape;
import org.ishafoundation.dwaraapi.api.resp.autoloader.TapeListSorterUsingBarcode;
import org.ishafoundation.dwaraapi.api.resp.autoloader.TapeListSorterUsingSlot;
import org.ishafoundation.dwaraapi.api.resp.autoloader.TapeStatus;
import org.ishafoundation.dwaraapi.api.resp.initialize.InitializeResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.MarkVolumeStatusResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.VolumeResponse;
import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.utils.JobUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.enumreferences.TapeStoragesubtype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.AutoloaderService;
import org.ishafoundation.dwaraapi.service.VolumeService;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.VolumeInitializer;
import org.ishafoundation.dwaraapi.utils.TapeUsageStatus;
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
	
	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private RequestDao requestDao;

	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private AutoloaderService autoloaderService;

	@Autowired
	private Map<String, AbstractStoragetaskAction> storagetaskActionMap;
	
	@Autowired
	private JobUtil jobUtil;
	
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
		List<Tape> handleTapeList = new ArrayList<Tape>();
		try {
			List<String> onlineVolumeList = new ArrayList<String>();
			Map<String, String> onlineVolume_Autoloader_Map = new HashMap<String, String>();
			Map<String, Tape> onlineBarcode_Tape_Map = new HashMap<String, Tape>();
	
			// get all online tapes across all libraries
			Set<Tape> tapeList = new HashSet<Tape>();
			
			List<Device> autoloaderDevices = deviceDao.findAllByType(Devicetype.tape_autoloader);
			for (Device autoloaderDevice : autoloaderDevices) {
				String autoloaderId = autoloaderDevice.getId();
				tapeList.addAll(autoloaderService.getLoadedTapesInLibrary(autoloaderDevice, false));

				for (Tape nthTape : tapeList) {
					String barcode = nthTape.getBarcode();
					onlineVolumeList.add(barcode);
					onlineVolume_Autoloader_Map.put(barcode, autoloaderId);
					onlineBarcode_Tape_Map.put(barcode, nthTape);
				}
			}
			
			logger.trace("Now deal with - Add tapes - for queued jobs not in tape library");
			// Add tapes - for queued jobs not in tape library 
			List<Job> jobList = null;
			
			List<Status> statusList = new ArrayList<Status>();
			statusList.add(Status.queued);
			statusList.add(Status.in_progress);
			
			List<Request> rewriteSystemRequestList = requestDao.findAllByActionIdAndStatusInAndType(Action.rewrite, statusList, RequestType.system);
			if(rewriteSystemRequestList.size() > 0) { // if there are any rewrite request pending, dont add all its jobs to the queue
				jobList = jobDao.findAllByStoragetaskActionIdIsNotNullAndRequestActionIdIsNotAndStatusOrderById(Action.rewrite, Status.queued);
				// add just handful of rewrite specific jobs to the collection
				jobList.addAll(jobDao.findTop3ByStoragetaskActionIdAndRequestActionIdAndStatusOrderByRequestId(Action.restore, Action.rewrite, Status.queued)); 
				jobList.addAll(jobDao.findTop3ByStoragetaskActionIdAndRequestActionIdAndStatusOrderByRequestId(Action.write, Action.rewrite, Status.queued));
			}
			else
				jobList = jobDao.findAllByStoragetaskActionIdIsNotNullAndStatusOrderById(Status.queued); 
			
			logger.trace("Iterating queued jobs");
			if(jobList.size() == 0)
				logger.info("No storage jobs in queue");
			else {
				int priorityCount = 1;
				for (Job nthJob : jobList) {
					Volume volume = null;
					TapeUsageStatus tapeUsageStatus = null;
					if(!jobUtil.isJobReadyToBeExecuted(nthJob))
						continue;
					
					AbstractStoragetaskAction storagetaskActionImpl = storagetaskActionMap.get(nthJob.getStoragetaskActionId().name());
					logger.trace("Building storage job - " + nthJob.getId() + ":" + storagetaskActionImpl.getClass().getSimpleName());
					StorageJob storageJob = null;
					try {
						storageJob = storagetaskActionImpl.buildStorageJob(nthJob);
					} catch (Exception e) {
						logger.error("Unable to gather necessary details for executing the job " + nthJob.getId() + " - " + Status.failed, e);
						continue;
					}
					
					volume = storageJob.getVolume();
					tapeUsageStatus = TapeUsageStatus.job_queued;
					
					if(volume != null) {
						String barcode = volume.getId();
						if(!onlineVolumeList.contains(barcode)) {
							Tape tapeNeeded = new Tape();//onlineBarcode_Tape_Map.get(barcode);
							tapeNeeded.setBarcode(barcode);
							tapeNeeded.setAction(nthJob.getStoragetaskActionId().name());
							tapeNeeded.setLocation(volume.getLocation().getId());
							tapeNeeded.setUsageStatus(tapeUsageStatus);
							//toLoadTape.setAutoloader(onlineVolume_Autoloader_Map.get(barcode));
							if(!handleTapeList.stream().anyMatch(x -> x.equals(tapeNeeded))) // avoid dupe entries...
								handleTapeList.add(tapeNeeded);
							logger.debug(tapeUsageStatus + " but tape " + barcode + " missing in library");
							priorityCount = priorityCount + 1;
						}
					}
				}
			}

			logger.trace("Now deal with - Add tapes");
			// Add tapes - for capacity expansion
			// If there are any groups running out of space and needing new tapes
			List<VolumeResponse> volGroupList = volumeService.getVolumeByVolumetype(Volumetype.group.name());
			for (VolumeResponse volumeResponse : volGroupList) {
				if(volumeResponse.getDetails().isExpandCapacity()) {
					Tape tapeNeeded = new Tape();//onlineBarcode_Tape_Map.get(barcode);
					tapeNeeded.setBarcode(volumeResponse.getDetails().getNextBarcode());
					tapeNeeded.setAction(Action.write.name()); // TODO - Action = Write ??? @MH what action for pools running out of space???
					// tapeNeeded.setUsageStatus(TapeUsageStatus.job_queued);
					handleTapeList.add(tapeNeeded);
				}
			}

			logger.trace("Now deal with - Tapes in action");
			// Show Tapes in action - currently restoring/writing
			List<Job> inProgressJobsList = jobDao.findAllByStoragetaskActionIdIsNotNullAndStatusOrderById(Status.in_progress);
			if(inProgressJobsList.size() == 0)
				logger.info("No storage jobs in progress");
			else {
				for (Job nthJob : inProgressJobsList) {
					Volume volume = nthJob.getVolume();
					TapeUsageStatus tapeUsageStatus = TapeUsageStatus.job_in_progress;
					if(volume != null) {
						String barcode = volume.getId();
						Tape tapeInAction = new Tape();
						tapeInAction.setBarcode(barcode);
						tapeInAction.setAction(nthJob.getStoragetaskActionId().name());
						tapeInAction.setLocation(volume.getLocation().getId());
						tapeInAction.setUsageStatus(tapeUsageStatus);
						handleTapeList.add(tapeInAction);
						logger.debug(barcode + " " + tapeUsageStatus);
					}
				}
			}
			
			logger.trace("Now deal with - Remove/Written tapes - No jobs queued and either finalized or removeAfterJob");
			// Remove/Written tapes - No jobs queued and either finalized or removeAfterJob
			for (Tape nthTapeOnLibrary : tapeList) {
				if(nthTapeOnLibrary.getUsageStatus() == TapeUsageStatus.no_job_queued && (nthTapeOnLibrary.getStatus() == TapeStatus.finalized || (nthTapeOnLibrary.isRemoveAfterJob() != null && nthTapeOnLibrary.isRemoveAfterJob()))) {
					// last job on tape determines if the tape need to be shown in remove tapes(restore) or written tapes
					Job lastJobOnTape = jobDao.findTopByStoragetaskActionIdIsNotNullAndVolumeIdAndStatusAndCompletedAtIsNotNullOrderByCompletedAtDesc(nthTapeOnLibrary.getBarcode(), Status.completed);
					Request request = lastJobOnTape.getRequest();
					Action requestedAction = request.getActionId();
					if(requestedAction == Action.restore_process || requestedAction == Action.restore)
						nthTapeOnLibrary.setAction("restore");
					else
						nthTapeOnLibrary.setAction("write");
					handleTapeList.add(nthTapeOnLibrary);
				}
				
			}
		}
		catch (Exception e) {
			String errorMsg = "Unable to get tape details - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		Collections.sort(handleTapeList, new TapeListSorterUsingBarcode()); // first order the list by barcode
		Collections.sort(handleTapeList, new TapeListSorterUsingSlot()); // next order the list by slot no
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
	
	@ApiOperation(value = "Marks a volume's healthstatus suspect|defective|normal")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok")
	})
	@PostMapping(value = "/volume/{volumeId}/healthstatus/{status}", produces = "application/json")
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
	@PostMapping(value = "/volume/{volumeId}/lifecyclestage/{status}", produces = "application/json")
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
