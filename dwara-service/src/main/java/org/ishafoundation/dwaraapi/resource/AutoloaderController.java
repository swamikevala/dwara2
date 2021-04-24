package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.resp.autoloader.AutoloaderResponse;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Element;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Tape;
import org.ishafoundation.dwaraapi.api.resp.autoloader.TapeStatus;
import org.ishafoundation.dwaraapi.api.resp.autoloader.ToLoadTape;
import org.ishafoundation.dwaraapi.api.resp.mapdrives.MapDrivesResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.VolumeResponse;
import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.utils.JobUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.service.AutoloaderMapDrivesService;
import org.ishafoundation.dwaraapi.service.AutoloaderService;
import org.ishafoundation.dwaraapi.service.VolumeService;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.TapeOnLibrary;
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
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class AutoloaderController {

	private static final Logger logger = LoggerFactory.getLogger(AutoloaderController.class);

	@Autowired
	private DeviceDao deviceDao;

	@Autowired
	private JobDao jobDao;

	@Autowired
	private AutoloaderMapDrivesService autoloaderMapDrivesService;
	
	@Autowired
	private AutoloaderService autoloaderService;
	
	@Autowired
	private VolumeService volumeService;
	
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
		Device autoloaderDevice = deviceDao.findById(autoloaderId).get();
		if(autoloaderDevice == null) {
			throw new DwaraException(autoloaderId + " does not exist in the system", null);
		}
		AutoloaderResponse autoloaderResponse = autoloaderService.getAutoloader(autoloaderDevice);
		return ResponseEntity.status(HttpStatus.OK).body(autoloaderResponse);
	}
	
	@GetMapping(value = "/autoloader/handleTapes", produces = "application/json")
	public ResponseEntity<Set<Tape>> handleTapes(){
		logger.info("/autoloader/handleTapes");
		Set<Tape> handleTapeList = new HashSet<Tape>();
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
			
//			List<Status> statusList = new ArrayList<Status>();
//			statusList.add(Status.queued);
//			statusList.add(Status.in_progress);
//			
//			List<Job> jobList = jobDao.findAllByStoragetaskActionIdIsNotNullAndStatusInOrderById(statusList);
			
			// Add tapes - for queued jobs not in tape library 			
			List<Job> jobList = jobDao.findAllByStoragetaskActionIdIsNotNullAndStatusOrderById(Status.queued);
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
							handleTapeList.add(tapeNeeded);
							logger.debug(tapeUsageStatus + " but tape " + barcode + " missing in library");
							priorityCount = priorityCount + 1;
						}
					}
				}
			}

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

			// Remove/Written tapes - No jobs queued and either finalized or removeAfterJob
			for (Tape nthTapeOnLibrary : tapeList) {
				if(nthTapeOnLibrary.getUsageStatus() == TapeUsageStatus.no_job_queued && (nthTapeOnLibrary.getStatus() == TapeStatus.finalized || (nthTapeOnLibrary.isRemoveAfterJob() != null && nthTapeOnLibrary.isRemoveAfterJob()))) {
					// last job on tape determines if the tape need to be shown in remove tapes(restore) or written tapes
					Job lastJobOnTape = jobDao.findTopByStoragetaskActionIdIsNotNullAndVolumeIdAndStatusAndCompletedAtIsNotNullOrderByCompletedAtDesc(nthTapeOnLibrary.getBarcode(), Status.completed);
					Request request = lastJobOnTape.getRequest();
					Action requestedAction = request.getActionId();
					if(requestedAction == Action.ingest)
						nthTapeOnLibrary.setAction("write");
					else
						nthTapeOnLibrary.setAction("restore");
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
		return ResponseEntity.status(HttpStatus.OK).body(handleTapeList);
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
			List<Job> queuedJobList = jobDao.findAllByStoragetaskActionIdIsNotNullAndStatusOrderById(Status.queued); // job has volume group for ingest
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