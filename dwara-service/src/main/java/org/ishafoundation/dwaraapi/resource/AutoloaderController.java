package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ishafoundation.dwaraapi.api.resp.autoloader.AutoloaderResponse;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Drive;
import org.ishafoundation.dwaraapi.api.resp.autoloader.DriveStatus;
import org.ishafoundation.dwaraapi.api.resp.autoloader.ToLoadTape;
import org.ishafoundation.dwaraapi.api.resp.mapdrives.MapDrivesResponse;
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
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.AutoloaderMapDrivesService;
import org.ishafoundation.dwaraapi.service.AutoloaderService;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeOnLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class AutoloaderController {

	private static final Logger logger = LoggerFactory.getLogger(AutoloaderController.class);

	@Autowired
	private DeviceDao deviceDao;
	
	@Autowired
	private RequestDao requestDao;

	@Autowired
	private JobDao jobDao;

	@Autowired
	private AutoloaderMapDrivesService autoloaderMapDrivesService;
	
	@Autowired
	private AutoloaderService autoloaderService;
	
	@Autowired
	private Map<String, AbstractStoragetaskAction> storagetaskActionMap;
	
	@Autowired
	private JobUtil jobUtil;

	@Autowired
	private TapeLibraryManager tapeLibraryManager;
	
	@PostMapping(value = "/autoloader/{autoloaderId}/mapDrives", produces = "application/json")
	public ResponseEntity<MapDrivesResponse> mapDrives(@PathVariable("autoloaderId") String autoloaderId){
		MapDrivesResponse mapDrivesResponse = null;
		
		try {
			mapDrivesResponse = autoloaderMapDrivesService.mapDrives(autoloaderId);
		}catch (Exception e) {
			String errorMsg = "Unable to map drives - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(mapDrivesResponse);
	}
	
	@GetMapping(value = "/autoloader", produces = "application/json")
	public ResponseEntity<List<AutoloaderResponse>> getAllAutoloaders(){
		logger.info("/autoloader");
		List<AutoloaderResponse> autoloaderResponseList = new ArrayList<AutoloaderResponse>();
		try {
			//List<Device> autoloaderDevices = configurationTablesUtil.getAllConfiguredAutoloaderDevices();
			List<Device> autoloaderDevices = deviceDao.findAllByType(Devicetype.tape_autoloader);
			for (Device autoloaderDevice : autoloaderDevices) {
				AutoloaderResponse autoloaderResponse = autoloaderService.getAutoloader(autoloaderDevice);
				autoloaderResponseList.add(autoloaderResponse);
			}
		}catch (Exception e) {
			String errorMsg = "Unable to get autoloader details - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(autoloaderResponseList);
	}
	
	@GetMapping(value = "/autoloader/{autoloaderId}", produces = "application/json")
	public ResponseEntity<AutoloaderResponse> getAutoloader(@PathVariable("autoloaderId") String autoloaderId){ // TODO is this id or uid thats going to be requested? should be uid
		logger.info("/autoloader/" + autoloaderId);
		Device autoloaderDevice = deviceDao.findById(autoloaderId).get();
		if(autoloaderDevice == null) {
			throw new DwaraException(autoloaderId + " does not exist in the system", null);
		}
		AutoloaderResponse autoloaderResponse = autoloaderService.getAutoloader(autoloaderDevice);
		return ResponseEntity.status(HttpStatus.OK).body(autoloaderResponse);
	}
	
	@GetMapping(value = "/autoloader/driveSummary", produces = "application/json")
	public ResponseEntity<List<Drive>> getDriveSummary(){
		logger.info("/autoloader/driveSummary");
		List<Drive> drives = new ArrayList<Drive>();
		try {
			List<Device> autoloaderDevices = deviceDao.findAllByType(Devicetype.tape_autoloader);
			for (Device autoloaderDevice : autoloaderDevices) {
				String autoloaderId = autoloaderDevice.getId();
				drives.addAll(autoloaderService.getAllDrivesDetails(autoloaderId));
			}
		}catch (Exception e) {
			String errorMsg = "Unable to get autoloader details - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(drives);
	}	
	
	@GetMapping(value = "/autoloader/toLoad", produces = "application/json")
	public ResponseEntity<Set<ToLoadTape>> getOfflineTapesToBeLoaded(){ 
		logger.info("/autoloader/toLoad");
		// get all queued storagetask jobs and their needed tapes
		
		// get all jobs with volume id not null and storagetask is not null and status is queued
//		List<Job> queuedJobListWithVolume = jobDao.findAllByVolumeIdIsNotNullAndStoragetaskActionIdIsNotNullAndStatus(Status.queued); // job has volume for restore and finalize
//		List<Job> queuedJobListWithVolumeGroup = jobDao.findAllByGroupVolumeIdIsNotNullAndStoragetaskActionIdIsNotNullAndStatus(Status.queued); // job has volume group for ingest 
		
		Set<ToLoadTape> toLoadTapeList = new HashSet<ToLoadTape>();
		try {
			List<Job> queuedJobList = null;
			
			List<Status> statusList = new ArrayList<Status>();
			statusList.add(Status.queued);
			statusList.add(Status.in_progress);
			
			List<Request> rewriteSystemRequestList = requestDao.findAllByActionIdAndStatusInAndType(Action.rewrite, statusList, RequestType.system);
			if(rewriteSystemRequestList.size() > 0) { // if there are any rewrite request pending, dont add all its jobs to the queue
				queuedJobList = jobDao.findAllByStoragetaskActionIdIsNotNullAndRequestActionIdIsNotAndStatusOrderById(Action.rewrite, Status.queued);
				// add just handful of rewrite specific jobs to the collection
				queuedJobList.addAll(jobDao.findTop3ByStoragetaskActionIdAndRequestActionIdAndStatusOrderByRequestId(Action.restore, Action.rewrite, Status.queued)); 
				queuedJobList.addAll(jobDao.findTop3ByStoragetaskActionIdAndRequestActionIdAndStatusOrderByRequestId(Action.write, Action.rewrite, Status.queued));
			}
			else
				queuedJobList = jobDao.findAllByStoragetaskActionIdIsNotNullAndStatusOrderById(Status.queued); // Irrespective of the tapedrivemapping or format request non storage jobs can still be dequeued, hence we are querying it all...

			// job has volume group for ingest
			if(queuedJobList.size() == 0)
				logger.info("No storage jobs in queue");
			else {
				List<String> onlineVolumeList = new ArrayList<String>();
				Map<String, String> onlineVolume_Autoloader_Map = new HashMap<String, String>();
	
				// get all online tapes across all libraries
				List<Device> autoloaderDevices = deviceDao.findAllByType(Devicetype.tape_autoloader);
				for (Device autoloaderDevice : autoloaderDevices) {
					String autoloaderId = autoloaderDevice.getId();
					List<TapeOnLibrary> tapeOnLibraryList = tapeLibraryManager.getAllLoadedTapesInTheLibrary(autoloaderDevice.getWwnId());
					
					for (TapeOnLibrary tapeOnLibrary : tapeOnLibraryList) {
						String barcode = tapeOnLibrary.getVolumeTag();
						onlineVolumeList.add(barcode);
						onlineVolume_Autoloader_Map.put(barcode, autoloaderId);
					}
				}
	
				
				int priorityCount = 1;
				for (Job nthQueuedJob : queuedJobList) {
					if(!jobUtil.isJobReadyToBeExecuted(nthQueuedJob))
					continue;
					
					AbstractStoragetaskAction storagetaskActionImpl = storagetaskActionMap.get(nthQueuedJob.getStoragetaskActionId().name());
					logger.trace("Building storage job - " + nthQueuedJob.getId() + ":" + storagetaskActionImpl.getClass().getSimpleName());
					StorageJob storageJob = null;
					try {
						storageJob = storagetaskActionImpl.buildStorageJob(nthQueuedJob);
					} catch (Exception e) {
						logger.error("Unable to gather necessary details for executing the job " + nthQueuedJob.getId() + " - " + Status.failed, e);
						continue;
					}
					
					Volume volume = storageJob.getVolume();
					if(volume != null) {
						String barcode = volume.getId();
						if(!onlineVolumeList.contains(barcode)) {
							ToLoadTape toLoadTape = new ToLoadTape();
							toLoadTape.setAutoloader(onlineVolume_Autoloader_Map.get(barcode));
							toLoadTape.setBarcode(barcode);
							toLoadTape.setFinalized(volume.isFinalized());
							toLoadTape.setLocation(volume.getLocation().getId());
							toLoadTape.setPriority(priorityCount);
							toLoadTapeList.add(toLoadTape);
							priorityCount = priorityCount + 1;
						}
					}
				}
			}
		}
		catch (Exception e) {
			String errorMsg = "Unable to get toload details - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(toLoadTapeList);
	}
}	