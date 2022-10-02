package org.ishafoundation.dwaraapi.service;

import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.req.GenerateMezzanineProxiesRequest;
import org.ishafoundation.dwaraapi.api.req.RewriteRequest;
import org.ishafoundation.dwaraapi.api.req.initialize.InitializeUserRequest;
import org.ishafoundation.dwaraapi.api.req.volume.MarkVolumeStatusRequest;
import org.ishafoundation.dwaraapi.api.resp.autoloader.Tape;
import org.ishafoundation.dwaraapi.api.resp.autoloader.TapeListSorterUsingBarcode;
import org.ishafoundation.dwaraapi.api.resp.autoloader.TapeListSorterUsingSlot;
import org.ishafoundation.dwaraapi.api.resp.autoloader.TapeStatus;
import org.ishafoundation.dwaraapi.api.resp.initialize.InitializeResponse;
import org.ishafoundation.dwaraapi.api.resp.job.JobResponse;
import org.ishafoundation.dwaraapi.api.resp.request.RequestResponse;
import org.ishafoundation.dwaraapi.api.resp.request.RestoreTapeAndMoveItToCPServerResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.Details;
import org.ishafoundation.dwaraapi.api.resp.volume.MarkVolumeStatusResponse;
import org.ishafoundation.dwaraapi.api.resp.volume.VolumeResponse;
import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TArtifactnameJobMapDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ArtifactVolumeDao;
import org.ishafoundation.dwaraapi.db.keys.VolumeArtifactServerNameKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.TArtifactnameJobMap;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.JobUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.ishafoundation.dwaraapi.enumreferences.Priority;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.RewriteMode;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.VolumeHealthStatus;
import org.ishafoundation.dwaraapi.enumreferences.VolumeLifecyclestage;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.index.VolumeindexManager;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.VolumeFinalizer;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.VolumeInitializer;
import org.ishafoundation.dwaraapi.utils.CpProxyServerInteracter;
import org.ishafoundation.dwaraapi.utils.TapeUsageStatus;
import org.ishafoundation.dwaraapi.utils.VolumeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class VolumeService extends DwaraService {

	private static final Logger logger = LoggerFactory.getLogger(VolumeService.class);
	
	@Autowired
	private VolumeDao volumeDao;

	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private ArtifactDao artifactDao;
	
	@Autowired
	private ArtifactVolumeDao artifactVolumeDao;
	
	@Autowired
	private VolumeUtil volumeUtil;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private VolumeInitializer volumeInitializer;

	@Autowired
	private VolumeFinalizer volumeFinalizer;
	
	@Autowired
	private JobCreator jobCreator;
	
	@Autowired
	private VolumeindexManager volumeindexManager;

	@Autowired
	private DeviceDao deviceDao;

	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private TArtifactnameJobMapDao tArtifactnameJobMapDao;
	
	@Autowired
	private AutoloaderService autoloaderService;
	
	@Autowired
	private JobService jobService;
	
	@Autowired
	private Map<String, AbstractStoragetaskAction> storagetaskActionMap;
	
	@Autowired
	private JobUtil jobUtil;
	
	@Autowired
	private CpProxyServerInteracter cpProxyServerInteracter;
	
	@Value("${filesystem.temporarylocation}")
	private String filesystemTemporarylocation;

	private static DecimalFormat df = new DecimalFormat("0.00");

	public List<Volume> getVolumeGroupByCopyNumber(int copyId) {
		List<Volume> list = volumeDao.findAllByCopyId(copyId);
		return list;
	}
	
	public List<VolumeResponse> getVolumeByVolumetype(String volumetype){
		List<VolumeResponse> volumeResponseList = null;
			// validate
			Volumetype neededVolumetype = Volumetype.valueOf(volumetype);
			
			volumeResponseList = new ArrayList<VolumeResponse>();
			List<Volume> volumeGroupList = volumeDao.findAllByTypeAndImportedIsFalse(neededVolumetype);
			for (Volume volume : volumeGroupList) {
				volumeResponseList.add(getVolume_Internal(volume));
			}
		return volumeResponseList;
	}

	public VolumeResponse getVolume(String volumeId) {
		Optional<Volume> volumeEntity = volumeDao.findById(volumeId);
		if(volumeEntity.isPresent()) {
			Volume volume = volumeEntity.get();
			return getVolume_Internal(volume);
		}else
			return null;
	}
	
	private VolumeResponse getVolume_Internal(Volume volume) {
		double GiB = 1073741824; // 1 GiB = 1073741824 bytes...
		double TiB = 1099511627776.00;
		double TB = 1000000000000.00;
		String sizeUnit = "TB"; // "GiB"; 
		double sizeUnitDivisor = TB;
		
		VolumeResponse volResp = new VolumeResponse();
		volResp.setId(volume.getId());
		volResp.setCopyNumber(volume.getType() == Volumetype.group ? volume.getCopy().getId() : volume.getGroupRef().getCopy().getId());
		volResp.setVolumetype(volume.getType().name());
		volResp.setStoragetype(volume.getStoragetype().name());
		volResp.setStoragelevel(volume.getStoragelevel().name());
		//volResp.setVolumeRef(volumeRef);
		//volResp.setChecksumtype(checksumtype);
		//volResp.setInitializedAt(initializetedAt);
		volResp.setFinalized(volume.isFinalized());
		volResp.setImported(volume.isImported());
		if(volume.getArchiveformat() != null)
			volResp.setArchiveformat(volume.getArchiveformat().getId());

		if(volume.getType() == Volumetype.group) {
			long groupVolumeCapacity = 0L;
			long groupVolumeUnusedCapacity = 0L;
			long groupVolumeUsedCapacity = 0L;
			long maxPhysicalUnusedCapacity = 0L;

			List<Volume> physicalVolumeList = volumeDao.findAllByGroupRefIdAndImportedIsFalseAndFinalizedIsFalseAndHealthstatusAndLifecyclestageOrderByIdAsc(volume.getId(), VolumeHealthStatus.normal, VolumeLifecyclestage.active); // get all not finalized physical volume in the group
			
			for (Volume nthPhyscialVolume : physicalVolumeList) { // iterate all physical volume from the group and sum up for total/used/unused cap
				logger.trace("Dashboard - " + nthPhyscialVolume.getId());
				
//				long nthPhysicalVolumeCapacity = nthPhyscialVolume.getCapacity();
//				long nthPhysicalVolumeUsedCapacity = volumeUtil.getVolumeUsedCapacity(domain, nthPhyscialVolume);
				groupVolumeCapacity += volumeUtil.getVolumeUsableCapacity(nthPhyscialVolume);//nthPhyscialVolume.getCapacity();
				logger.trace("Dashboard -groupVolumeCapacity - " + groupVolumeCapacity);
				logger.trace("Dashboard -groupVolumeCapacity in " + sizeUnit + " - " + groupVolumeCapacity/sizeUnitDivisor);
				long nthPhysicalVolumeUnusedCapacity = volumeUtil.getVolumeUnusedCapacity(nthPhyscialVolume);
				groupVolumeUnusedCapacity += nthPhysicalVolumeUnusedCapacity;
				logger.trace("Dashboard -groupVolumeUnusedCapacity - " + groupVolumeUnusedCapacity);
				logger.trace("Dashboard -groupVolumeUnusedCapacity in " + sizeUnit + " - "  + groupVolumeUnusedCapacity/sizeUnitDivisor);
				if(maxPhysicalUnusedCapacity < nthPhysicalVolumeUnusedCapacity)
					maxPhysicalUnusedCapacity = nthPhysicalVolumeUnusedCapacity;
				logger.trace("Dashboard -maxPhysicalUnusedCapacity - " + maxPhysicalUnusedCapacity);
				logger.trace("Dashboard -maxPhysicalUnusedCapacity in " + sizeUnit + " - "  + maxPhysicalUnusedCapacity/sizeUnitDivisor);
				groupVolumeUsedCapacity += volumeUtil.getVolumeUsedCapacity(nthPhyscialVolume);
				logger.trace("Dashboard -groupVolumeUsedCapacity - " + groupVolumeUsedCapacity);
				logger.trace("Dashboard -groupVolumeUsedCapacity in " + sizeUnit + " - "  + groupVolumeUsedCapacity/sizeUnitDivisor);
			}

			volResp.setTotalCapacity(Float.valueOf(df.format(groupVolumeCapacity/sizeUnitDivisor)));
			volResp.setUsedCapacity(Float.valueOf(df.format(groupVolumeUsedCapacity/sizeUnitDivisor)));
			volResp.setUnusedCapacity(Float.valueOf(df.format(groupVolumeUnusedCapacity/sizeUnitDivisor)));
			volResp.setMaxPhysicalUnusedCapacity(Float.valueOf(df.format(maxPhysicalUnusedCapacity/sizeUnitDivisor)));
			volResp.setSizeUnit(sizeUnit); 
		}
		
		if(volume.getLocation() != null)
			volResp.setLocation(volume.getLocation().getId());
		
		VolumeDetails volumeDetails = volume.getDetails();
		if(volumeDetails != null) {
			Details details = new Details();
			
			//details.setBarcoded(volumeDetails.isBarcoded());
			if(volumeDetails.getBlocksize() != null) {
				details.setBlocksize(volumeDetails.getBlocksize()/1024);
				details.setBlocksizeUnit("KiB");
			}
			details.setStoragesubtype(volume.getStoragesubtype());
			//details.setMountPoint(mountPoint);
			//details.setProvider(provider);
			//details.setRemoveAfterJob(removeAfterJob);
			if(volume.getType() == Volumetype.group && !volume.isImported()) {
				logger.info(volume.getId() + ":" +  volResp.getUnusedCapacity() + ":" + (volumeDetails.getMinimumFreeSpace()/sizeUnitDivisor));
				if(volResp.getUnusedCapacity() < (volumeDetails.getMinimumFreeSpace()/sizeUnitDivisor))
					details.setExpandCapacity(true);
				details.setNextBarcode(volume.getSequence().getPrefix() + (volume.getSequence().getCurrrentNumber() + 1) + "L7"); // TODO - How to findout LTO Generation???
			}
			
			volResp.setDetails(details);
		}
		return volResp;
	}
	
	public InitializeResponse initialize(List<InitializeUserRequest> initializeRequestList) throws Exception{	
		Action requestedBusinessAction = Action.initialize;
		org.ishafoundation.dwaraapi.db.model.master.reference.Action action = configurationTablesUtil.getAction(requestedBusinessAction);
		if(action == null)
			throw new Exception("Action for " + requestedBusinessAction.name() + " not configured in DB properly. Please set it first");

		return volumeInitializer.initialize(getUserFromContext(), initializeRequestList);
	}
	
	public String finalize(String volumeId) throws Exception{
		return volumeFinalizer.finalize(volumeId, getUserFromContext());
	}

	public String generateVolumeindex(String volumeId) throws Exception {
		Volume volume = volumeDao.findById(volumeId).get();
		
		String tmpXmlFilepathname = filesystemTemporarylocation + File.separator + volumeId + "_index.xml";
		volumeindexManager.createVolumeindexXml(volume, tmpXmlFilepathname);
		
		String response = tmpXmlFilepathname + " created"; 
		logger.trace(response);
		
		return response;
	}
	
	public List<Tape> handleTapes(){
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
			List<VolumeResponse> volGroupList = getVolumeByVolumetype(Volumetype.group.name());
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
		return handleTapeList;
	}
	
	public RequestResponse generateMezzanineProxies(String volumeId, GenerateMezzanineProxiesRequest generateMezzanineProxiesRequest) throws Exception {
		return generateMezzanineProxies(volumeId, generateMezzanineProxiesRequest, Action.generate_mezzanine_proxies);
	}
	
	public RequestResponse generateMezzanineProxies(String volumeId, GenerateMezzanineProxiesRequest generateMezzanineProxiesRequest, Action action) throws Exception {
		Volume volume = volumeDao.findById(volumeId).get();
		// create user request
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("volumeId", volumeId);
		
		Request userRequest = createUserRequest(action, data);
		List<ArtifactVolume> artifactVolumeList = artifactVolumeDao.findAllByIdVolumeIdAndStatus(volumeId, ArtifactVolumeStatus.current); // only not deleted artifacts need to be rewritten
		
		Pattern artifactclassRegexPattern = null;
		String artifactclassRegex = generateMezzanineProxiesRequest.getArtifactclassRegex();
		if(artifactclassRegex != null) {
			artifactclassRegexPattern = Pattern.compile(artifactclassRegex);
			data.put("artifactclassRegex", artifactclassRegex);
		}
		
		Pattern artifactRegexPattern = null;
		String artifactRegex = generateMezzanineProxiesRequest.getArtifactRegex();
		if(artifactRegex != null) {
			artifactRegexPattern = Pattern.compile(artifactRegex);
			data.put("artifactRegex", artifactRegex);
		}
		
		// loop artifacts on volume
		for (ArtifactVolume nthArtifactVolume : artifactVolumeList) {
			int artifactId = nthArtifactVolume.getId().getArtifactId();
			
			Artifact artifact = artifactDao.findById(artifactId).get(); // get the artifact details from DB
 
			// filtering out artifacts that dont match specified artifactclass(es)
			if(artifactclassRegexPattern != null) {
				String artifactclassId = artifact.getArtifactclass().getId();
				Matcher m = artifactclassRegexPattern.matcher(artifactclassId);
				if(!m.matches()) { 
					logger.info("Skipping " + artifact.getName() + "(" + artifactId + ") as " + artifactclassId + " doesnt match " + artifactclassRegex);
					continue;
				}
			}
			
			// filtering out artifacts that dont match specified artifact regex
			if(artifactRegexPattern != null) {
				String artifactName = artifact.getName();
				Matcher m = artifactRegexPattern.matcher(artifactName);
				if(!m.matches()) { 
					logger.info("Skipping " + artifact.getName() + "(" + artifactId + ") as " + artifactName + " doesnt match " + artifactRegex);
					continue;
				}
			}
			
			//create system requests			
			Request systemRequest = new Request();
			systemRequest.setType(RequestType.system);
			systemRequest.setRequestRef(userRequest);
			systemRequest.setActionId(userRequest.getActionId());
			systemRequest.setStatus(Status.queued);
			systemRequest.setRequestedBy(userRequest.getRequestedBy());
			systemRequest.setRequestedAt(LocalDateTime.now());
		
			RequestDetails systemrequestDetails = new RequestDetails();
			systemrequestDetails.setArtifactId(artifactId);
			systemrequestDetails.setVolumeId(volumeId);
			if(generateMezzanineProxiesRequest.getArtifactclassRegex() != null)
				systemrequestDetails.setArtifactclassRegex(generateMezzanineProxiesRequest.getArtifactclassRegex());
			if(generateMezzanineProxiesRequest.getArtifactRegex() != null)
				systemrequestDetails.setArtifactRegex(generateMezzanineProxiesRequest.getArtifactRegex());
			systemRequest.setDetails(systemrequestDetails);
			systemRequest = requestDao.save(systemRequest);
			
			logger.info(DwaraConstants.SYSTEM_REQUEST + systemRequest.getId());

			//create jobs
			jobCreator.createJobs(systemRequest, artifact);
		}
		return frameRequestResponse(userRequest, RequestType.user, userRequest.getId());
	}
	
	
	public List<RestoreTapeAndMoveItToCPServerResponse> restoreTapeAndMoveItToCPServer(String volumeId, GenerateMezzanineProxiesRequest generateMezzanineProxiesRequest) throws Exception {
		RequestResponse responseFromCPProxyServer = callGenerateMezzanineProxiesApiOnCpProxyServer(volumeId, generateMezzanineProxiesRequest);
		
		String cpServerName = "CP-Proxy";
		String ingestServerName = "Ingest";
		List<RequestResponse> systemRequests = responseFromCPProxyServer.getRequest();
		//	save in DB for referencing to create job folder on cp - 
		for (RequestResponse nthSystemRequest : systemRequests) {
			VolumeArtifactServerNameKey volumeArtifactServerNameKey = new VolumeArtifactServerNameKey(volumeId, nthSystemRequest.getArtifact().getName(), cpServerName);
			
			TArtifactnameJobMap tArtifactnameJobMap = new TArtifactnameJobMap();
			tArtifactnameJobMap.setId(volumeArtifactServerNameKey);
			tArtifactnameJobMap.setJobId(Integer.parseInt(nthSystemRequest.getJob().get(0).getId()));
			tArtifactnameJobMapDao.save(tArtifactnameJobMap);
		}
		
		RestoreTapeAndMoveItToCPServerResponse responseFromCPProxy = new RestoreTapeAndMoveItToCPServerResponse();
		responseFromCPProxy.setServerName(cpServerName);
		responseFromCPProxy.setResponse(responseFromCPProxyServer);
		
		
		RequestResponse responseFromIngestServer = generateMezzanineProxies(volumeId, generateMezzanineProxiesRequest, Action.restore_tape_and_move_it_to_cp_proxy_server);
		systemRequests = responseFromIngestServer.getRequest();
		//	save in DB for referencing to create job folder on cp - 
		for (RequestResponse nthSystemRequest : systemRequests) {
			VolumeArtifactServerNameKey volumeArtifactServerNameKey = new VolumeArtifactServerNameKey(volumeId, nthSystemRequest.getArtifact().getName(), ingestServerName);
			
			TArtifactnameJobMap tArtifactnameJobMap = new TArtifactnameJobMap();
			tArtifactnameJobMap.setId(volumeArtifactServerNameKey);
			tArtifactnameJobMap.setJobId(Integer.parseInt(nthSystemRequest.getJob().get(0).getId()));
			tArtifactnameJobMapDao.save(tArtifactnameJobMap);
		}
		
		RestoreTapeAndMoveItToCPServerResponse responseFromIngest = new RestoreTapeAndMoveItToCPServerResponse();
		responseFromIngest.setServerName(ingestServerName);
		responseFromIngest.setResponse(responseFromIngestServer);
		
		List<RestoreTapeAndMoveItToCPServerResponse> requestResponseList = new ArrayList<RestoreTapeAndMoveItToCPServerResponse>();
		requestResponseList.add(responseFromCPProxy);
		requestResponseList.add(responseFromIngest);
		
		return requestResponseList;
	}

	public RequestResponse callGenerateMezzanineProxiesApiOnCpProxyServer(String volumeId, GenerateMezzanineProxiesRequest generateMezzanineProxiesRequest) throws Exception {
		String endpointUrlSuffix = "/volume/" + volumeId + "/generateMezzanineProxies";
		
		ObjectMapper mapper = new ObjectMapper();
		String postBody = mapper.writeValueAsString(generateMezzanineProxiesRequest);
		RequestResponse responseFromCPServer = null;
		
		try {
			String responseFromCPServerAsString = cpProxyServerInteracter.callCpProxyServer(endpointUrlSuffix, postBody);
			responseFromCPServer = mapper.readValue(responseFromCPServerAsString, RequestResponse.class);
		} catch (JsonProcessingException e) {
			logger.error("Unable to call generateMezzanineProxies on CP Proxy server for " + volumeId + "::" + e.getMessage(), e);
		}
		return responseFromCPServer;
	}
	
	public RequestResponse frameRequestResponse(Request request, RequestType requestType, Integer userReqId){
		RequestResponse requestResponse = new RequestResponse();
		int requestId = request.getId();
		
		requestResponse.setId(requestId);
		requestResponse.setType(request.getType().name()); 
		requestResponse.setUserRequestId(userReqId);
		requestResponse.setRequestedAt(getDateForUI(request.getRequestedAt()));
		requestResponse.setCompletedAt(getDateForUI(request.getCompletedAt()));
		requestResponse.setRequestedBy(request.getRequestedBy().getName());
		requestResponse.setPriority(request.getPriority() != null ? request.getPriority().name() : Priority.normal.name());
		requestResponse.setStatus(request.getStatus().name());
		Action requestAction = request.getActionId();
		requestResponse.setAction(requestAction.name());
		if(requestType == RequestType.user) {
			List<RequestResponse> systemRequestResponseList = new ArrayList<RequestResponse>();
			List<Request> systemRequestList = requestDao.findAllByRequestRefId(requestId);

			for (Request nthSystemRequest : systemRequestList) {
				systemRequestResponseList.add(frameRequestResponse(nthSystemRequest, RequestType.system, userReqId));
			}
			requestResponse.setRequest(systemRequestResponseList);
		}
		if(requestType == RequestType.system) {		

			List<JobResponse> jobRespList = jobService.getJobs(request.getId(), null, false);
			requestResponse.setJob(jobRespList);
			
			
			int inputArtifactId = jobRespList.get(0).getInputArtifactId();
			Artifact systemArtifact = artifactDao.findById(inputArtifactId).get(); // TODO use Artifactclass().isSource() instead of orderBy
			org.ishafoundation.dwaraapi.api.resp.request.Artifact artifactForResponse = new org.ishafoundation.dwaraapi.api.resp.request.Artifact();
			artifactForResponse.setId(inputArtifactId);
			artifactForResponse.setName(systemArtifact.getName());
			requestResponse.setArtifact(artifactForResponse);
		}
		return requestResponse;
	}


	public void rewriteVolume(String volumeId, RewriteRequest rewriteRequest) throws Exception {
		//Optional<Volume> volumeEntity = volumeDao.findById(volumeId);
		Volume volume = volumeDao.findById(volumeId).get();
		if(volume.getHealthstatus() != VolumeHealthStatus.defective)
			throw new Exception(volumeId + " not flagged as defective. Please double check and flag it first");
		
		List<Status> statusList = new ArrayList<Status>();
		statusList.add(Status.queued);
		statusList.add(Status.in_progress);
		
		List<Request> rewriteSystemRequestList = requestDao.findAllByActionIdAndStatusInAndType(Action.rewrite, statusList, RequestType.user);
		if(rewriteSystemRequestList.size() > 0)
			throw new Exception("Already there is a rewrite request in_progress " + rewriteSystemRequestList.get(0).getId() + ". System can handle only one rewrite request at a time");
		
		// create user request
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("volumeId", volumeId);
		data.put("mode", rewriteRequest.getMode());
		data.put("souceCopy", rewriteRequest.getSourceCopy());
		Integer additionalCopy = rewriteRequest.getDestinationCopy();
		if(additionalCopy != null)
			data.put("destinationCopy", additionalCopy);
		
		Pattern artifactclassRegexPattern = null;
		String artifactclassRegex = rewriteRequest.getArtifactclassRegex();
		if(artifactclassRegex != null) {
			artifactclassRegexPattern = Pattern.compile(artifactclassRegex);
			data.put("artifactclassRegex", artifactclassRegex);
		}
		Request userRequest = createUserRequest(Action.rewrite, data);
		List<ArtifactVolume> artifactVolumeList = artifactVolumeDao.findAllByIdVolumeIdAndStatus(volumeId, ArtifactVolumeStatus.current); // only not deleted artifacts need to be rewritten
		
		// loop artifacts on volume
		for (ArtifactVolume nthArtifactVolume : artifactVolumeList) {
			int artifactId = nthArtifactVolume.getId().getArtifactId();
			
			Artifact artifact = artifactDao.findById(artifactId).get(); // get the artifact details from DB
 
			// filtering out artifacts that dont match specified artifactclass(es)
			if(artifactclassRegexPattern != null) {
				String artifactclassId = artifact.getArtifactclass().getId();
				Matcher m = artifactclassRegexPattern.matcher(artifactclassId);
				if(!m.matches()) { 
					logger.info("Skipping " + artifact.getName() + "(" + artifactId + ") as " + artifactclassId + " doesnt match " + artifactclassRegex);
					continue;
				}
			}

			// Also System will skip any artifacts which already exist on the additional copy (e.g. if the config was changed to increase the number of copies from 3 to 4 while a tape was partially written - so the artifacts on the latter part of that tape would need to be skipped)
			if(additionalCopy != null) {
				boolean isCopyAlreadyWritten = false; 
				List<ArtifactVolume> artifactVolumeList2 = artifactVolumeDao.findAllByIdArtifactIdAndStatus(artifactId, ArtifactVolumeStatus.current);
				for (ArtifactVolume nthArtifactVolume2 : artifactVolumeList2) {
					if(nthArtifactVolume2.getVolume().getGroupRef().getCopy().getId() == additionalCopy) {
						isCopyAlreadyWritten = true;
						break;
					}
				}
				if(isCopyAlreadyWritten) {
					logger.info("Skipping " + artifact.getName() + "(" + artifactId + ") as additional copy already written");
					continue;
				}
			}
			
			//create system requests			
			Request systemRequest = new Request();
			systemRequest.setType(RequestType.system);
			systemRequest.setRequestRef(userRequest);
			systemRequest.setActionId(userRequest.getActionId());
			systemRequest.setStatus(Status.queued);
			systemRequest.setRequestedBy(userRequest.getRequestedBy());
			systemRequest.setRequestedAt(LocalDateTime.now());
		
			RequestDetails systemrequestDetails = new RequestDetails();
			systemrequestDetails.setArtifactId(artifactId);
			systemrequestDetails.setVolumeId(volumeId);
			systemrequestDetails.setMode(RewriteMode.valueOf(rewriteRequest.getMode()));
			systemrequestDetails.setSourceCopy(rewriteRequest.getSourceCopy());
			if(rewriteRequest.getDestinationCopy() != null)
				systemrequestDetails.setDestinationCopy(rewriteRequest.getDestinationCopy());
			if(rewriteRequest.getArtifactclassRegex() != null)
				systemrequestDetails.setArtifactclassRegex(rewriteRequest.getArtifactclassRegex());
			systemRequest.setDetails(systemrequestDetails);
			systemRequest = requestDao.save(systemRequest);
			
			logger.info(DwaraConstants.SYSTEM_REQUEST + systemRequest.getId());
			

			//create jobs
			jobCreator.createJobs(systemRequest, artifact);
		}
	}

	public MarkVolumeStatusResponse markVolumeHealthstatus(String volumeId, String healthstatus, MarkVolumeStatusRequest markVolumeStatusRequest) throws Exception {
		MarkVolumeStatusResponse markVolumeStatusResponse = new MarkVolumeStatusResponse();
		
		Volume volume = volumeDao.findById(volumeId).get();
		if(volume == null)
			throw new Exception(volumeId + " not found");
		
		VolumeHealthStatus volumeStatus = null;
		try {
			volumeStatus = VolumeHealthStatus.valueOf(healthstatus);
			if(volumeStatus == null)
				throw new Exception(healthstatus + " not supported");
				
		}catch (Exception e) {
			throw new Exception(healthstatus + " not supported");
		}
		
		String reason = markVolumeStatusRequest.getReason();
		// create user request
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("volumeId", volumeId);
		data.put("healthstatus", healthstatus);
		data.put("reason", reason);
		Request userRequest = createUserRequest(Action.mark_volume, Status.completed, data);
		userRequest.setMessage(reason);
		requestDao.save(userRequest);
		
		volume.setHealthstatus(volumeStatus);

		volumeDao.save(volume);
		
		markVolumeStatusResponse.setRequestId(userRequest.getId());
		markVolumeStatusResponse.setRequestedBy(userRequest.getRequestedBy().getName());
		markVolumeStatusResponse.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
		
		markVolumeStatusResponse.setVolumeId(volumeId);
		markVolumeStatusResponse.setVolumeStatus(healthstatus);
		
		markVolumeStatusResponse.setAction(userRequest.getActionId().name());
		markVolumeStatusResponse.setStatus(userRequest.getStatus().name());
		markVolumeStatusResponse.setCompletedAt(getDateForUI(userRequest.getCompletedAt()));
		
		return markVolumeStatusResponse;
	}
	
	public MarkVolumeStatusResponse markVolumeLifecyclestage(String volumeId, String lifecyclestage, MarkVolumeStatusRequest markVolumeStatusRequest) throws Exception {
		MarkVolumeStatusResponse markVolumeStatusResponse = new MarkVolumeStatusResponse();
		
		Volume volume = volumeDao.findById(volumeId).get();
		if(volume == null)
			throw new Exception(volumeId + " not found");
		
		VolumeLifecyclestage volumeStatus = null;
		try {
			volumeStatus = VolumeLifecyclestage.valueOf(lifecyclestage);
			if(volumeStatus == null)
				throw new Exception(lifecyclestage + " not supported");
				
		}catch (Exception e) {
			throw new Exception(lifecyclestage + " not supported");
		}
		
		String reason = markVolumeStatusRequest.getReason();
		// create user request
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("volumeId", volumeId);
		data.put("lifecyclestage", lifecyclestage);
		data.put("reason", reason);
		Request userRequest = createUserRequest(Action.mark_volume, Status.completed, data);
		userRequest.setMessage(reason);
		requestDao.save(userRequest);
		
		volume.setLifecyclestage(volumeStatus);

		volumeDao.save(volume);
		
		markVolumeStatusResponse.setRequestId(userRequest.getId());
		markVolumeStatusResponse.setRequestedBy(userRequest.getRequestedBy().getName());
		markVolumeStatusResponse.setRequestedAt(getDateForUI(userRequest.getRequestedAt()));
		
		markVolumeStatusResponse.setVolumeId(volumeId);
		markVolumeStatusResponse.setVolumeStatus(lifecyclestage);
		
		markVolumeStatusResponse.setAction(userRequest.getActionId().name());
		markVolumeStatusResponse.setStatus(userRequest.getStatus().name());
		markVolumeStatusResponse.setCompletedAt(getDateForUI(userRequest.getCompletedAt()));
		
		return markVolumeStatusResponse;
	}
}

